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

import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.NonNull;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.FluidAPIDesign;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;

/**
 *
 * @see #newBuilder()
 * @see AlchemyHttpBuilder
 *
 * @author SirWellington
 */
@Immutable
@ThreadSafe
@FluidAPIDesign
@BuilderPattern(role = PRODUCT)
public interface AlchemyHttp
{

    /**
     * Sets a default header on this instance. This default header will be included with every
     * request, unless it is explicitly
     * {@linkplain  AlchemyRequest.Step3#usingHeader(java.lang.String, java.lang.String)
     * }
     *
     * @param key
     * @param value
     *
     * @return
     */
    AlchemyHttp usingDefaultHeader(@NonEmpty String key, String value);

    /**
     * Get the Default Headers used by this instance.
     *
     * @return Headers, never {@code null}.
     */
    @NonNull
    Map<String, String> getDefaultHeaders();

    /**
     * Begins a new Alchemy HTTP Request.
     *
     * @return
     */
    AlchemyRequest.Step1 go();

    /**
     * Creates a new {@link AlchemyHttp} using the default settings for the
     * {@linkplain HttpClient Apache HTTP Client} and a
     * {@linkplain Executors#newWorkStealingPool(int) Single-Threaded Executor} for Async requests.
     *
     * @return
     */
    static AlchemyHttp newDefaultInstance()
    {
        HttpClient apacheHttpClient = HttpClientBuilder.create()
                .build();

        ExecutorService executor = Executors.newWorkStealingPool(1);

        return AlchemyHttpBuilder.newInstance()
                .usingApacheHttpClient(apacheHttpClient)
                .usingExecutorService(executor)
                .build();
    }

    /**
     * Create an instance using the {@linkplain HttpClient Apache HTTP Client} provided.
     *
     * @param apacheHttpClient
     *
     * @return
     *
     * @throws IllegalArgumentException
     */
    static AlchemyHttp newInstanceWithApacheHttpClient(@NonNull HttpClient apacheHttpClient) throws IllegalArgumentException
    {
        return newInstance(apacheHttpClient, MoreExecutors.newDirectExecutorService(), Collections.EMPTY_MAP);
    }

    /**
     * Creates a new {@link AlchemyHttp} instance.
     *
     * @param apacheHttpClient The {@linkplain HttpClient Apache Http Client} to use when making
     *                         requests.
     * @param executorService  For Async requests, this {@link ExecutorService} will be used.
     * @param defaultHeaders   Default Headers are included in every request, unless otherwise
     *                         specified.
     *
     * @return
     * @throws IllegalArgumentException
     */
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
                .usingMessage("Default HTTP Headers cannot be null. Use an empty map instead.")
                .is(notNull());

        return AlchemyHttpBuilder.newInstance()
                .usingApacheHttpClient(apacheHttpClient)
                .usingExecutorService(executorService)
                .usingDefaultHeaders(defaultHeaders)
                .build();
    }

    /**
     * Creates a new {@link AlchemyHttpBuilder} instance that allows additional customization of the
     * {@link AlchemyHttp} instance created.
     *
     * @return
     */
    static AlchemyHttpBuilder newBuilder()
    {
        return AlchemyHttpBuilder.newInstance();
    }

}
