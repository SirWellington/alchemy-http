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

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.HttpClient;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.arguments.Assertions;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import static tech.sirwellington.alchemy.http.Constants.DEFAULT_HEADERS;

/**
 *
 * @author SirWellington
 */
@BuilderPattern(role = BuilderPattern.Role.BUILDER)
public final class AlchemyHttpBuilder
{


    private HttpClient apacheHttpClient;
    private ExecutorService executor = MoreExecutors.newDirectExecutorService();

    //Copy from DEFAULT HEADERS
    private final Map<String, String> defaultHeaders = Maps.newHashMap(DEFAULT_HEADERS);

    static AlchemyHttpBuilder newInstance()
    {
        return new AlchemyHttpBuilder();
    }

    AlchemyHttpBuilder()
    {
    }

    public AlchemyHttpBuilder usingApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
    {
        checkThat(apacheHttpClient).is(Assertions.notNull());

        this.apacheHttpClient = apacheHttpClient;
        return this;
    }

    /**
     * Directly sets the Executor Service to use for Asynchronous Requests. Asynchronous requests
     * only happen when the
     * {@linkplain  AlchemyRequest.Step4#onSuccess(tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess) Callback}
     * is set on the Request.
     *
     * @param executor
     * @return
     * @throws IllegalArgumentException
     */
    public AlchemyHttpBuilder usingExecutorService(ExecutorService executor) throws IllegalArgumentException
    {
        checkThat(executor).is(Assertions.notNull());

        this.executor = executor;
        return this;
    }

    public AlchemyHttpBuilder enableAsyncCallbacks()
    {
        return usingExecutorService(Executors.newSingleThreadExecutor());
    }

    public AlchemyHttpBuilder disableAsyncCallbacks()
    {
        return usingExecutorService(MoreExecutors.newDirectExecutorService());
    }

    public AlchemyHttpBuilder usingDefaultHeaders(Map<String, String> defaultHeaders) throws IllegalArgumentException
    {
        checkThat(defaultHeaders).is(Assertions.notNull());

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
                .usingApacheHttpClient(apacheHttpClient)
                .usingExecutorService(executor)
                .build();
    }

}
