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

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateLong;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.asString;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class AlchemyHttpBuilderTest
{

    @Mock
    private ExecutorService executor;

    private Map<String, String> defaultHeaders;

    private AlchemyHttpBuilder instance;

    @GenerateLong(min = 100)
    private long timeout;

    @Before
    public void setUp()
    {
        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 20);
        timeout = NumberGenerators.longs(100, 2000).get();

        instance = AlchemyHttpBuilder.newInstance()
                .usingTimeout(Math.toIntExact(timeout), TimeUnit.MILLISECONDS)
                .usingExecutor(executor)
                .usingDefaultHeaders(defaultHeaders);
    }

    @Test
    public void testNewInstance()
    {
        instance = AlchemyHttpBuilder.newInstance();
        assertThat(instance, notNullValue());
    }

    @Repeat(50)
    @Test
    public void testUsingTimeout()
    {
        int socketTimeout = one(integers(15, 100));
        AlchemyHttpBuilder result = instance.usingTimeout(socketTimeout, TimeUnit.SECONDS);
        assertThat(result, notNullValue());
    }

    @Repeat(10)
    @Test
    public void testUsingTimeoutWithBadArgs()
    {
        int negativeNumber = one(negativeIntegers());

        assertThrows(() -> instance.usingTimeout(negativeNumber, TimeUnit.SECONDS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testUsingGson()
    {
        Gson gson = new Gson();
        AlchemyHttpBuilder result = instance.usingGson(gson);
        assertThat(result, notNullValue());
    }

    @Repeat(100)
    @Test
    public void testUsingExecutorService()
    {
        AlchemyHttpBuilder result = instance.usingExecutor(executor);
        assertThat(result, notNullValue());
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
        instance = AlchemyHttpBuilder.newInstance();

        Map<String, String> headers = CollectionGenerators.mapOf(alphabeticStrings(),
                                                                  asString(smallPositiveIntegers()),
                                                                  100);

        AlchemyHttpBuilder result = instance.usingDefaultHeaders(headers);
        assertThat(result, notNullValue());

        AlchemyHttp http = result.build();
        assertThat(http, notNullValue());

        Map<String, String> expected = new HashMap<>(Constants.DEFAULT_HEADERS);
        expected.putAll(headers);
        assertThat(http.getDefaultHeaders(), equalTo(expected));

        // Empty headers is ok
        instance.usingDefaultHeaders(Collections.emptyMap());
    }

    @Repeat
    @Test
    public void testUsingDefaultHeader()
    {
        String key = one(alphabeticStrings());
        String value = one(hexadecimalString(10));

        AlchemyHttpBuilder result = instance.usingDefaultHeader(key, value);
        assertThat(result, notNullValue());

        AlchemyHttp http = result.build();
        assertThat(http.getDefaultHeaders(), hasEntry(key, value));
    }

    @Test
    public void testUsingDefaultHeaderEdgeCases()
    {
        String key = one(alphabeticStrings());
        // should be ok
        instance.usingDefaultHeader(key, "");
    }

    @Repeat(100)
    @Test
    public void testBuild()
    {
        AlchemyHttp result = instance.build();
        assertThat(result, notNullValue());
        Map<String, String> expectedHeaders = new HashMap<>(Constants.DEFAULT_HEADERS);
        expectedHeaders.putAll(this.defaultHeaders);
        assertThat(result.getDefaultHeaders(), equalTo(expectedHeaders));
    }

    @Test
    public void testBuildEdgeCases()
    {
        // Nothing is set
        instance = AlchemyHttpBuilder.newInstance();
        instance.build();

        // No Executor Service set
        instance = AlchemyHttpBuilder.newInstance();
        instance.build();

        // No Timeout
        instance = AlchemyHttpBuilder.newInstance().usingExecutor(executor);
        instance.build();
    }

    @Test
    public void testDefaultIncludesBasicRequestHeaders()
    {
        instance = AlchemyHttpBuilder.newInstance()
                .usingExecutor(executor);

        AlchemyHttp result = instance.build();
        assertThat(result, notNullValue());
        Map<String, String> headers = result.getDefaultHeaders();
        assertThat(headers, hasKey("Accept"));
        assertThat(headers, hasKey("Content-Type"));
    }
}
