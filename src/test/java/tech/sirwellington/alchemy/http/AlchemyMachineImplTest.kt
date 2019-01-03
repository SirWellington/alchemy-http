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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong

import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnFailure
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.http.exceptions.JsonException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.util.concurrent.Executor

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class AlchemyMachineImplTest
{

    @Mock
    private lateinit var executor: Executor

    @Captor
    private lateinit var taskCaptor: ArgumentCaptor<Runnable>

    private var gson = Constants.DEFAULT_GSON

    @Mock
    private lateinit var mockRequest: HttpRequest

    private lateinit var request: TestRequest

    @Mock
    private lateinit var onSuccess: OnSuccess<TestPojo>

    @Mock
    private lateinit var onFailure: OnFailure

    @Mock
    private lateinit var requestExecutor: HttpRequestExecutor

    @Mock
    private lateinit var response: HttpResponse

    private lateinit var pojo: TestPojo
    private var responseClass = TestPojo::class.java


    private lateinit var instance: AlchemyHttpStateMachine


    @Before
    @Throws(Exception::class)
    fun setUp()
    {
        request = TestRequest()

        instance = AlchemyMachineImpl(executor, gson, requestExecutor)
        verifyZeroInteractions(executor, requestExecutor)

        setupExecutor()
        setupResponse()
    }

    private fun setupExecutor()
    {
        whenever(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenReturn(response)

    }

    private fun setupResponse()
    {
        pojo = TestPojo.generate()

        whenever(response.isOk).thenReturn(true)
        whenever(response.bodyAs(responseClass)).thenReturn(pojo)
    }


    @Test
    fun testBegin()
    {
        val step1 = instance.begin(mockRequest)
        assertThat(step1, notNullValue())
    }

    @Test
    fun testJumpToStep2()
    {
        val step2 = instance.jumpToStep2(mockRequest)
        assertThat(step2, notNullValue())

    }

    @Test
    fun testJumpToStep3()
    {
        val step3 = instance.jumpToStep3(mockRequest)
        assertThat(step3, notNullValue())

    }

    @Test
    fun testJumpToStep4()
    {
        val step4 = instance.jumpToStep4(mockRequest, responseClass)
        assertThat(step4, notNullValue())

        assertThrows { instance.jumpToStep4(mockRequest, Void::class.java) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testJumpToStep5()
    {
        val step5 = instance.jumpToStep5(mockRequest, responseClass, onSuccess)
        assertThat(step5, notNullValue())

        //Edge cases

        assertThrows { instance.jumpToStep5(mockRequest, Void::class.java, mock { }) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testJumpToStep6()
    {
        val step6 = instance.jumpToStep6(mockRequest, responseClass, onSuccess, onFailure)
        assertThat(step6, notNullValue())

        //Edge cases

        assertThrows { instance.jumpToStep6(mockRequest, Void::class.java, mock { }, onFailure) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Repeat(200)
    @Test
    fun testExecuteSync()
    {

        val result = instance.executeSync(request)

        assertThat(result, equalTo(response))
    }

    @Repeat(200)
    @Test
    fun testExecuteSyncWithCustomClass()
    {
        whenever(response.bodyAs(responseClass)).thenReturn(pojo)

        val result = instance.executeSync(request, responseClass)
        assertThat(result, equalTo(pojo))
    }

    @Test
    fun testExecuteSyncWhenHttpExecutorFails()
    {

        whenever(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenThrow(RuntimeException())

        assertThrows { instance.executeSync(request) }
                .isInstanceOf(AlchemyHttpException::class.java)

        //Reset and do another assertion
        reset(requestExecutor)

        whenever(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenThrow(AlchemyHttpException(request))

        assertThrows { instance.executeSync(request) }
                .isInstanceOf(AlchemyHttpException::class.java)
    }

    @Test
    fun testExecuteSyncWithBadArguments()
    {
        assertThrows { instance.executeSync(mockRequest, Void::class.java) }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { instance.executeSync(request, Void::class.java) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testExecuteWhenHttpExecutorReturnsNullResponse()
    {
        whenever(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenReturn(null)

        assertThrows { instance.executeSync(request, responseClass) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testExecuteSyncWhenClassOfResponseTypeIsString()
    {
        instance.executeSync(request, String::class.java)
        verify(response).bodyAsString()
    }

    @Repeat(200)
    @Test
    fun testExecuteWhenResponseNotOk()
    {
        whenever(response.isOk).thenReturn(false)

        assertThrows { instance.executeSync(request, responseClass) }
                .isInstanceOf(AlchemyHttpException::class.java)
    }

    @Test
    fun testExecuteWhenCastingToResponseClassFails()
    {
        whenever(response.bodyAs(responseClass))
                .thenThrow(JsonException())

        assertThrows { instance.executeSync(request, responseClass) }
                .isInstanceOf(AlchemyHttpException::class.java)
    }

    @Repeat(200)
    @Test
    @Throws(Exception::class)
    fun testExecuteAsync()
    {

        instance.executeAsync(request, responseClass, onSuccess, onFailure)

        verify(executor).execute(taskCaptor.capture())

        val task = taskCaptor.value
        assertThat(task, notNullValue())

        task.run()
        verify(onSuccess).processResponse(pojo)
    }

    @Test
    fun testExecuteAsyncWhenFails()
    {
        val ex = AlchemyHttpException()

        whenever(requestExecutor.execute(request, gson, Constants.DEFAULT_TIMEOUT))
                .thenThrow(ex)

        instance.executeAsync(request, responseClass, onSuccess, onFailure)

        verify(executor).execute(taskCaptor.capture())

        val task = taskCaptor.value
        assertThat(task, notNullValue())

        task.run()
        verify(onFailure).handleError(ex)
    }

    @Test
    fun testExecuteAsyncWhenRuntimeExceptionHappens()
    {
        whenever(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenThrow(RuntimeException())

        instance.executeAsync(request, responseClass, onSuccess, onFailure)

        verify(executor).execute(taskCaptor.capture())

        val task = taskCaptor.value
        assertThat(task, notNullValue())

        task.run()
        verify(onFailure).handleError(any())

    }

    @Test
    fun testExecuteAsyncWhenOnSuccessFails()
    {
        doThrow(RuntimeException())
                .whenever(onSuccess)
                .processResponse(pojo)

        instance.executeAsync(request, responseClass, onSuccess, onFailure)

        verify(executor).execute(taskCaptor.capture())

        val task = taskCaptor.value
        assertThat(task, notNullValue())
        task.run()
        verify(onFailure).handleError(any())
    }

    @Test
    fun testExecuteAsyncWithBadArgs()
    {

        assertThrows { instance.executeAsync(mockRequest, Void::class.java, mock { }, onFailure) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testToString()
    {
        val toString = instance.toString()
        assertThat(toString, not(isEmptyOrNullString()))
    }

}
