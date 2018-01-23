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
import com.nhaarman.mockito_kotlin.whenever
import io.mikael.urlbuilder.UrlBuilder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.http.Generators.jsonElements
import tech.sirwellington.alchemy.http.Generators.validUrls
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum
import tech.sirwellington.alchemy.test.junit.runners.GenerateURL
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner::class)
class HttpConnectionPreparerTest
{

    private lateinit var instance: HttpConnectionPreparer

    @GenerateURL
    private lateinit var url: URL

    @GenerateURL
    private lateinit var expandedUrl: URL

    @GenerateEnum
    private lateinit var requestMethod: RequestMethod

    @Mock
    private lateinit var request: HttpRequest

    private lateinit var body: JsonElement

    private lateinit var queryParams: Map<String, String>


    @Before
    @Throws(Exception::class)
    fun setUp()
    {
        body = one(jsonElements())
        queryParams = CollectionGenerators.mapOf(alphabeticStrings(10),
                                                 alphabeticStrings(10),
                                                 10)
        url = one(validUrls())
        expandedUrl = expandUrl()

        whenever(request.url).thenReturn(url)
        whenever(request.body).thenReturn(body)
        whenever(request.queryParams).thenReturn(queryParams)
        whenever(request.method).thenReturn(requestMethod)

        instance = HttpConnectionPreparer.create()
    }


    @Test
    @Throws(Exception::class)
    fun testMap()
    {

        whenever(request.hasBody()).thenReturn(true)

        val result = instance.map(request)
        assertThat(result, notNullValue())
        assertThat(result.requestMethod, equalTo(requestMethod.asString))
        assertThat(result.doInput, equalTo(true))
        assertThat(result.doOutput, equalTo(true))

        for ((key, value1) in result.requestProperties)
        {
            val value = value1.joinToString(", ")

            assertThat(queryParams.containsKey(key), equalTo(true))
            assertThat(queryParams[key], equalTo(value))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMapExpandsURL()
    {
        instance = HttpConnectionPreparer.create()

        whenever(request.hasQueryParams())
                .thenReturn(java.lang.Boolean.TRUE)

        val result = instance.map(request)
        assertThat(result.url, equalTo<URL>(expandedUrl))
    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testExpandUrlFromRequestWhenNoQueryParams()
    {
        whenever(request.hasQueryParams()).thenReturn(false)

        val result = HttpConnectionPreparer.expandUrlFromRequest(request)
        assertThat(result, equalTo(url))
    }

    @Test
    @Throws(Exception::class)
    fun testExpandUrlFromRequestWhenQueryParamsPresent()
    {
        //When there are query params
        whenever(request.hasQueryParams()).thenReturn(true)

        val result = HttpConnectionPreparer.expandUrlFromRequest(request)
        assertThat(result, equalTo(expandedUrl))
    }

    @Throws(URISyntaxException::class, MalformedURLException::class)
    private fun expandUrl(): URL
    {
        var builder = UrlBuilder.fromUrl(url)

        for ((key, value) in queryParams)
        {
            builder = builder.addParameter(key, value)
        }

        return builder.toUrl()
    }

}
