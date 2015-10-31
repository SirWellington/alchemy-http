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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.http.VerbAssertions.assertDeleteRequestMade;
import static tech.sirwellington.alchemy.http.VerbAssertions.assertGetRequestMade;
import static tech.sirwellington.alchemy.http.VerbAssertions.assertPostRequestMade;
import static tech.sirwellington.alchemy.http.VerbAssertions.assertPutRequestMade;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class Step1ImplTest
{
    
    @Mock(answer = RETURNS_SMART_NULLS)
    private AlchemyHttpStateMachine stateMachine;
    
    private HttpRequest request;
    
    @Captor
    ArgumentCaptor<HttpRequest> requestCaptor;
    
    private Step1Impl instance;
    
    @Before
    public void setUp()
    {
        request = HttpRequest.Builder
                .newInstance()
                .build();
        
        instance = new Step1Impl(stateMachine, request);
    }
    
    @Test
    public void testGet() throws Exception
    {
        System.out.println("testGet");
        
        instance.get();
        
        verify(stateMachine).jumpToStep3(requestCaptor.capture());
        
        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertGetRequestMade(requestMade.getVerb());
    }
    
    @Test
    public void testPost() throws Exception
    {
        System.out.println("testPost");
        
        instance.post();
        
        verify(stateMachine).jumpToStep2(requestCaptor.capture());
        
        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertPostRequestMade(requestMade.getVerb());
    }
    
    @Test
    public void testPut() throws Exception
    {
        System.out.println("testPut");
        
        instance.put();
        
        verify(stateMachine).jumpToStep2(requestCaptor.capture());
        
        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertPutRequestMade(requestMade.getVerb());
    }
    
    @Test
    public void testDelete() throws Exception
    {
        System.out.println("testDelete");
        
        instance.delete();
        
        verify(stateMachine).jumpToStep2(requestCaptor.capture());
        
        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertDeleteRequestMade(requestMade.getVerb());
    }
    
    @Test
    public void testToString()
    {
        System.out.println("testToString");
        
        String toString = instance.toString();
        assertThat(toString, containsString(request.toString()));
        assertThat(toString, containsString(stateMachine.toString()));
        
    }
    
}
