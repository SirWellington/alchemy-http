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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.RepeatedTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
public class AlchemyHttpTest
{

    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Mock
    private AlchemyRequestSteps.Step1 step1;

    @Mock
    private Executor executor;

    private Map<String, String> defaultHeaders;

    private String headerKey;

    private String headerValue;

    private AlchemyHttpImpl instance;

    @BeforeEach
    public void setUp()
    {
        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(),
                                                     alphabeticStrings(),
                                                     20);

        instance = new AlchemyHttpImpl(defaultHeaders, stateMachine);
    }

    @Test
    public void testUsingDefaultHeader()
    {
        var result = instance.usingDefaultHeader(headerKey, headerValue);
        assertThat(result, notNullValue());
        assertThat(result.getDefaultHeaders().containsKey(headerKey), is(true));
        assertThat(result.getDefaultHeaders().get(headerKey), equalTo(headerValue));
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
        var result = instance.getDefaultHeaders();
        assertThat(result, equalTo(defaultHeaders));
    }

    @Test
    public void testGo()
    {
        when(stateMachine.begin(any())).thenReturn(step1);

        var step = instance.go();

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
        var result = AlchemyHttp.newDefaultInstance();
        assertThat(result, notNullValue());
    }

    @Test
    public void testNewInstance()
    {
        var result = AlchemyHttp.newInstance(executor, defaultHeaders);
        assertThat(result, notNullValue());

        // Edge cases
        assertThrows(() -> AlchemyHttp.newInstance(executor, defaultHeaders, -1, TimeUnit.SECONDS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNewBuilder()
    {
        var result = AlchemyHttp.newBuilder();
        assertThat(result, notNullValue());

        var client = result
                .usingExecutor(executor)
                .usingDefaultHeaders(defaultHeaders)
                .build();

        assertThat(client, notNullValue());

        for (var entry : defaultHeaders.entrySet())
        {
            assertThat(entry.getValue().equals(client.getDefaultHeaders().get(entry.getKey())), is(true));
        }
    }
}
