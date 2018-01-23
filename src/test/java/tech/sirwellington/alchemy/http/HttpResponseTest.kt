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

import com.google.gson.JsonObject
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.generator.AlchemyGenerator
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.integers
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.strings
import tech.sirwellington.alchemy.http.Generators.jsonElements
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat
class HttpResponseTest
{

    private val gson = Constants.DEFAULT_GSON

    private lateinit var first: TestResponse
    private lateinit var second: TestResponse

    private lateinit var pojo: TestPojo
    private lateinit var pojoAsJson: JsonObject

    private lateinit var builder: HttpResponse.Builder

    @Before
    fun setUp()
    {
        first = TestResponse()
        second = first.copy()

        pojo = TestPojo.generate()
        pojoAsJson = gson.toJsonTree(pojo).asJsonObject

        builder = HttpResponse.builder().copyFrom(first!!)
    }

    @Test
    fun testStatusCode()
    {
        val instance = builder.build()
        assertThat(instance.statusCode(), equalTo(first.statusCode))
    }

    @Test
    fun testIsOk()
    {
        first.statusCode = one(integers(200, 209))
        builder = builder.copyFrom(first)

        val instance = builder.build()
        assertThat(instance.isOk, equalTo(true))
    }

    @Test
    fun testIsOkWhenNotOk()
    {
        first.statusCode = one(integers(400, 506))
        builder = builder.copyFrom(first)

        val instance = builder.build()
        assertThat(instance.isOk, equalTo(false))
    }

    @Test
    fun testResponseHeaders()
    {
        val instance = builder.build()
        assertThat(instance.responseHeaders(), equalTo(first.responseHeaders))

        val value = one(alphabeticStrings())
        val headers = instance.responseHeaders() as? java.util.Map<String, String>
        assertThrows { headers?.put(value, value) }
    }

    @Test
    fun testBody()
    {
        val instance = builder.build()
        assertThat(instance.body(), equalTo(first.responseBody))
    }

    @Test
    fun testBodyAsString()
    {
        val instance = builder.build()
        val asString = instance.bodyAsString()
        val expected = first.responseBody.toString()
        assertThat(asString, equalTo(expected))
    }

    @Test
    fun testBodyAs()
    {
        first.responseBody = pojoAsJson
        val instance = builder.copyFrom(first!!).build()

        val result = instance.bodyAs(TestPojo::class.java)
        assertThat(result == pojo, equalTo(true))
    }

    @Test
    fun testBodyAsArrayOf()
    {
        val pojos = CollectionGenerators.listOf(AlchemyGenerator { TestPojo.generate() })
        val jsonArray = gson.toJsonTree(pojos)
        assertThat(jsonArray.isJsonArray, equalTo(true))
        first.responseBody = jsonArray

        val instance = builder.copyFrom(first).build()
        val result = instance.bodyAsArrayOf(TestPojo::class.java)
        assertThat(result, equalTo(pojos))
    }

    @Test
    fun testEqualsWhenTrue()
    {
        assertBothEquals()
    }

    @Test
    fun testEqualsWhenStatusCodeDifferent()
    {
        do
        {
            second.statusCode = one(integers(200, 500))
        } while (second.statusCode == first.statusCode)

        assertBothDifferent()
    }

    @Test
    fun testEqualsWhenResponseHeadersDifferent()
    {
        do
        {
            second.responseHeaders = CollectionGenerators.mapOf(strings(10),
                                                                strings(10),
                                                                20)

        } while (second.responseHeaders == first.responseHeaders)
    }

    @Test
    fun testEqualsWhenResponseBodyDifferent()
    {
        do
        {
            second.responseBody = one(jsonElements())
        } while (second.responseBody == first.responseBody)
    }

    private fun assertBothEquals()
    {
        assertThat(second, equalTo<TestResponse>(first))
        assertThat(first.equals(second), equalTo(true))
        assertThat(second.equals(first), equalTo(true))
    }

    private fun assertBothDifferent()
    {
        assertThat(second, not<TestResponse>(first))
        assertThat(first.equals(second), equalTo(false))
        assertThat(second.equals(first), equalTo(false))
    }

    @Test
    fun testBuilder()
    {
        assertThat(HttpResponse.builder(), notNullValue())
    }

}
