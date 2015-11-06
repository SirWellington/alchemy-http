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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.asString;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.http.AlchemyHttpBuilder.newInstance;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class AlchemyHttpBuilderTest
{

    @Mock
    private ExecutorService executor;

    @Mock
    private HttpClient apacheHttpClient;

    private Map<String, String> defaultHeaders;

    private AlchemyHttpBuilder instance;

    @Before
    public void setUp()
    {
        defaultHeaders = mapOf(alphabeticString(), alphabeticString(), 20);

        instance = new AlchemyHttpBuilder()
                .usingApacheHttpClient(apacheHttpClient)
                .usingExecutorService(executor)
                .usingDefaultHeaders(defaultHeaders);
    }

    @Test
    public void testNewInstance()
    {
        instance = AlchemyHttpBuilder.newInstance();
        assertThat(instance, notNullValue());
    }

    @Test
    public void testUsingApacheHttpClient()
    {
        AlchemyHttpBuilder result = instance.usingApacheHttpClient(apacheHttpClient);
        assertThat(result, notNullValue());

        //Edge cases
        assertThrows(() -> instance.usingApacheHttpClient(null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Repeat(100)
    @Test
    public void testUsingExecutorService()
    {
        AlchemyHttpBuilder result = instance.usingExecutorService(executor);
        assertThat(result, notNullValue());

        //Edge cases
        assertThrows(() -> instance.usingExecutorService(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDisableAsyncCallbacks()
    {
        AlchemyHttpBuilder result = instance.disableAsyncCallbacks();
        assertThat(result, notNullValue());
    }

    @Test
    public void testEnableAsyncCallbacks()
    {
        AlchemyHttpBuilder result = instance.enableAsyncCallbacks();
        assertThat(result, notNullValue());
    }

    @Repeat(100)
    @Test
    public void testUsingDefaultHeaders()
    {
        Map<String, String> headers = mapOf(alphabeticString(),                                               asString(smallPositiveIntegers()),
                                               100);

        AlchemyHttpBuilder result = instance.usingDefaultHeaders(headers);
        assertThat(result, notNullValue());

        AlchemyHttp http = result.build();
        assertThat(http, notNullValue());
        assertThat(http.getDefaultHeaders(), is(headers));

        //Edge cases
        assertThrows(() -> instance.usingDefaultHeaders(null))
                .isInstanceOf(IllegalArgumentException.class);

        //Empty headers is ok
        instance.usingDefaultHeaders(Collections.emptyMap());
    }

    @Repeat
    @Test
    public void testUsingDefaultHeader()
    {
        String key = one(alphabeticString());
        String value = one(hexadecimalString(10));

        AlchemyHttpBuilder result = instance.usingDefaultHeader(key, value);
        assertThat(result, notNullValue());

        AlchemyHttp http = result.build();
        assertThat(http.getDefaultHeaders(), Matchers.hasEntry(key, value));
    }

    @Test
    public void testUsingDefaultHeaderEdgeCases()
    {
        assertThrows(() -> instance.usingDefaultHeader(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingDefaultHeader("", null))
                .isInstanceOf(IllegalArgumentException.class);

        String key = one(alphabeticString());
        //should be ok
        instance.usingDefaultHeader(key, "");
    }

    @Repeat(100)
    @Test
    public void testBuild()
    {

        AlchemyHttp result = instance.build();
        assertThat(result, notNullValue());
        assertThat(result.getDefaultHeaders(), is(defaultHeaders));
    }

    @Test
    public void testBuildEdgeCases()
    {
        //Missing everything
        instance = newInstance();
        assertThrows(() -> instance.build())
                .isInstanceOf(IllegalStateException.class);

        //No Executor Service set
        instance = newInstance().usingApacheHttpClient(apacheHttpClient);
        instance.build();

        //Missing Apache Client
        instance = newInstance().usingExecutorService(executor);
        assertThrows(() -> instance.build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testDefaultIncludesBasicRequestHeaders()
    {
        instance = newInstance()
                .usingApacheHttpClient(apacheHttpClient)
                .usingExecutorService(executor);

        AlchemyHttp result = instance.build();
        assertThat(result, notNullValue());
        Map<String, String> headers = result.getDefaultHeaders();
        assertThat(headers, Matchers.hasKey("Accept"));
        assertThat(headers, Matchers.hasKey("Content-Type"));

    }

}
