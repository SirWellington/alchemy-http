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

import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import sir.wellington.alchemy.annotations.concurrency.Immutable;
import sir.wellington.alchemy.annotations.concurrency.ThreadSafe;
import sir.wellington.alchemy.annotations.patterns.BuilderPattern;
import static sir.wellington.alchemy.annotations.patterns.BuilderPattern.Role.PRODUCT;
import sir.wellington.alchemy.annotations.patterns.FluidAPIPattern;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.Assertions.notNull;

/**
 *
 *
 * @see #newBuilder()
 * @see AlchemyHttpBuilder
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

    AlchemyRequest.Step1 begin();

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

    static AlchemyHttp newInstanceWithApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
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
                .usingMessage("missing default HTTP Headers")
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
