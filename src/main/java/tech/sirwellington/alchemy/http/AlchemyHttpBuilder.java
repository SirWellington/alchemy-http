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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.HttpClient;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.arguments.Arguments;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import tech.sirwellington.alchemy.arguments.Assertions;
import static tech.sirwellington.alchemy.arguments.Assertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@BuilderPattern(role = BuilderPattern.Role.BUILDER)
public final class AlchemyHttpBuilder
{

    @Internal
    private final static Map<String, String> DEFAULT_HEADERS = ImmutableMap.<String, String>builder()
            .put("Accept", "application/json, text/plain")
            .put("User-Agent", "Alchemy HTTP")
            .build();

    private HttpClient apacheHttpClient;
    private ExecutorService executor = Executors.newWorkStealingPool(1);
    //Copy from DEFAULT HEADERS
    private final Map<String, String> defaultHeaders = Maps.newHashMap(DEFAULT_HEADERS);

    static AlchemyHttpBuilder newInstance()
    {
        return new AlchemyHttpBuilder();
    }

    private AlchemyHttpBuilder()
    {
    }

    public AlchemyHttpBuilder usingApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
    {
        checkThat(apacheHttpClient).is(Assertions.notNull());
        this.apacheHttpClient = apacheHttpClient;
        return this;
    }

    public AlchemyHttpBuilder usingExecutorService(ExecutorService executor) throws IllegalArgumentException
    {
        checkThat(executor).is(Assertions.notNull());
        this.executor = executor;
        return this;
    }

    public AlchemyHttpBuilder withoutAsyncThread()
    {
        return usingExecutorService(MoreExecutors.newDirectExecutorService());
    }

    public AlchemyHttpBuilder usingDefaultHeaders(Map<String, String> defaultHeaders) throws IllegalArgumentException
    {
        Arguments.checkThat(defaultHeaders).is(Assertions.notNull());
        this.defaultHeaders.clear();
        this.defaultHeaders.putAll(defaultHeaders);
        return this;
    }

    public AlchemyHttp build() throws IllegalStateException
    {
        checkThat(apacheHttpClient)
                .throwing(ex -> new IllegalStateException("missing apache HTTP Client"))
                .is(notNull());

        checkThat(executor)
                .throwing(ex -> new IllegalStateException("missing Executor Service"))
                .is(Assertions.notNull());

        AlchemyHttpStateMachine stateMachine = buildTheStateMachine();

        return new AlchemyHttpImpl(defaultHeaders, stateMachine);
    }

    private AlchemyHttpStateMachine buildTheStateMachine()
    {
        return AlchemyHttpStateMachine.Builder.newInstance()
                .withApacheHttpClient(apacheHttpClient)
                .withExecutorService(executor)
                .build();
    }

    @Internal
    @BuilderPattern(role = BuilderPattern.Role.PRODUCT)
    static class AlchemyHttpImpl implements AlchemyHttp
    {

        private final Map<String, String> defaultHeaders;
        private final AlchemyHttpStateMachine stateMachine;

        AlchemyHttpImpl(Map<String, String> defaultHeaders, AlchemyHttpStateMachine stateMachine)
        {
            this.defaultHeaders = ImmutableMap.copyOf(defaultHeaders);
            this.stateMachine = stateMachine;
        }

        @Override
        public AlchemyHttp setDefaultHeader(String key, String value)
        {
            checkThat(key)
                    .usingMessage("Key is empty")
                    .is(nonEmptyString());
            Map<String, String> copy = ImmutableMap.copyOf(defaultHeaders);
            copy.put(key, value);
            return new AlchemyHttpImpl(copy, stateMachine);
        }

        @Override
        public AlchemyRequest.Step1 begin()
        {
            HttpRequest initialRequest = HttpRequest.Builder.newInstance()
                    .usingRequestHeaders(defaultHeaders)
                    .build();
            return stateMachine.begin(initialRequest);
        }
    }

}
