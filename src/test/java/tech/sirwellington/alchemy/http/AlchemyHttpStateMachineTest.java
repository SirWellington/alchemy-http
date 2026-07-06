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
package tech.sirwellington.alchemy.http;

import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class AlchemyHttpStateMachineTest
{
    private AlchemyHttpStateMachine instance;

    @Mock
    private AlchemyRequestSteps.Step1 step1;

    @Mock
    private AlchemyRequestSteps.Step2 step2;

    @Mock
    private AlchemyRequestSteps.Step3 step3;

    @Mock
    private AlchemyRequestSteps.Step4<?> step4;

    @Mock
    private AlchemyRequestSteps.Step5<?> step5;

    @Mock
    private AlchemyRequestSteps.Step6<?> step6;

    @Mock
    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    @Mock
    private Executor executor;

    private Map<String, String> requestHeaders;

    @Before
    public void setUp()
    {
        requestHeaders = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 15);
        instance = new TestImpl();
        instance = spy(instance);
    }

    @Test
    public void testBegin()
    {
        AlchemyRequestSteps.Step1 result = instance.begin();
        assertThat(result, notNullValue());
        assertThat(result, equalTo(step1));
    }

    @Test
    public void testExecuteSyncCallsImplementation()
    {
        instance.executeSync(request);
        verify(instance).executeSync(request, HttpResponse.class);
    }

    @Test
    public void testBuilder()
    {
        AlchemyHttpStateMachine.Builder builder = AlchemyHttpStateMachine.Builder.newInstance();
        assertThat(builder, notNullValue());

        AlchemyHttpStateMachine result = AlchemyHttpStateMachine.Builder.newInstance()
                .usingExecutorService(executor)
                .build();

        assertThat(result, notNullValue());
    }

    @DontRepeat
    @Test
    public void testUsingGson() throws Exception
    {
        Gson gson = new Gson();

        AlchemyHttpStateMachine result = AlchemyHttpStateMachine.Builder.newInstance()
                .usingGson(gson)
                .build();

        assertThat(result, notNullValue());
    }

    @Test
    public void testBuilderWithEdgeCases()
    {
        AlchemyHttpStateMachine result = AlchemyHttpStateMachine.Builder.newInstance().build();
        assertThat(result, notNullValue());
    }

    class TestImpl implements AlchemyHttpStateMachine
    {
        @Override
        public AlchemyRequestSteps.Step1 begin(HttpRequest initialRequest)
        {
            return step1;
        }

        @Override
        public AlchemyRequestSteps.Step2 jumpToStep2(HttpRequest request)
        {
            return step2;
        }

        @Override
        public AlchemyRequestSteps.Step3 jumpToStep3(HttpRequest request)
        {
            return step3;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <ResponseType> AlchemyRequestSteps.Step4<ResponseType> jumpToStep4(HttpRequest request, Class<ResponseType> classOfResponseType)
        {
            return (AlchemyRequestSteps.Step4<ResponseType>) step4;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <ResponseType> AlchemyRequestSteps.Step5<ResponseType> jumpToStep5(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequestSteps.OnSuccess<ResponseType> successCallback)
        {
            return (AlchemyRequestSteps.Step5<ResponseType>) step5;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <ResponseType> AlchemyRequestSteps.Step6<ResponseType> jumpToStep6(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequestSteps.OnSuccess<ResponseType> successCallback, AlchemyRequestSteps.OnFailure failureCallback)
        {
            return (AlchemyRequestSteps.Step6<ResponseType>) step6;
        }

        @Override
        public <ResponseType> ResponseType executeSync(HttpRequest request, Class<ResponseType> classOfResponseType) throws AlchemyHttpException
        {
            return Mockito.mock(classOfResponseType);
        }

        @Override
        public <ResponseType> void executeAsync(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequestSteps.OnSuccess<ResponseType> successCallback, AlchemyRequestSteps.OnFailure failureCallback)
        {
        }
    }
}
