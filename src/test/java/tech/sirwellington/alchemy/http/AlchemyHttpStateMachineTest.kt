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
import com.natpryce.hamkrest.assertion.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.http.AlchemyHttpStateMachine.Builder
import tech.sirwellington.alchemy.http.AlchemyRequest.Step4
import tech.sirwellington.alchemy.http.AlchemyRequest.Step5
import tech.sirwellington.alchemy.http.AlchemyRequest.Step6
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import java.util.concurrent.Executor

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class AlchemyHttpStateMachineTest
{

    private lateinit var instance: AlchemyHttpStateMachine

    @Mock
    private lateinit var step1: AlchemyRequest.Step1

    @Mock
    private lateinit var step2: AlchemyRequest.Step2

    @Mock
    private lateinit var step3: AlchemyRequest.Step3

    @Mock
    private lateinit var step4: AlchemyRequest.Step4<*>

    @Mock
    private lateinit var step5: AlchemyRequest.Step5<*>

    @Mock
    private lateinit var step6: AlchemyRequest.Step6<*>

    @Mock
    private lateinit var request: HttpRequest

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<HttpRequest>

    @Mock
    private lateinit var executor: Executor

    @Mock

    private var requestHeaders: Map<String, String>? = null

    @Before
    fun setUp()
    {
        requestHeaders = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 15)

        instance = TestImpl()
        instance = spy(instance)
    }

    @Test
    fun testBegin()
    {
        val result = instance.begin()
        assertThat(result, notNullValue())
        assertThat(result, equalTo(step1))
    }

    @Test
    fun testExecuteSyncCallsImplementation()
    {
        instance.executeSync(request)
        verify(instance).executeSync(request, HttpResponse::class.java)
    }

    @Test
    fun testBuilder()
    {
        val builder = Builder.newInstance()
        assertThat(builder, notNullValue())

        val result = Builder.newInstance()
                .usingExecutorService(executor)
                .build()

        assertThat(result, notNullValue())

    }

    @DontRepeat
    @Test
    @Throws(Exception::class)
    fun testUsingGson()
    {
        val gson = Gson()

        val result = Builder.newInstance()
                .usingGson(gson)
                .build()

        assertThat(result, notNullValue())
    }

    @Test
    fun testBuilderWithEdgeCases()
    {

        val result = Builder.newInstance().build();
        assertThat(result, notNull)
    }

    internal open inner class TestImpl : AlchemyHttpStateMachine
    {

        override fun begin(): AlchemyRequest.Step1
        {
            val request = HttpRequest.Builder
                    .newInstance()
                    .build()

            return begin(request)
        }

        override fun begin(initialRequest: HttpRequest): AlchemyRequest.Step1
        {
            return step1
        }

        @Throws(IllegalArgumentException::class)
        override fun jumpToStep2(request: HttpRequest): AlchemyRequest.Step2
        {
            return step2
        }

        @Throws(IllegalArgumentException::class)
        override fun jumpToStep3(request: HttpRequest): AlchemyRequest.Step3
        {
            return step3
        }

        @Throws(IllegalArgumentException::class)
        override fun <ResponseType> jumpToStep4(request: HttpRequest, classOfResponseType: Class<ResponseType>): AlchemyRequest.Step4<ResponseType>
        {
            return step4 as Step4<ResponseType>
        }

        @Throws(IllegalArgumentException::class)
        override fun <ResponseType> jumpToStep5(request: HttpRequest, classOfResponseType: Class<ResponseType>, successCallback: AlchemyRequest.OnSuccess<ResponseType>): AlchemyRequest.Step5<ResponseType>
        {
            return step5 as Step5<ResponseType>
        }

        override fun <ResponseType> jumpToStep6(request: HttpRequest, classOfResponseType: Class<ResponseType>, successCallback: AlchemyRequest.OnSuccess<ResponseType>, failureCallback: AlchemyRequest.OnFailure): AlchemyRequest.Step6<ResponseType>
        {
            return step6 as Step6<ResponseType>
        }

        @Throws(AlchemyHttpException::class)
        override fun <ResponseType> executeSync(request: HttpRequest, classOfResponseType: Class<ResponseType>): ResponseType
        {
            return Mockito.mock(classOfResponseType)
        }

        override fun <ResponseType> executeAsync(request: HttpRequest, classOfResponseType: Class<ResponseType>, successCallback: AlchemyRequest.OnSuccess<ResponseType>, failureCallback: AlchemyRequest.OnFailure)
        {
        }

    }

}
