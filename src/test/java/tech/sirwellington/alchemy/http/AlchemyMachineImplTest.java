/*
 * Copyright 2015 SirWellington Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnFailure;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class AlchemyMachineImplTest
{

    @Mock
    private HttpClient apacheClient;

    private ExecutorService executorService;

    private Gson gson;

    private AlchemyHttpStateMachine instance;

    @Mock
    private HttpRequest request;

    private TestRequest validRequest = new TestRequest();

    @Mock
    private OnSuccess<TestPojo> onSuccess;

    @Mock
    private OnFailure onFailure;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private final Class<TestPojo> responseClass = TestPojo.class;

    private TestPojo pojo;

    @Mock
    private HttpVerb verb;

    @Mock
    private HttpResponse response;

    @Before
    public void setUp()
    {
        gson = Constants.getDefaultGson();
        executorService = spy(MoreExecutors.newDirectExecutorService());

        instance = new AlchemyMachineImpl(apacheClient, executorService, gson);
        verifyZeroInteractions(apacheClient, executorService);

        setupVerb();
        setupResponseBody();
    }

    private void setupResponseBody()
    {
        pojo = TestPojo.generate();
    }

    private void setupVerb()
    {
        when(verb.execute(apacheClient, validRequest))
                .thenReturn(response);

        validRequest.verb = this.verb;

    }

    @Test
    public void testConstructor()
    {
        assertThrows(() -> new AlchemyMachineImpl(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyMachineImpl(apacheClient, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyMachineImpl(null, executorService, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyMachineImpl(null, null, gson))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBegin()
    {
        AlchemyRequest.Step1 step1 = instance.begin(request);
        assertThat(step1, notNullValue());

        //Edge cases
        assertThrows(() -> instance.begin(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep2()
    {
        AlchemyRequest.Step2 step2 = instance.jumpToStep2(request);
        assertThat(step2, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep2(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep3()
    {
        AlchemyRequest.Step3 step3 = instance.jumpToStep3(request);
        assertThat(step3, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep3(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep4()
    {
        AlchemyRequest.Step4<TestPojo> step4 = instance.jumpToStep4(request, responseClass);
        assertThat(step4, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep4(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep4(request, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep4(request, Void.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep5()
    {
        AlchemyRequest.Step5<TestPojo> step5 = instance.jumpToStep5(request, responseClass, onSuccess);
        assertThat(step5, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep5(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(request, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(request, responseClass, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(request, null, onSuccess))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(null, responseClass, onSuccess))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep5(request, Void.class, mock(OnSuccess.class)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testJumpToStep6()
    {
        AlchemyRequest.Step6<TestPojo> step6 = instance.jumpToStep6(request, responseClass, onSuccess, onFailure);
        assertThat(step6, notNullValue());

        //Edge cases
        assertThrows(() -> instance.jumpToStep6(request, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(request, responseClass, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(request, responseClass, onSuccess, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(null, responseClass, onSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(null, null, onSuccess, onFailure))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.jumpToStep6(request, Void.class, mock(OnSuccess.class), onFailure))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecuteSync()
    {
        when(response.isOk())
                .thenReturn(true);

        HttpResponse result = instance.executeSync(validRequest);

        assertThat(result, is(response));
    }

    @Test
    public void testExecuteSyncWithCustomClass()
    {
        when(response.as(responseClass)).thenReturn(pojo);
        when(response.isOk()).thenReturn(true);
        
        TestPojo result = instance.executeSync(validRequest, responseClass);
        assertThat(result, is(pojo));
    }

    @Test
    public void testExecuteSyncWhenVerbFails()
    {

        when(verb.execute(apacheClient, validRequest))
                .thenThrow(new RuntimeException());

        assertThrows(() -> instance.executeSync(validRequest))
                .isInstanceOf(AlchemyHttpException.class);

        //Reset and do another assertion
        reset(verb);

        when(verb.execute(apacheClient, validRequest))
                .thenThrow(new AlchemyHttpException(validRequest));

        assertThrows(() -> instance.executeSync(validRequest))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testExecuteSyncEdgeCases()
    {
        assertThrows(() -> instance.executeSync(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeSync(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeSync(request, Void.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.executeSync(validRequest, Void.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecuteAsync()
    {
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(toString, not(isEmptyOrNullString()));
    }

}
