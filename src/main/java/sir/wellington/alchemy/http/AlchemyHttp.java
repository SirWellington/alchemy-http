/*
 * Copyright 2015 Wellington.
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
package sir.wellington.alchemy.http;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.access.Internal;
import sir.wellington.alchemy.annotations.concurrency.Immutable;
import sir.wellington.alchemy.annotations.concurrency.ThreadSafe;
import sir.wellington.alchemy.annotations.patterns.BuilderPattern;
import static sir.wellington.alchemy.annotations.patterns.BuilderPattern.Role.BUILDER;
import static sir.wellington.alchemy.annotations.patterns.BuilderPattern.Role.PRODUCT;
import sir.wellington.alchemy.annotations.patterns.FluidAPIPattern;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.Assertions.nonEmptyString;
import static sir.wellington.alchemy.arguments.Assertions.notNull;
import static sir.wellington.alchemy.collections.maps.Maps.immutableCopyOf;
import sir.wellington.alchemy.http.operations.HttpOperation;
import sir.wellington.alchemy.http.operations.HttpRequest;

/**
 *
 *
 * @see Builder
 *
 * @author SirWellington
 */
@Immutable
@ThreadSafe
@FluidAPIPattern
@BuilderPattern(role = PRODUCT)
public interface AlchemyHttp
{

    AlchemyHttp setDefaultHeader(String key, String value);

    HttpOperation.Step1 begin();

    static AlchemyHttp newDefaultInstance()
    {
        HttpClient apacheHttpClient = HttpClientBuilder.create()
                .build();

        ExecutorService executor = Executors.newWorkStealingPool(1);

        return Builder.newInstance()
                .usingApacheHttpClient(apacheHttpClient)
                .usingExecutorService(executor)
                .build();
    }

    static AlchemyHttp newInstanceWithApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
    {
        return newInstance(apacheHttpClient, MoreExecutors.newDirectExecutorService(), Collections.EMPTY_MAP);
    }

    static AlchemyHttp newInstance(HttpClient apacheHttpClient,
                                   ExecutorService executorService,
                                   Map<String, String> defaultHeaders) throws IllegalArgumentException
    {
        checkThat(apacheHttpClient)
                .usingMessage("missing Apache HTTP Client")
                .is(notNull());
        checkThat(executorService)
                .usingMessage("missing ExecutorService")
                .is(notNull());
        checkThat(defaultHeaders)
                .usingMessage("missing default HTTP Headers")
                .is(notNull());

        return Builder.newInstance()
                .usingApacheHttpClient(apacheHttpClient)
                .usingExecutorService(executorService)
                .usingDefaultHeaders(defaultHeaders)
                .build();
    }

    @BuilderPattern(role = BUILDER)
    static final class Builder
    {

        private HttpClient apacheHttpClient;
        private ExecutorService executor = Executors.newWorkStealingPool(1);
        private final Map<String, String> defaultHeaders = Maps.newHashMap();

        public static Builder newInstance()
        {
            return new Builder();
        }

        private Builder()
        {
            defaultHeaders.put("Accept", "application/json");
            defaultHeaders.put("User-Agent", "Mozilla");
        }

        public Builder usingApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
        {
            checkThat(apacheHttpClient).is(notNull());
            this.apacheHttpClient = apacheHttpClient;
            return this;
        }

        public Builder usingExecutorService(ExecutorService executor) throws IllegalArgumentException
        {
            checkThat(executor).is(notNull());
            this.executor = executor;
            return this;
        }

        public Builder withoutAsyncThread()
        {
            return usingExecutorService(MoreExecutors.newDirectExecutorService());
        }

        public Builder usingDefaultHeaders(Map<String, String> defaultHeaders) throws IllegalArgumentException
        {
            checkThat(defaultHeaders).is(notNull());
            this.defaultHeaders.clear();
            this.defaultHeaders.putAll(defaultHeaders);
            return this;
        }

        public AlchemyHttp build() throws IllegalStateException
        {
            checkThat(apacheHttpClient)
                    .usingException(ex -> new IllegalStateException("missing apache HTTP Client"))
                    .is(notNull());

            checkThat(executor)
                    .usingException(IllegalStateException.class)
                    .is(notNull());

            AlchemyHttpStateMachine stateMachine = AlchemyHttpStateMachine.Builder.newInstance()
                    .withApacheHttpClient(apacheHttpClient)
                    .withExecutorService(executor)
                    .build();

            return new AlchemyHttpImpl(defaultHeaders, stateMachine);
        }

        @Internal
        @BuilderPattern(role = PRODUCT)
        static class AlchemyHttpImpl implements AlchemyHttp
        {

            private final Map<String, String> defaultHeaders;
            private final AlchemyHttpStateMachine stateMachine;

            AlchemyHttpImpl(Map<String, String> defaultHeaders, AlchemyHttpStateMachine stateMachine)
            {
                this.defaultHeaders = immutableCopyOf(defaultHeaders);
                this.stateMachine = stateMachine;
            }

            @Override
            public AlchemyHttp setDefaultHeader(String key, String value)
            {
                checkThat(key)
                        .usingMessage("Key is empty")
                        .is(nonEmptyString());

                Map<String, String> copy = sir.wellington.alchemy.collections.maps.Maps.mutableCopyOf(defaultHeaders);
                copy.put(key, value);
                return new AlchemyHttpImpl(copy, stateMachine);
            }

            @Override
            public HttpOperation.Step1 begin()
            {
                HttpRequest initialRequest = HttpRequest.Builder.newInstance()
                        .usingRequestHeaders(defaultHeaders)
                        .build();

                return stateMachine.begin(initialRequest);
            }

        }
    }

    public static void main(String[] args) throws MalformedURLException
    {
        Logger LOG = LoggerFactory.getLogger(AlchemyHttp.class);

        AlchemyHttp http = AlchemyHttp.newDefaultInstance();
        URL url = new URL("https://montanaflynn-dictionary.p.mashape.com/define?word=plain");

        http.begin()
                .bodyWithJsonObjectKeyValue("user-agent", "Mozilla/5.0 (Linux; U; Android 1.6; ar-us; SonyEricssonX10i Build/R2BA026) AppleWebKit/528.5+ (KHTML, like Gecko) Version/3.1.2 Mobile Safari/525.20.1")
                .get()
                .usingHeader("X-Mashape-Key", "3OoZRAgXILmsh6fzCW8MyQwbwCN2p17o64yjsn6qdjtZ9H1UdP")
                .onSuccess(r -> System.out.println(r))
                .onFailure(ex -> System.out.println("Failed: " + ex.getResponse()))
                .at(url);

    }
}
