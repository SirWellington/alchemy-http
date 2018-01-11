/*
 * Copyright © 2018. Sir Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.sirwellington.alchemy.http;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.FluidAPIDesign;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * To create an instance, see {@link #newDefaultInstance() }.
 *
 * To do a Get:
 *
 * <pre>
 * {@code
 * HttpResponse response = http.go()
 *                             .get()
 *                             .at("http://maps.google.com/api");
 *
 *  GoogleQuery query = response.as(GoogleQuery.class);
 * }
 * </pre>
 *
 * To do a Post:
 *
 * <pre>
 * {@code
 * Coffee coffee = http.go()
 *                     .post(request)
 *                     .expecting(Coffee.class)
 *                     .at("http://aroma.coffee/orders?orderNumber=99");
 * }
 * </pre>
 *
 * @see #newBuilder()
 * @see AlchemyHttpBuilder
 * @see <a href="https://github.com/SirWellington/alchemy-http">https://github.com/SirWellington/alchemy-http</a>
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
     * Sets a default header on this instance. This default header will be included with every request.
     *
     * @param key
     * @param value
     *
     * @return
     */
    AlchemyHttp usingDefaultHeader(@NonEmpty String key, @Optional String value);

    /**
     * Get the Default Headers used by this instance.
     *
     * @return Headers, never {@code null}.
     */
    @Required
    Map<String, String> getDefaultHeaders();

    /**
     * Begins a new Alchemy HTTP Request.
     *
     * @return
     */
    AlchemyRequest.Step1 go();

    /**
     * Creates a new {@link AlchemyHttp} using the default settings for the {@linkplain HttpClient Apache HTTP Client}
     * and a {@linkplain Executors#newWorkStealingPool(int) Single-Threaded Executor} for Async requests.
     *
     * @return
     */
    static AlchemyHttp newDefaultInstance()
    {
        HttpClient apacheHttpClient = HttpClientBuilder.create()
                .build();
        return newInstanceWithApacheHttpClient(apacheHttpClient);
    }

    /**
     * Create an instance using the {@linkplain HttpClient Apache HTTP Client} provided.
     *
     * @param apacheHttpClient
     * @return
     *
     * @throws IllegalArgumentException
     */
    static AlchemyHttp newInstanceWithApacheHttpClient(@Required HttpClient apacheHttpClient) throws IllegalArgumentException
    {
        return AlchemyHttpBuilder.newInstance()
                .usingApacheHttpClient(apacheHttpClient)
                .build();
    }

    /**
     * Creates a new {@link AlchemyHttp} instance.
     *
     * @param apacheHttpClient The {@linkplain HttpClient Apache Http Client} to use when making requests.
     * @param executorService  For Async requests, this {@link ExecutorService} will be used.
     * @param defaultHeaders   Default Headers are included in every request, unless otherwise specified.
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
     * Creates a new {@link AlchemyHttpBuilder} instance that allows additional customization of the {@link AlchemyHttp}
     * instance created.
     *
     * @return
     */
    static AlchemyHttpBuilder newBuilder()
    {
        return AlchemyHttpBuilder.newInstance();
    }

}
