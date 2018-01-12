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

import com.nhaarman.mockito_kotlin.mock
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
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.http.AlchemyRequest.OnFailure
import tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess
import tech.sirwellington.alchemy.http.AlchemyRequest.Step1
import tech.sirwellington.alchemy.http.AlchemyRequest.Step5
import tech.sirwellington.alchemy.http.Generators.validUrls
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.io.IOException
import java.net.MalformedURLException
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

    private var headers: Map<String, String>? = null

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
        val instance = object: Step1
        {
            override fun get(): AlchemyRequest.Step3
            {
                throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
            }

            override fun post(): AlchemyRequest.Step2
            {
                throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
            }

            override fun put(): AlchemyRequest.Step2
            {
                throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
            }

            override fun delete(): AlchemyRequest.Step2
            {
                throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
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
        val instance = object : AlchemyRequest.Step3
        {

            var url: URL? = null
            val savedHeaders = Maps.create<String, String>()

            @Throws(IllegalArgumentException::class)
            override fun usingHeader(key: String, value: String): AlchemyRequest.Step3
            {
                savedHeaders.put(key, value)
                return this
            }

            @Throws(IllegalArgumentException::class)
            override fun usingQueryParam(name: String, value: String): AlchemyRequest.Step3
            {
                return this
            }

            @Throws(IllegalArgumentException::class)
            override fun followRedirects(maxNumberOfTimes: Int): AlchemyRequest.Step3
            {
                return this
            }

            @Throws(AlchemyHttpException::class)
            override fun at(url: URL): HttpResponse
            {
                this.url = url
                return HttpResponse.builder().build()
            }

            override fun onSuccess(onSuccessCallback: OnSuccess<HttpResponse>): AlchemyRequest.Step5<HttpResponse>
            {
                throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
            }

            @Throws(IllegalArgumentException::class)
            override fun <ResponseType> expecting(classOfResponseType: Class<ResponseType>): AlchemyRequest.Step4<ResponseType>
            {
                throw UnsupportedOperationException("Not supported yet.") //To change body of generated methods, choose Tools | Templates.
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
        class TestImpl<T> : AlchemyRequest.Step4<T?>
        {

            var url: URL? = null
            var onSuccess: OnSuccess<*>? = null

            @Throws(AlchemyHttpException::class, MalformedURLException::class)
            override fun at(url: String): T?
            {
                return null
            }

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
        class TestImpl<T> : AlchemyRequest.Step6<T>
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
    fun testOnFailureNoOp()
    {
        val instance = OnFailure.NO_OP
        assertThat(instance, notNullValue())

        val ex = AlchemyHttpException()
        instance.handleError(ex)
    }

}
