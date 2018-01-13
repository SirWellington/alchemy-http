/*
 * Copyright © 2018. Sir Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import sir.wellington.alchemy.collections.lists.Lists
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.hexadecimalString
import tech.sirwellington.alchemy.http.Generators.jsonElements
import tech.sirwellington.alchemy.http.Generators.jsonObjects
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.System.currentTimeMillis
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat(100)
class HttpExecutorImplTest
{

    @Mock
    private lateinit var requestMapper: AlchemyRequestMapper

    @Mock
    private lateinit var request: HttpRequest

    @Mock
    private lateinit var httpConnection: HttpURLConnection

    @Mock
    private lateinit var output: OutputStream

    @Mock
    private lateinit var input: InputStream

    private lateinit var responseBody: JsonElement
    private lateinit var responseString: String


    private var responseHeaders: Map<String, String>? = null
    private val gson = Constants.DEFAULT_GSON
    private var timeout: Long = 0

    private lateinit var instance: HttpExecutor


    @Before
    @Throws(IOException::class)
    fun setUp()
    {
        instance = HttpExecutorImpl(requestMapper!!)

        verifyZeroInteractions(requestMapper)

        timeout = NumberGenerators.smallPositiveLongs().get()
        whenever(requestMapper.map(request)).thenReturn(httpConnection)

        setupResponse()
    }

    @Throws(IOException::class)
    fun setupResponse()
    {
        setupResponseBody()
        setupResponseHeaders()
        whenever(httpConnection.responseCode).thenReturn(200)

    }

    @Throws(IOException::class)
    private fun setupResponseBody()
    {
        responseBody = one(jsonElements())
        responseString = responseBody.toString()

        val bytes = responseString.toByteArray(Charsets.UTF_8)
        input = ByteArrayInputStream(bytes)
        input = spy(input!!)

        whenever(httpConnection.inputStream).thenReturn(input)
        whenever(httpConnection.outputStream).thenReturn(output)
        whenever(httpConnection.contentType).thenReturn(ContentTypes.APPLICATION_JSON)
    }

    fun setupResponseHeaders()
    {
        responseHeaders = CollectionGenerators.mapOf(alphabeticStrings(), hexadecimalString(10), 15)

        val headers = Maps.create<String, List<String>>()

        for ((key, value) in responseHeaders!!)
        {
            headers.put(key, Lists.createFrom(value))
        }

        whenever(httpConnection.headerFields).thenReturn(headers)
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testConstructor()
    {
        assertThrows { HttpExecutorImpl(null!!) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testCreate()
    {
        val result = HttpExecutorImpl.create(requestMapper!!)
        assertThat(result, notNullValue())

        assertThrows { HttpExecutorImpl.create(null!!) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(IOException::class)
    fun testExecute()
    {
        val response = instance.execute(request, gson, timeout)

        assertThat(response, notNullValue())
        assertThat(response.statusCode(), equalTo(httpConnection.responseCode))
        assertThat(response.isOk, equalTo(true))
        assertThat(response.body(), equalTo<JsonElement>(responseBody))
        assertThat(response.responseHeaders(), equalTo<Map<String, String>>(responseHeaders))
        assertThat(response.bodyAsString(), equalTo(responseBody.toString()))

        verify(httpConnection).connectTimeout = timeout.toInt()
    }

    //Edge Cases
    @DontRepeat
    @Test
    fun testExecuteWithBadArgs()
    {
        assertThrows { instance.execute(null!!, gson, 1L) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThrows { instance.execute(request, null!!, 1L) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThrows { instance.execute(request, gson, -1L) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testExecuteWhenRequestMapperReturnsNull()
    {
        whenever(requestMapper.map(request)).thenReturn(null)
        assertThrows { instance.execute(request, gson, timeout) }
    }

    @Test
    @Throws(Exception::class)
    fun testWhenRequestTimesOut()
    {
        whenever(httpConnection.inputStream)
                .thenThrow(SocketTimeoutException::class.java)

        assertThrows { instance.execute(request, gson, timeout) }
                .isInstanceOf(AlchemyHttpException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testWhenResponseBodyIsNull()
    {
        whenever(httpConnection.inputStream)
                .thenReturn(null)

        val response = instance.execute(request, gson, timeout)
        assertThat(response, notNullValue())
        assertThat(response.body(), equalTo<JsonElement>(JsonNull.INSTANCE))
    }

    @Test
    @Throws(Exception::class)
    fun testWhenResponseBodyIsEmpty()
    {
        val binary = "".toByteArray(Charsets.UTF_8)
        val istream = ByteArrayInputStream(binary)
        whenever(httpConnection.inputStream).thenReturn(istream)

        val response = instance.execute(request, gson, timeout)
        assertThat(response, notNullValue())
        assertThat(response.body(), equalTo<JsonElement>(JsonNull.INSTANCE))
    }

    @Test
    @Throws(Exception::class)
    fun testWhenResponseContentTypeIsNotJson()
    {
        whenever(httpConnection.contentType).thenReturn(ContentTypes.PLAIN_TEXT)

        val response = instance.execute(request, gson, timeout)
        assertThat(response, notNullValue())
        assertTrue(response.isOk)

        val expected = JsonPrimitive(responseBody.toString())
        val result = response.body()
        assertThat(result, equalTo<JsonElement>(expected))
    }

    //=============================================
    // PERFORMANCE TESTS
    //=============================================

    @DontRepeat
    @Test
    fun testPerformance()
    {
        val parser = JsonParser()

        println("performance test")
        val body = one(jsonObjects()).toString()

        var time = time {
            parser.parse(body)
        }
        println("Parser took " + time)

        time = time({ gson.fromJson(body, JsonElement::class.java) })
        println("Gson took " + time)

        val iterations = 100

        time = time {
            for (i in 0 until iterations)
            {
                parser.parse(body)
            }
        }

        System.out.printf("Parser took %dms across %d runs\n", time, iterations)

        time = time {

            for (i in 0 until iterations)
            {
                gson.fromJson(body, JsonElement::class.java)
            }
        }
        System.out.printf("Gson took %dms across %d runs\n", time, iterations)
    }

    @DontRepeat
    @Test
    fun compareGsonMethods()
    {
        responseBody = one(jsonObjects())

        val text = responseBody.toString()

        val fromJson = gson.fromJson(text, JsonElement::class.java)
        val toJsonTree = gson.toJsonTree(text)

        val equals = fromJson == toJsonTree
        println("Equal? " + equals)
    }

    private fun time(task: () -> (Unit)): Long
    {
        val start = currentTimeMillis()
        task()
        val end = currentTimeMillis()
        return end - start
    }
}
