/*
 * Copyright © 2019. Sir Wellington.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.sirwellington.alchemy.http

import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import tech.sirwellington.alchemy.arguments.FailedAssertionException
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.integers
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.hexadecimalString
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.net.MalformedURLException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class HttpAssertionsTest
{

    @GenerateEnum
    private val requestMethod: RequestMethod? = null

    @Before
    fun setUp()
    {
    }

    @Test
    fun testConstructor()
    {
        assertThrows { HttpAssertions::class.java.newInstance() }
                .isInstanceOf(IllegalAccessException::class.java)
    }

    @Repeat
    @Test
    fun testValidHttpStatusCode()
    {
        val instance = HttpAssertions.validHttpStatusCode()
        assertThat(instance, notNullValue())

        val statusCode = one(integers(200, 500))
        instance.check(statusCode)

        val badStatusCode = one(integers(-100, 100))

        assertThrows { instance.check(badStatusCode) }
                .isInstanceOf(FailedAssertionException::class.java)

        val anotherBadCode = one(integers(600, Int.MAX_VALUE))
        assertThrows { instance.check(anotherBadCode) }
                .isInstanceOf(FailedAssertionException::class.java)
    }

    @Test
    fun testValidResponseClass()
    {
        //Check Object
        val instanceOne = HttpAssertions.validResponseClass<Any>()
        assertThat(instanceOne, notNullValue())
        instanceOne.check(Any::class.java)

        //Check String
        val instanceTwo = HttpAssertions.validResponseClass<String>()
        instanceTwo.check(String::class.java)

        //Edge Cases
        assertThrows { instanceOne.check(null) }
                .isInstanceOf(FailedAssertionException::class.java)

        val instanceThree = HttpAssertions.validResponseClass<Void>()
        assertThrows { instanceThree.check(Void::class.java) }
                .isInstanceOf(FailedAssertionException::class.java)
    }

    @Test
    @Throws(MalformedURLException::class)
    fun testRequestReady()
    {
        val instance = HttpAssertions.ready()

        val url = one(Generators.validUrls())

        val request = mock(HttpRequest::class.java)
        whenever(request.url).thenReturn(url)
        whenever(request.method).thenReturn(requestMethod)

        instance.check(request)
    }

    @Repeat
    @Test
    @Throws(MalformedURLException::class)
    fun testRequestReadyEdgeCases()
    {
        val instance = HttpAssertions.ready()

        //Edge cases
        assertThrows { instance.check(null) }
                .isInstanceOf(FailedAssertionException::class.java)

        val url = one(Generators.validUrls())
        val request = mock(HttpRequest::class.java)
        whenever(request.url).thenReturn(url)
        whenever(request.method).thenReturn(requestMethod)

        //Missing Request Method
        whenever(request.method).thenReturn(null)

        assertThrows { instance.check(request) }
                .isInstanceOf(FailedAssertionException::class.java)

        whenever(request.method).thenReturn(requestMethod)

        //Missing URL
        whenever(request.url).thenReturn(null)

        assertThrows { instance.check(request) }
                .isInstanceOf(FailedAssertionException::class.java)

        //Bad URL
        val badUrl = URL("file://" + one(alphabeticStrings()))
        whenever(request.url).thenReturn(badUrl)

        assertThrows { instance.check(request) }
                .isInstanceOf(FailedAssertionException::class.java)

    }

    @Repeat(10)
    @Test
    fun testValidContentType()
    {
        val instance = HttpAssertions.validContentType()
        assertThat(instance, notNullValue())

        val validTypes = StringGenerators.stringsFromFixedList(ContentTypes.APPLICATION_JSON, ContentTypes.PLAIN_TEXT)

        val contentType = one(validTypes)

        instance.check(contentType)
        instance.check(contentType + one<String>(alphabeticStrings()))
    }

    @Test
    fun testValidContentTypeEdgeCases()
    {
        val instance = HttpAssertions.validContentType()

        //Edge cases
        assertThrows { instance.check(null) }
                .isInstanceOf(FailedAssertionException::class.java)

        assertThrows { instance.check("") }
                .isInstanceOf(FailedAssertionException::class.java)

        assertThrows { instance.check(one(alphabeticStrings())) }
                .isInstanceOf(FailedAssertionException::class.java)

        assertThrows { instance.check(one(hexadecimalString(10))) }
                .isInstanceOf(FailedAssertionException::class.java)
    }

    @Repeat
    @Test
    fun testJsonArray()
    {
        val instance = HttpAssertions.jsonArray()
        assertThat(instance, notNullValue())

        val valid = one(Generators.jsonArrays())
        instance.check(valid)

        val `object` = one(Generators.jsonObjects())
        assertThrows { instance.check(`object`) }
                .isInstanceOf(FailedAssertionException::class.java)

        val primitive = one(Generators.jsonPrimitives())
        assertThrows { instance.check(primitive) }
                .isInstanceOf(FailedAssertionException::class.java)

    }

    @Test
    fun testOkResponse()
    {
        val instance = HttpAssertions.okResponse()
        assertThat(instance, notNullValue())

        //Check with null argument
        assertThrows { instance.check(null) }
                .isInstanceOf(FailedAssertionException::class.java)

        //Response is OK
        val okResponse = mock(HttpResponse::class.java)
        whenever(okResponse.isOk).thenReturn(true)
        instance.check(okResponse)

        //Response is NOT OK
        val notOkResponse = mock(HttpResponse::class.java)
        whenever(notOkResponse.isOk).thenReturn(false)
        assertThrows { instance.check(notOkResponse) }
                .isInstanceOf(FailedAssertionException::class.java)

    }

    companion object
    {

        fun runTheThing()
        {

        }
    }

}
