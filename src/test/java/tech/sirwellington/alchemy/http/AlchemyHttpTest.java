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

import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

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
    private HttpClient apacheClient;

    @Mock
    private ExecutorService executorService;

    private Map<String, String> defaultHeaders;

    private AlchemyHttp instance;

    @Before
    public void setUp()
    {
        defaultHeaders = mapOf(alphabeticString(),
                               alphabeticString(),
                               20);

        instance = new AlchemyHttpImpl(defaultHeaders, stateMachine);
    }

    @Repeat(100)
    @Test
    public void testUsingDefaultHeader()
    {
    }

    @Test
    public void testGetDefaultHeaders()
    {
    }

    @Test
    public void testGo()
    {
    }

    @Test
    public void testNewDefaultInstance()
    {
        AlchemyHttp result = AlchemyHttp.newDefaultInstance();
        assertThat(result, notNullValue());
    }

    @Test
    public void testNewInstanceWithApacheHttpClient()
    {
        AlchemyHttp result = AlchemyHttp.newInstanceWithApacheHttpClient(apacheClient);
        assertThat(result, notNullValue());

        //Edge cases
        assertThrows(() ->
        {
            AlchemyHttp.newInstanceWithApacheHttpClient(null);
        }).isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testNewInstance()
    {

        AlchemyHttp result = AlchemyHttp.newInstance(apacheClient, executorService, defaultHeaders);
        assertThat(result, notNullValue());

        //Edge cases
        assertThrows(() ->
        {
            AlchemyHttp.newInstance(null, null, null);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThrows(() ->
        {
            AlchemyHttp.newInstance(apacheClient, null, null);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThrows(() ->
        {
            AlchemyHttp.newInstance(apacheClient, executorService, null);
        }).isInstanceOf(IllegalArgumentException.class);

        assertThrows(() ->
        {
            AlchemyHttp.newInstance(apacheClient, null, defaultHeaders);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNewBuilder()
    {
        AlchemyHttpBuilder result = AlchemyHttp.newBuilder();
        assertThat(result, notNullValue());

        AlchemyHttp client = result.usingApacheHttpClient(apacheClient)
                .usingExecutorService(executorService)
                .usingDefaultHeaders(defaultHeaders)
                .build();
        assertThat(client, notNullValue());
    }

}
