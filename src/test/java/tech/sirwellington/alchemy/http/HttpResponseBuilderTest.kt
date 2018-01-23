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
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.integers
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.negativeIntegers
import tech.sirwellington.alchemy.http.Generators.jsonNull
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat
class HttpResponseBuilderTest
{

    private val gson = Constants.DEFAULT_GSON

    private lateinit var response: TestResponse
    private lateinit var responseBody: JsonElement

    private lateinit var instance: HttpResponse.Builder

    @Before
    fun setUp()
    {
        instance = HttpResponse.Builder.newInstance()

        response = TestResponse()
        responseBody = response.responseBody
    }

    @Test
    fun testWithStatusCode()
    {
        val goodStatusCode = one(integers(200, 500))
        val result = instance.withStatusCode(goodStatusCode)
        assertThat(result, notNullValue())

        val badStatusCode = one(integers(520, 10000))
        assertThrows { instance.withStatusCode(badStatusCode) }
                .isInstanceOf(IllegalArgumentException::class.java)

        val negativeStatusCode = one(negativeIntegers())
        assertThrows { instance.withStatusCode(negativeStatusCode) }
                .isInstanceOf(IllegalArgumentException::class.java)

    }

    @Test
    fun testUsingGson()
    {
        val result = instance.usingGson(gson)
        assertThat(result, notNullValue())
    }

    @Test
    fun testWithResponseBody()
    {
        val result = instance.withResponseBody(responseBody)
        assertThat(result, notNullValue())

        instance.withResponseBody(one(jsonNull()))
    }

    @Test
    fun testWithResponseHeaders()
    {
        val result = instance.withResponseHeaders(response.responseHeaders)
        assertThat(result, notNullValue())

        //Empty Map is ok
        instance.withResponseHeaders(emptyMap())
        instance.withResponseHeaders(null)
    }

    @Test
    fun testBuild()
    {
        val result = instance
                .withResponseBody(responseBody)
                .withResponseHeaders(response.responseHeaders)
                .withStatusCode(response.statusCode)
                .build()

        assertThat(result, notNullValue())
        assertThat(result.equals(response), equalTo(true))
        assertThat(response.equals(result), equalTo(true))

    }

    @Test
    fun testBuildMissingStatusCode()
    {
        instance.withResponseBody(responseBody)
                .withResponseHeaders(response.responseHeaders)

        assertThrows { instance.build() }
                .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun testMergeFrom()
    {
        instance.copyFrom(response)

        val result = instance.build()
        assertThat<HttpResponse>(result, equalTo(response))
        assertThat(response, equalTo(result))
    }

    @Test
    @Throws(Exception::class)
    fun testSetResponseHeadersWithBadArgs()
    {
        assertThrows { response.responseHeaders = null as Map<String, String> }
    }

    @Test
    fun testMergeFromEdgeCases()
    {
        val headers = response.responseHeaders
        response.responseHeaders = Maps.emptyMap()
        val result = instance.copyFrom(response).build()
        assertThat(result, notNullValue())

        response.responseHeaders = headers
        response.statusCode = one(negativeIntegers())
        assertThrows { instance.copyFrom(response) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

}
