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
import java.util.concurrent.TimeUnit;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static java.util.concurrent.TimeUnit.SECONDS;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.http.Constants.DEFAULT_HEADERS;

/**
 *
 * @author SirWellington
 */
@BuilderPattern(role = BUILDER)
public final class AlchemyHttpBuilder
{
    private static final HttpClient DEFAULT_APACHE_CLIENT = createDefaultApacheClient();
    
    private HttpClient apacheHttpClient = DEFAULT_APACHE_CLIENT;
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
    
    public AlchemyHttpBuilder usingApacheHttpClient(@Required HttpClient apacheHttpClient) throws IllegalArgumentException
    {
        checkThat(apacheHttpClient).is(notNull());
        
        this.apacheHttpClient = apacheHttpClient;
        return this;
    }

    /**
     * Directly sets the Executor Service to use for Asynchronous Requests. Asynchronous requests only happen when the
     * {@linkplain  AlchemyRequest.Step4#onSuccess(tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess) Callback}
     * is set on the Request.
     *
     * @param executor
     * @return
     * @throws IllegalArgumentException
     */
    public AlchemyHttpBuilder usingExecutorService(@Required ExecutorService executor) throws IllegalArgumentException
    {
        checkThat(executor).is(notNull());
        
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
    
    public AlchemyHttpBuilder usingDefaultHeaders(@Required Map<String, String> defaultHeaders) throws IllegalArgumentException
    {
        checkThat(defaultHeaders).is(notNull());
        
        this.defaultHeaders.clear();
        this.defaultHeaders.putAll(defaultHeaders);
        return this;
    }
    
    public AlchemyHttpBuilder usingDefaultHeader(@NonEmpty String key, @Optional String value) throws IllegalArgumentException
    {
        checkThat(key)
                .usingMessage("missing key")
                .is(nonEmptyString());

        this.defaultHeaders.put(key, value);
        return this;
    }
    
    public AlchemyHttp build() throws IllegalStateException
    {
        checkThat(apacheHttpClient)
                .throwing(ex -> new IllegalStateException("missing apache HTTP Client"))
                .is(notNull());
        
        checkThat(executor)
                .throwing(ex -> new IllegalStateException("missing Executor Service"))
                .is(notNull());
        
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

    private static HttpClient createDefaultApacheClient()
    {
        return createDefaultApacheClient(45, SECONDS);
    }

    private static HttpClient createDefaultApacheClient(int timeout, TimeUnit timeUnit)
    {
        int actualTimeout = (int) TimeUnit.SECONDS.toMillis(timeout);

        //I really hate that they don't specify the expected Unit for the Timeout.
        RequestConfig config = RequestConfig.custom()
            .setSocketTimeout(actualTimeout)
            .setConnectTimeout(actualTimeout)
            .setConnectionRequestTimeout(actualTimeout)
            .build();

        return HttpClientBuilder.create()
            .setDefaultRequestConfig(config)
            .build();
    }

}
