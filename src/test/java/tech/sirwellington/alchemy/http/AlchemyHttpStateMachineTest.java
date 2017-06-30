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

import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyHttpStateMachine.Builder;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class AlchemyHttpStateMachineTest
{
    
    private AlchemyHttpStateMachine instance;
    
    @Mock
    private AlchemyRequest.Step1 step1;
    
    @Mock
    private AlchemyRequest.Step2 step2;
    
    @Mock
    private AlchemyRequest.Step3 step3;
    
    @Mock
    private AlchemyRequest.Step4 step4;
    
    @Mock
    private AlchemyRequest.Step5 step5;
    
    @Mock
    private AlchemyRequest.Step6 step6;
    
    private Map<String, String> requestHeaders;
    
    @Mock
    private HttpRequest request;
    
    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;
    
    @Mock
    private HttpClient apacheClient;
    
    @Mock
    private ExecutorService executorService;
    
    @Before
    public void setUp()
    {
        requestHeaders = mapOf(alphabeticString(), alphabeticString(), 15);
        
        instance = new TestImpl();
        instance = spy(instance);
    }
    
    @Test
    public void testBegin()
    {
        AlchemyRequest.Step1 result = instance.begin();
        assertThat(result, notNullValue());
        assertThat(result, is(step1));
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
        Builder builder = Builder.newInstance();
        assertThat(builder, notNullValue());
        
        AlchemyHttpStateMachine result = Builder.newInstance()
                .usingApacheHttpClient(apacheClient)
                .usingExecutorService(executorService)
                .build();
        
        assertThat(result, notNullValue());
        
    }
    
    @DontRepeat
    @Test
    public void testUsingGson() throws Exception
    {
        Gson gson = new Gson();
        
        AlchemyHttpStateMachine result = Builder.newInstance()
            .usingApacheHttpClient(apacheClient)
            .usingGson(gson)
            .build();
        
        assertThat(result, notNullValue());
    }
    
    @Test
    public void testBuilderWithEdgeCases()
    {
        Builder builder = Builder.newInstance();
        
        assertThrows(() -> builder.usingApacheHttpClient(null))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> builder.usingExecutorService(null))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> Builder.newInstance().build())
                .isInstanceOf(IllegalStateException.class);
        
    }
    
    class TestImpl implements AlchemyHttpStateMachine
    {
        
        @Override
        public AlchemyRequest.Step1 begin(HttpRequest initialRequest)
        {
            return step1;
        }
        
        @Override
        public AlchemyRequest.Step2 jumpToStep2(HttpRequest request) throws IllegalArgumentException
        {
            return step2;
        }
        
        @Override
        public AlchemyRequest.Step3 jumpToStep3(HttpRequest request) throws IllegalArgumentException
        {
            return step3;
        }
        
        @Override
        public <ResponseType> AlchemyRequest.Step4<ResponseType> jumpToStep4(HttpRequest request, Class<ResponseType> classOfResponseType) throws IllegalArgumentException
        {
            return step4;
        }
        
        @Override
        public <ResponseType> AlchemyRequest.Step5<ResponseType> jumpToStep5(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequest.OnSuccess<ResponseType> successCallback) throws IllegalArgumentException
        {
            return step5;
        }
        
        @Override
        public <ResponseType> AlchemyRequest.Step6<ResponseType> jumpToStep6(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequest.OnSuccess<ResponseType> successCallback, AlchemyRequest.OnFailure failureCallback)
        {
            return step6;
        }
        
        @Override
        public <ResponseType> ResponseType executeSync(HttpRequest request, Class<ResponseType> classOfResponseType) throws AlchemyHttpException
        {
            return null;
        }
        
        @Override
        public <ResponseType> void executeAsync(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequest.OnSuccess<ResponseType> successCallback, AlchemyRequest.OnFailure failureCallback)
        {
        }
    }
    
}
