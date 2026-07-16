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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.*;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
final class AlchemyHttpBuilderTest {

    @Mock
    private ExecutorService executor;

    private Map<String, String> defaultHeaders;

    private AlchemyHttpBuilder instance;

    @BeforeEach
    void setUp() {
        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 20);
        var timeout = NumberGenerators.longs(100, 2000).get();

        instance = AlchemyHttpBuilder.newInstance()
                                     .usingTimeout(Math.toIntExact(timeout), TimeUnit.MILLISECONDS)
                                     .usingExecutor(executor)
                                     .usingDefaultHeaders(defaultHeaders);
    }

    @Test
    public void testNewInstance() {
        instance = AlchemyHttpBuilder.newInstance();
        assertThat(instance, notNullValue());
    }

    @RepeatedTest(50)
    public void testUsingTimeout() {
        // Given
        var socketTimeout = one(integers(15, 100));
        // When
        var result = instance.usingTimeout(socketTimeout, TimeUnit.SECONDS);
        // Then
        assertThat(result, notNullValue());
    }

    @RepeatedTest(10)
    public void testUsingTimeoutWithBadArgs() {
        var negativeNumber = one(negativeIntegers());
        assertThrows(() -> instance.usingTimeout(negativeNumber, TimeUnit.SECONDS))
            .isIllegalArgumentException();
    }

    @Test
    public void testUsingGson() {
        var gson = new Gson();
        var result = instance.usingGson(gson);
        assertThat(result, notNullValue());
    }

    @RepeatedTest(100)
    public void testUsingExecutorService() {
        var result = instance.usingExecutor(executor);
        assertThat(result, notNullValue());
    }

    @Test
    public void testDisableAsyncCallbacks() {
        var result = instance.disableAsyncCallbacks();
        assertThat(result, notNullValue());
    }

    @Test
    public void testEnableAsyncCallbacks() {
        var result = instance.enableAsyncCallbacks();
        assertThat(result, notNullValue());
    }

    @RepeatedTest(100)
    public void testUsingDefaultHeaders() {
        instance = AlchemyHttpBuilder.newInstance();

        var headers = CollectionGenerators.mapOf(
            alphabeticStrings(),
            smallPositiveIntegers().mapping(String::valueOf),
            100
        );

        var result = instance.usingDefaultHeaders(headers);
        assertThat(result, notNullValue());

        var http = result.build();
        assertThat(http, notNullValue());

        var expected = Map.copyOf(headers);
        assertThat(http.getDefaultHeaders(), equalTo(expected));

        // Empty headers is ok
        instance.usingDefaultHeaders(Collections.emptyMap());
    }

    @Test
    public void testUsingDefaultHeader() {
        var key = one(alphabeticStrings());
        var value = one(hexadecimalString(10));

        var result = instance.usingDefaultHeader(key, value);
        assertThat(result, notNullValue());

        var http = result.build();
        assertThat(http.getDefaultHeaders(), hasEntry(key, value));
    }

    @Test
    public void testUsingDefaultHeaderEdgeCases() {
        var key = one(alphabeticStrings());
        // should be ok
        instance.usingDefaultHeader(key, "");
    }

    @RepeatedTest(100)
    public void testBuild() {
        var result = instance.build();
        assertThat(result, notNullValue());
        var expectedHeaders = Maps.copyOf(defaultHeaders);
        expectedHeaders.putAll(this.defaultHeaders);
        assertThat(result.getDefaultHeaders(), equalTo(expectedHeaders));
    }

    @Test
    public void testBuildEdgeCases() {
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
    public void testDefaultIncludesBasicRequestHeaders() {
        instance = AlchemyHttpBuilder.newInstance()
                                     .usingExecutor(executor);

        var result = instance.build();
        assertThat(result, notNullValue());
        var headers = result.getDefaultHeaders();
        assertThat(headers, hasKey("Accept"));
        assertThat(headers, hasKey("Content-Type"));
    }
}
