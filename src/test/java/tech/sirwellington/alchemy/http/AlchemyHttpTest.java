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

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class AlchemyHttpTest
{

    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Mock
    private AlchemyRequestSteps.Step1 step1;

    @Mock
    private Executor executor;

    private Map<String, String> defaultHeaders;

    @GenerateString
    private String headerKey;

    @GenerateString
    private String headerValue;

    private AlchemyHttpImpl instance;

    @Before
    public void setUp()
    {
        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(),
                                                     alphabeticStrings(),
                                                     20);

        instance = new AlchemyHttpImpl(defaultHeaders, stateMachine);
    }

    @Repeat(100)
    @Test
    public void testUsingDefaultHeader()
    {
        AlchemyHttp result = instance.usingDefaultHeader(headerKey, headerValue);
        assertThat(result, notNullValue());
        assertTrue(result.getDefaultHeaders().containsKey(headerKey));
        assertEquals(result.getDefaultHeaders().get(headerKey), headerValue);
    }

    @Test
    public void testUsingDefaultHeaderWithEmptyKey() throws Exception
    {
        assertThrows(() -> instance.usingDefaultHeader("", headerValue))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetDefaultHeaders()
    {
        Map<String, String> result = instance.getDefaultHeaders();
        assertThat(result, equalTo(defaultHeaders));
    }

    @Test
    public void testGo()
    {
        when(stateMachine.begin(any())).thenReturn(step1);

        AlchemyRequestSteps.Step1 step = instance.go();

        assertThat(step, equalTo(step1));

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);

        verify(stateMachine).begin(captor.capture());

        HttpRequest request = captor.getValue();
        assertThat(request, notNullValue());
        assertThat(request.method(), notNullValue());
        assertThat(request.requestHeaders(), notNullValue());
    }

    @Test
    public void testNewDefaultInstance()
    {
        AlchemyHttp result = AlchemyHttp.newDefaultInstance();
        assertThat(result, notNullValue());
    }

    @Test
    public void testNewInstance()
    {
        AlchemyHttp result = AlchemyHttp.newInstance(executor, defaultHeaders);
        assertThat(result, notNullValue());

        // Edge cases
        assertThrows(() -> AlchemyHttp.newInstance(executor, defaultHeaders, -1, TimeUnit.SECONDS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNewBuilder()
    {
        AlchemyHttpBuilder result = AlchemyHttp.newBuilder();
        assertThat(result, notNullValue());

        AlchemyHttp client = result
                .usingExecutor(executor)
                .usingDefaultHeaders(defaultHeaders)
                .build();

        assertThat(client, notNullValue());

        for (Map.Entry<String, String> entry : defaultHeaders.entrySet())
        {
            assertTrue(entry.getValue().equals(client.getDefaultHeaders().get(entry.getKey())));
        }
    }
}
