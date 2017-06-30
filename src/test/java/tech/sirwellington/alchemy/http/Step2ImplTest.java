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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.http.Generators.jsonObjects;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat(50)
public class Step2ImplTest
{
    
    @Mock
    private AlchemyHttpStateMachine stateMachine;
    
    private HttpRequest request;
    
    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;
    
    private JsonElement expectedBody;
    
    private final Gson gson = Constants.getDefaultGson();
    
    private Step2Impl instance;
    
    @Before
    public void setUp()
    {
        
        request = HttpRequest.Builder.newInstance().build();
        
        instance = new Step2Impl(request, stateMachine, gson);
        
        expectedBody = one(jsonObjects());
    }
    
    @DontRepeat
    @Test
    public void testNothing()
    {
        instance.nothing();
        
        verify(stateMachine).jumpToStep3(requestCaptor.capture());
        
        expectedBody = JsonNull.INSTANCE;
        HttpRequest requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }
    
    @Test
    public void testStringBody()
    {
        String stringBody = gson.toJson(expectedBody);
        instance.body(stringBody);
        
        verify(stateMachine).jumpToStep3(requestCaptor.capture());
        
        HttpRequest requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }
    
    @Test
    public void testStringBodyWhenEmpty()
    {
        assertThrows(() -> instance.body(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testObjectBody()
    {
        
        TestPojo pojo = TestPojo.generate();
        
        instance.body(pojo);
        
        verify(stateMachine).jumpToStep3(requestCaptor.capture());
        
        expectedBody = gson.toJsonTree(pojo);
        HttpRequest requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }
    
    @Test
    public void testObjectBodyWhenNull()
    {
        assertThrows(() -> instance.body(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    private void verifyRequestMade(HttpRequest requestMade)
    {
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.getBody(), is(expectedBody));
    }
    
    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(Strings.isNullOrEmpty(toString), is(false));
    }
}
