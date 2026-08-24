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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import sir.wellington.alchemy.collections.maps.Maps;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveInteger;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

public class AlchemyHttpBuilder {
    private Executor executor = SynchronousExecutor.newInstance();
    private final Map<String, String> defaultHeaders = Maps.copyOf(Constants.DEFAULT_HEADERS);
    private Gson gson = Constants.DEFAULT_GSON;
    private long timeoutMillis = Constants.DEFAULT_TIMEOUT;

    private AlchemyHttpBuilder() {
    }

    public AlchemyHttpBuilder usingExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public AlchemyHttpBuilder usingGson(Gson gson) {
        this.gson = gson;
        return this;
    }

    public AlchemyHttpBuilder usingTimeout(int timeout, TimeUnit timeUnit) {
        checkThat(timeout)
            .usingMessage("timeout must be > 0")
            .isA(positiveInteger());

        this.timeoutMillis = timeUnit.toMillis(timeout);
        return this;
    }

    public AlchemyHttpBuilder enableAsyncCallbacks() {
        return usingExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    public AlchemyHttpBuilder disableAsyncCallbacks() {
        return usingExecutor(SynchronousExecutor.newInstance());
    }

    public AlchemyHttpBuilder usingDefaultHeaders(Map<String, String> defaultHeaders) {
        checkThat(defaultHeaders)
            .isA(notNull());

        this.defaultHeaders.putAll(defaultHeaders);
        return this;
    }

    public AlchemyHttpBuilder usingDefaultHeader(
        String key,
        String value
    ) {
        checkThat(key)
            .usingMessage("missing key")
            .isA(nonEmptyString());

        this.defaultHeaders.put(key, value);
        return this;
    }

    public AlchemyHttp build() {
        checkThat(executor)
            .throwing(ex -> new IllegalStateException("missing Executor Service"))
            .isA(notNull());

        var stateMachine = AlchemyHttpStateMachine.Builder
            .newInstance()
            .usingExecutorService(executor)
            .usingGson(gson)
            .usingTimeout(timeoutMillis)
            .build();

        return new AlchemyHttpImpl(defaultHeaders, stateMachine);
    }

    public static AlchemyHttpBuilder newInstance() {
        return new AlchemyHttpBuilder();
    }
}
