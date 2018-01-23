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

import com.google.gson.Gson
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.integers
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.negativeIntegers
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.smallPositiveIntegers
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.asString
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.hexadecimalString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateLong
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 *
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner::class)
class AlchemyHttpBuilderTest
{

    @Mock
    private lateinit var executor: ExecutorService


    private lateinit var defaultHeaders: Map<String, String>

    private lateinit var instance: AlchemyHttpBuilder

    @GenerateLong(min = 100)
    private var timeout: Long = 0L

    @Before
    fun setUp()
    {
        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 20)
        timeout = NumberGenerators.longs(100, 2000).get()

        instance = AlchemyHttpBuilder()
                .usingTimeout(Math.toIntExact(timeout), TimeUnit.MILLISECONDS)
                .usingExecutor(executor)
                .usingDefaultHeaders(defaultHeaders)
    }

    @Test
    fun testNewInstance()
    {
        instance = AlchemyHttpBuilder.newInstance()
        assertThat<AlchemyHttpBuilder>(instance, notNullValue())
    }

    @Repeat(50)
    @Test
    fun testUsingTimeout()
    {
        val socketTimeout = one(integers(15, 100))
        val result = instance.usingTimeout(socketTimeout, TimeUnit.SECONDS)
        assertThat(result, notNullValue())
    }

    @Repeat(10)
    @Test
    fun testUsingTimeoutWithBadArgs()
    {
        val negativeNumber = one(negativeIntegers())

        assertThrows { instance.usingTimeout(negativeNumber, TimeUnit.SECONDS) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testUsingGson()
    {
        val gson = Gson()
        val result = instance.usingGson(gson)
        assertThat(result, notNullValue())

    }

    @Repeat(100)
    @Test
    fun testUsingExecutorService()
    {
        val result = instance.usingExecutor(executor)
        assertThat(result, notNullValue())
    }

    @Test
    fun testDisableAsyncCallbacks()
    {
        val result = instance.disableAsyncCallbacks()
        assertThat(result, notNullValue())
    }

    @Test
    fun testEnableAsyncCallbacks()
    {
        val result = instance.enableAsyncCallbacks()
        assertThat(result, notNullValue())
    }

    @Repeat(100)
    @Test
    fun testUsingDefaultHeaders()
    {
        instance = AlchemyHttpBuilder.newInstance()

        val headers = CollectionGenerators.mapOf(alphabeticStrings(),
                                                 asString(smallPositiveIntegers()),
                                                 100)

        val result = instance.usingDefaultHeaders(headers)
        assertThat(result, notNullValue())

        val http = result.build()
        assertThat(http, notNullValue())

        val expected = headers + Constants.DEFAULT_HEADERS
        assertThat(http.defaultHeaders, equalTo(expected))

        //Empty headers is ok
        instance.usingDefaultHeaders(emptyMap())
    }

    @Repeat
    @Test
    fun testUsingDefaultHeader()
    {
        val key = one(alphabeticStrings())
        val value = one(hexadecimalString(10))

        val result = instance.usingDefaultHeader(key, value)
        assertThat(result, notNullValue())

        val http = result.build()
        assertThat(http.defaultHeaders, Matchers.hasEntry(key, value))
    }

    @Test
    fun testUsingDefaultHeaderEdgeCases()
    {
        val key = one(alphabeticStrings())
        //should be ok
        instance.usingDefaultHeader(key, "")
    }

    @Repeat(100)
    @Test
    fun testBuild()
    {

        val result = instance.build()
        assertThat(result, notNullValue())
        val expectedHeaders = this.defaultHeaders + Constants.DEFAULT_HEADERS
        assertThat(result.defaultHeaders, equalTo(expectedHeaders))
    }

    @Test
    fun testBuildEdgeCases()
    {
        //Nothing is set
        instance = AlchemyHttpBuilder.newInstance()
        instance.build()

        //No Executor Service set
        instance = AlchemyHttpBuilder.newInstance()
        instance.build()

        //No Timeout
        instance = AlchemyHttpBuilder.newInstance().usingExecutor(executor)
        instance.build()
    }

    @Test
    fun testDefaultIncludesBasicRequestHeaders()
    {
        instance = AlchemyHttpBuilder.newInstance()
                .usingExecutor(executor)

        val result = instance.build()
        assertThat(result, notNullValue())
        val headers = result.defaultHeaders
        assertThat(headers, Matchers.hasKey("Accept"))
        assertThat(headers, Matchers.hasKey("Content-Type"))

    }


}
