/*
 * Copyright © 2026. Sir Wellington.
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
package tech.sirwellington.alchemy.http;

import java.util.concurrent.Executor;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnFailure;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class AlchemyMachineImplTest
{
    @Mock
    private Executor executor;

    @Captor
    private ArgumentCaptor<Runnable> taskCaptor;

    private Gson gson = Constants.DEFAULT_GSON;

    @Mock
    private HttpRequest mockRequest;

    private TestRequest request;

    @Mock
    private OnSuccess<TestPojo> onSuccess;

    @Mock
    private OnFailure onFailure;

    @Mock
    private HttpRequestExecutor requestExecutor;

    @Mock
    private HttpResponse response;

    private TestPojo pojo;
    private Class<TestPojo> responseClass = TestPojo.class;

    private AlchemyHttpStateMachine instance;

    @Before
    public void setUp() throws Exception
    {
        request = new TestRequest();

        instance = new AlchemyMachineImpl(executor, gson, requestExecutor);
        verifyNoInteractions(executor, requestExecutor);

        setupExecutor();
        setupResponse();
    }

    private void setupExecutor()
    {
        when(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenReturn(response);
    }

    private void setupResponse()
    {
        pojo = TestPojo.generate();

        when(response.isOk()).thenReturn(true);
        when(response.bodyAs(responseClass)).thenReturn(pojo);
    }

    @Test
    public void testBegin()
    {
        var step1 = instance.begin(mockRequest);
        assertThat(step1, notNullValue());
    }

    @Test
    public void testJumpToStep2()
    {
        var step2 = instance.jumpToStep2(mockRequest);
        assertThat(step2, notNullValue());
    }

    @Test
    public void testJumpToStep3()
    {
        var step3 = instance.jumpToStep3(mockRequest);
        assertThat(step3, notNullValue());
    }

    @Test
    public void testJumpToStep4()
    {
        var step4 = instance.jumpToStep4(mockRequest, responseClass);
        assertThat(step4, notNullValue());

        assertThrows(() -> instance.jumpToStep4(mockRequest, Void.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep5()
    {
        var step5 = instance.jumpToStep5(mockRequest, responseClass, onSuccess);
        assertThat(step5, notNullValue());

        // Edge cases
        @SuppressWarnings("unchecked")
        OnSuccess<Void> mockOnSuccess = mock(OnSuccess.class);
        assertThrows(() -> instance.jumpToStep5(mockRequest, Void.class, mockOnSuccess))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep6()
    {
        var step6 = instance.jumpToStep6(mockRequest, responseClass, onSuccess, onFailure);
        assertThat(step6, notNullValue());

        // Edge cases
        @SuppressWarnings("unchecked")
        OnSuccess<Void> mockOnSuccess = mock(OnSuccess.class);
        assertThrows(() -> instance.jumpToStep6(mockRequest, Void.class, mockOnSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @RepeatedTest(200)
    @Test
    public void testExecuteSync()
    {
        var result = instance.executeSync(request);
        assertThat(result, equalTo(response));
    }

    @RepeatedTest(200)
    @Test
    public void testExecuteSyncWithCustomClass()
    {
        when(response.bodyAs(responseClass)).thenReturn(pojo);

        var result = instance.executeSync(request, responseClass);
        assertThat(result, equalTo(pojo));
    }

    @Test
    public void testExecuteSyncWhenHttpExecutorFails()
    {
        when(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenThrow(RuntimeException.class);

        assertThrows(() -> instance.executeSync(request))
                .isInstanceOf(AlchemyHttpException.class);

        // Reset and do another assertion
        reset(requestExecutor);

        when(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenThrow(new AlchemyHttpException(request));

        assertThrows(() -> instance.executeSync(request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testExecuteSyncWithBadArguments()
    {
        assertThrows(() -> instance.executeSync(mockRequest, Void.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeSync(request, Void.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecuteWhenHttpExecutorReturnsNullResponse()
    {
        when(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenReturn(null);

        assertThrows(() -> instance.executeSync(request, responseClass))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecuteSyncWhenClassOfResponseTypeIsString()
    {
        instance.executeSync(request, String.class);
        verify(response).bodyAsString();
    }

    @RepeatedTest(200)
    @Test
    public void testExecuteWhenResponseNotOk()
    {
        when(response.isOk()).thenReturn(false);

        assertThrows(() -> instance.executeSync(request, responseClass))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testExecuteWhenCastingToResponseClassFails()
    {
        when(response.bodyAs(responseClass))
                .thenThrow(new JsonException());

        assertThrows(() -> instance.executeSync(request, responseClass))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @RepeatedTest(200)
    @Test
    public void testExecuteAsync() throws Exception
    {
        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executor).execute(taskCaptor.capture());

        var task = taskCaptor.getValue();
        assertThat(task, notNullValue());

        task.run();
        verify(onSuccess).processResponse(pojo);
    }

    @Test
    public void testExecuteAsyncWhenFails()
    {
        AlchemyHttpException ex = new AlchemyHttpException();

        when(requestExecutor.execute(request, gson, Constants.DEFAULT_TIMEOUT))
                .thenThrow(ex);

        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executor).execute(taskCaptor.capture());

        var task = taskCaptor.getValue();
        assertThat(task, notNullValue());

        task.run();
        verify(onFailure).handleError(ex);
    }

    @Test
    public void testExecuteAsyncWhenRuntimeExceptionHappens()
    {
        when(requestExecutor.execute(eq(request), eq(gson), anyLong()))
                .thenThrow(RuntimeException.class);

        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executor).execute(taskCaptor.capture());

        var task = taskCaptor.getValue();
        assertThat(task, notNullValue());

        task.run();
        verify(onFailure).handleError(any());
    }

    @Test
    public void testExecuteAsyncWhenOnSuccessFails()
    {
        doThrow(RuntimeException.class)
                .when(onSuccess)
                .processResponse(pojo);

        instance.executeAsync(request, responseClass, onSuccess, onFailure);

        verify(executor).execute(taskCaptor.capture());

        var task = taskCaptor.getValue();
        assertThat(task, notNullValue());
        task.run();
        verify(onFailure).handleError(any());
    }

    @Test
    public void testExecuteAsyncWithBadArgs()
    {
        @SuppressWarnings("unchecked")
        var mockOnSuccess = mock(OnSuccess.class);

        assertThrows(() -> instance.executeAsync(mockRequest, Void.class, mockOnSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(toString, not(isEmptyOrNullString()));
    }
}
