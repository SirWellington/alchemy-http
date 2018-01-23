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

import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.argumentCaptor
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.sameInstance
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.Companion.booleans
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.integers
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.smallPositiveIntegers
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.hexadecimalString
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step3
import tech.sirwellington.alchemy.http.Generators.validUrls
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.net.MalformedURLException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat
class Step3ImplTest
{

    @Mock
    private lateinit var stateMachine: AlchemyHttpStateMachine

    private lateinit var request: HttpRequest

    private lateinit var requestCaptor: KArgumentCaptor<HttpRequest>

    @Mock
    private lateinit var onSuccess: OnSuccess<*>

    private lateinit var url: URL

    private lateinit var instance: Step3

    @Before
    @Throws(MalformedURLException::class)
    fun setUp()
    {
        url = one(validUrls())

        request = HttpRequest.Builder.newInstance()
                .usingUrl(url)
                .build()

        instance = Step3Impl(stateMachine, request)

        verifyZeroInteractions(stateMachine)

        requestCaptor = argumentCaptor()
    }


    @Test
    fun testUsingHeader()
    {
        //Edge Cases
        assertThrows { instance.usingHeader("", "") }
                .isInstanceOf(IllegalArgumentException::class.java)

        //Happy cases
        val expectedHeaders = CollectionGenerators.mapOf(alphabeticStrings(),
                                                         hexadecimalString(10),
                                                         20)

        for ((key, value) in expectedHeaders)
        {
            instance = instance.usingHeader(key, value)
        }

        instance.at(url)

        verify(stateMachine).executeSync(requestCaptor.capture())

        val requestMade = requestCaptor.firstValue
        assertThat(requestMade, notNullValue())
        assertThat(requestMade, not(sameInstance(request)))
        assertThat(requestMade.requestHeaders, equalTo((expectedHeaders)))

        //Adding an empty value should be ok too
        val key = one(alphabeticStrings())
        instance.usingHeader(key, "")
    }

    @Test
    fun testUsingQueryParam()
    {
        val amount = one(integers(5, 20))

        val strings = CollectionGenerators.mapOf(alphabeticStrings(),
                                                 hexadecimalString(10),
                                                 amount)

        val integers = CollectionGenerators.mapOf(alphabeticStrings(),
                                                  smallPositiveIntegers(),
                                                  amount)

        val booleans = CollectionGenerators.mapOf(alphabeticStrings(),
                                                  booleans(),
                                                  amount)

        for ((key, value) in strings)
        {
            instance = instance.usingQueryParam(key, value)
        }

        for ((key, value) in integers)
        {
            instance = instance.usingQueryParam(key, value)
        }

        for ((key, value) in booleans)
        {
            instance = instance.usingQueryParam(key, value)
        }

        val expected = Maps.mutableCopyOf(strings)
        //Put the integers
        integers.forEach { k, v -> expected.put(k, v.toString()) }
        //Put the booleans too
        booleans.forEach { k, v -> expected.put(k, v.toString()) }

        instance.at(url)

        verify(stateMachine).executeSync(requestCaptor.capture())
        val requestMade = requestCaptor.firstValue
        assertThat(requestMade, notNullValue())
        assertThat(requestMade.queryParams, equalTo(expected))
        assertThat(requestMade, not(sameInstance(request)))
    }

    @DontRepeat
    @Test
    fun testUsingQueryParamEdgeCases()
    {
        //Edge cases
        assertThrows { instance.usingQueryParam("", "") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testFollowRedirects()
    {
        assertThrows { instance.followRedirects(-10) }
                .isInstanceOf(IllegalArgumentException::class.java)

        instance = instance.followRedirects()
        assertThat(instance, notNullValue())

        instance = instance.followRedirects(30)
        assertThat(instance, notNullValue())

    }

    @Test
    fun testAt()
    {
        //Edge Cases
        assertThrows { instance.at("") }
                .isInstanceOf(IllegalArgumentException::class.java)

        instance.at(url)
        verify(stateMachine).executeSync(requestCaptor.capture())

        val requestMade = requestCaptor.firstValue
        assertThat(requestMade, notNullValue())
        assertThat(requestMade.url, equalTo(url))
        assertThat(requestMade, not(sameInstance(request)))
    }

    @Test
    fun testOnSuccess()
    {

        instance.onSuccess(onSuccess as OnSuccess<HttpResponse>)

        verify(stateMachine).jumpToStep5(request, HttpResponse::class.java, onSuccess as OnSuccess<HttpResponse>)
    }

    @Test
    fun testExpecting()
    {
        //Sad Cases
        assertThrows { instance.expecting(Void::class.java) }
                .isInstanceOf(IllegalArgumentException::class.java)

        //Happy cases
        val expectedClass = String::class.java
        instance.expecting(expectedClass)
        verify(stateMachine).jumpToStep4(request, expectedClass)
    }

}
