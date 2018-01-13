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
package tech.sirwellington.alchemy.http;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class AlchemyHttpTest
{

    @Mock
    private AlchemyHttpStateMachine stateMachine;

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
        defaultHeaders = mapOf(alphabeticStrings(),
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
        AlchemyRequest.Step1 step = instance.go();
        assertThat(step, notNullValue());
    }

    @Test
    public void testNewDefaultInstance()
    {
        AlchemyHttp result = AlchemyHttp.Companion.newDefaultInstance();
        assertThat(result, notNullValue());
    }

    @Test
    public void testNewInstance()
    {

        AlchemyHttp result = AlchemyHttp.Companion.newInstance(executor, defaultHeaders);
        assertThat(result, notNullValue());

        //Edge cases
        assertThrows(() ->
        {
            AlchemyHttp.Companion.newInstance(null, null, 0, null);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThrows(() ->
        {
            AlchemyHttp.Companion.newInstance(executor, null, 0, null);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThrows(() ->
        {
            AlchemyHttp.Companion.newInstance(executor, defaultHeaders, -1, TimeUnit.SECONDS);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThrows(() ->
        {
            AlchemyHttp.Companion.newInstance(executor, defaultHeaders, 1, null);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNewBuilder()
    {
        AlchemyHttpBuilder result = AlchemyHttp.Companion.newBuilder();
        assertThat(result, notNullValue());

        AlchemyHttp client = result
                                .usingExecutor(executor)
                                .usingDefaultHeaders(defaultHeaders)
                                .build();

        assertThat(client, notNullValue());
        assertThat(client.getDefaultHeaders(), equalTo(defaultHeaders));
    }

}
