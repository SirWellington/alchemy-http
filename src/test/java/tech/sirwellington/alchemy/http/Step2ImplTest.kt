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

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.argumentCaptor
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.http.Generators.jsonObjects
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.Repeat

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat(50)
class Step2ImplTest
{

    @Mock
    private lateinit var stateMachine: AlchemyHttpStateMachine

    private lateinit var request: HttpRequest

    private lateinit var requestCaptor: KArgumentCaptor<HttpRequest>

    private lateinit var expectedBody: JsonElement

    private var gson = Constants.DEFAULT_GSON

    private lateinit var instance: Step2Impl

    @Before
    fun setUp()
    {

        request = HttpRequest.Builder.newInstance().build()

        instance = Step2Impl(request, stateMachine, gson)

        expectedBody = one(jsonObjects())

        requestCaptor = argumentCaptor()
    }

    @DontRepeat
    @Test
    fun testNoBody()
    {
        instance.noBody()

        verify(stateMachine).jumpToStep3(requestCaptor.capture())

        expectedBody = JsonNull.INSTANCE
        val requestMade = requestCaptor.firstValue
        verifyRequestMade(requestMade)
    }
    @DontRepeat
    @Test
    fun testNothing()
    {
        instance.nothing()

        verify(stateMachine).jumpToStep3(requestCaptor.capture())

        expectedBody = JsonNull.INSTANCE
        val requestMade = requestCaptor.firstValue
        verifyRequestMade(requestMade)
    }

    @Test
    fun testStringBody()
    {
        val stringBody = gson.toJson(expectedBody)
        instance.body(stringBody)

        verify(stateMachine).jumpToStep3(requestCaptor.capture())

        val requestMade = requestCaptor.firstValue
        verifyRequestMade(requestMade)
    }

    @Test
    fun testStringBodyWhenEmpty()
    {
        assertThrows { instance.body("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testObjectBody()
    {

        val pojo = TestPojo.generate()

        instance.body(pojo)

        verify(stateMachine).jumpToStep3(requestCaptor.capture())

        expectedBody = gson.toJsonTree(pojo)
        val requestMade = requestCaptor.firstValue
        verifyRequestMade(requestMade)
    }

    private fun verifyRequestMade(requestMade: HttpRequest)
    {
        assertThat(requestMade, notNullValue())
        assertThat(requestMade.body, equalTo(expectedBody))
    }

    @Test
    fun testToString()
    {
        val toString = instance.toString()
        assertThat(Strings.isNullOrEmpty(toString), equalTo(false))
    }
}
