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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.BinaryGenerators
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.*
import tech.sirwellington.alchemy.http.Generators.validUrls
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.io.IOException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner::class)
class AlchemyRequestTest
{

    private var url: URL? = null

    private lateinit var headers: Map<String, String>

    @Before
    fun setUp()
    {
        url = one(validUrls())
        headers = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 14)
    }

    @Test
    fun testSomeMethod()
    {
    }

    @Test
    @Throws(IOException::class)
    fun testStep1()
    {
        val instance = object : Step1
        {
            override fun get(): AlchemyRequestSteps.Step3
            {
                throw UnsupportedOperationException("Not supported yet.")
            }

            override fun post(): AlchemyRequestSteps.Step2
            {
                throw UnsupportedOperationException("Not supported yet.")
            }

            override fun put(): AlchemyRequestSteps.Step2
            {
                throw UnsupportedOperationException("Not supported yet.")
            }

            override fun delete(): AlchemyRequestSteps.Step2
            {
                throw UnsupportedOperationException("Not supported yet.")
            }

            override fun method(requestMethod: RequestMethod): Step2
            {
                throw UnsupportedOperationException("Not supported yet.")
            }
        }

        //Test the built-in download()
        val data = BinaryGenerators.binary(100000).get()
        val tempFile = TestFile.writeToTempFile(data)
        val result = instance.download(tempFile.toURI().toURL())
        assertThat(result, `is`<ByteArray>(data))
    }

    @Test
    @Throws(Exception::class)
    fun testStep3()
    {
        val instance = object : AlchemyRequestSteps.Step3
        {

            var url: URL? = null
            val savedHeaders = Maps.create<String, String>()

            @Throws(IllegalArgumentException::class)
            override fun usingHeader(key: String, value: String): AlchemyRequestSteps.Step3
            {
                savedHeaders[key] = value
                return this
            }

            @Throws(IllegalArgumentException::class)
            override fun usingQueryParam(name: String, value: String): AlchemyRequestSteps.Step3
            {
                return this
            }

            @Throws(IllegalArgumentException::class)
            override fun followRedirects(maxNumberOfTimes: Int): AlchemyRequestSteps.Step3
            {
                return this
            }

            @Throws(AlchemyHttpException::class)
            override fun at(url: URL): HttpResponse
            {
                this.url = url
                return mock { }
            }

            override fun onSuccess(onSuccessCallback: OnSuccess<HttpResponse>): AlchemyRequestSteps.Step5<HttpResponse>
            {
                throw UnsupportedOperationException("Not supported yet.")
            }

            @Throws(IllegalArgumentException::class)
            override fun <ResponseType> expecting(classOfResponseType: Class<ResponseType>): AlchemyRequestSteps.Step4<ResponseType>
            {
                throw UnsupportedOperationException("Not supported yet.")
            }
        }

        //Test the built-in at(String) function
        instance.at(url!!.toString())
        assertThat(instance.url, equalTo(this.url))
        assertThrows { instance.at("") }
                .isInstanceOf(IllegalArgumentException::class.java)

        //Test the built-in accepts(String...) function
        val types = alphabeticStrings()
        val first = types.get()
        val second = types.get()
        val third = types.get()

        instance.accept(first, second, third)

        assertThat(instance.savedHeaders, hasKey("Accept"))
        val expected = "$first,$second,$third"
        assertThat<String>(instance.savedHeaders["Accept"], `is`<String>(expected))

        //Edge cases
        assertThrows { instance.accept("", "") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testStep4()
    {
        class TestImpl<T> : AlchemyRequestSteps.Step4<T?>
        {

            var url: URL? = null
            var onSuccess: OnSuccess<*>? = null

            @Throws(IllegalArgumentException::class, AlchemyHttpException::class)
            override fun at(url: URL): T?
            {
                this.url = url
                return null
            }

            override fun onSuccess(onSuccessCallback: OnSuccess<T?>): Step5<T?>
            {
                this.onSuccess = onSuccessCallback
                return mock {  }
            }
        }

        val instance = TestImpl<String>()

        //Test built-in URL method
        instance.at(url!!.toString())
        assertThat(instance.url, equalTo(this.url))

        //Edge cases
        assertThrows { instance.at("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testStep6()
    {
        class TestImpl<T> : AlchemyRequestSteps.Step6<T>
        {

            var url: URL? = null

            override fun at(url: URL)
            {
                this.url = url
            }

        }

        val instance = TestImpl<String>()

        //Test built-in at() method
        instance.at(url!!.toString())
        assertThat(instance.url, equalTo(this.url))

        //Edge cases
        assertThrows { instance.at("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testOnSuccessNoOp()
    {
        val instance = OnSuccess.NO_OP
        assertThat(instance, notNullValue())

        val response = one(alphabeticStrings())
        instance.processResponse(response)
    }

    @Test
    fun testOnSuccessCreate()
    {
        val string = StringGenerators.hexadecimalString(30).get()
        val mock = mock<OnSuccess<String>> { }

        val result = OnSuccess.create<String> { mock.processResponse(string) }

        result.processResponse(string)
        verify(mock).processResponse(string)
    }

    @Test
    fun testOnFailureNoOp()
    {
        val instance = OnFailure.NO_OP
        assertThat(instance, notNullValue())

        val ex = AlchemyHttpException()
        instance.handleError(ex)
    }

    @Test
    fun testOnFailureCreate()
    {
        val mock = mock<OnFailure> { }
        val ex = AlchemyHttpException(Generators.validUrls().get().toString())
        val result = OnFailure.create { mock.handleError(ex) }

        result.handleError(ex)
        verify(mock).handleError(ex)

    }

    interface MockInterface<ResponseType>
    {
        fun handleError(ex: AlchemyHttpException)

        fun handleSuccess(response: ResponseType)
    }
}
