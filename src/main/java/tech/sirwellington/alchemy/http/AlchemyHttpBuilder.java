/*
 * Copyright 2015 Sir Wellington.
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
import sir.wellington.alchemy.arguments.Arguments;
import sir.wellington.alchemy.arguments.Assertions;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

/**
 *
 * @author SirWellington
 */
@BuilderPattern(role = BuilderPattern.Role.BUILDER)
public final class AlchemyHttpBuilder
{

    private HttpClient apacheHttpClient;
    private ExecutorService executor = Executors.newWorkStealingPool(1);
    private final Map<String, String> defaultHeaders = Maps.newHashMap();

    static AlchemyHttpBuilder newInstance()
    {
        return new AlchemyHttpBuilder();
    }

    private AlchemyHttpBuilder()
    {
        defaultHeaders.put("Accept", "application/json");
        defaultHeaders.put("User-Agent", "Mozilla");
    }

    public AlchemyHttpBuilder usingApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
    {
        Arguments.checkThat(apacheHttpClient).is(Assertions.notNull());
        this.apacheHttpClient = apacheHttpClient;
        return this;
    }

    public AlchemyHttpBuilder usingExecutorService(ExecutorService executor) throws IllegalArgumentException
    {
        Arguments.checkThat(executor).is(Assertions.notNull());
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
        Arguments.checkThat(apacheHttpClient).usingException((sir.wellington.alchemy.arguments.FailedAssertionException ex) -> new IllegalStateException("missing apache HTTP Client")).is(Assertions.notNull());
        Arguments.checkThat(executor).usingException(IllegalStateException.class).is(Assertions.notNull());
        AlchemyHttpStateMachine stateMachine = AlchemyHttpStateMachine.Builder.newInstance().withApacheHttpClient(apacheHttpClient).withExecutorService(executor).build();
        return new AlchemyHttpImpl(defaultHeaders, stateMachine);
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
            Arguments.checkThat(key).usingMessage("Key is empty").is(Assertions.nonEmptyString());
            Map<String, String> copy = ImmutableMap.copyOf(defaultHeaders);
            copy.put(key, value);
            return new AlchemyHttpImpl(copy, stateMachine);
        }

        @Override
        public AlchemyRequest.Step1 begin()
        {
            HttpRequest initialRequest = HttpRequest.Builder.newInstance().usingRequestHeaders(defaultHeaders).build();
            return stateMachine.begin(initialRequest);
        }
    }

}
