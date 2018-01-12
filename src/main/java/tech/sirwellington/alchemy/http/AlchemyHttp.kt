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

package tech.sirwellington.alchemy.http

import sun.net.www.http.HttpClient
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.concurrency.Immutable
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe
import tech.sirwellington.alchemy.annotations.designs.FluidAPIDesign
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 *
 * To create an instance, see [.newDefaultInstance].
 *
 * To do a Get:
 *
 * ```
 * HttpResponse response = http.go()
 * .get()
 * .at("http://maps.google.com/api");
 *
 * GoogleQuery query = response.as(GoogleQuery.class);
 * ```
 *
 * To execute a POST:
 *
 * ```
 * Coffee coffee = http.go()
 * .post(request)
 * .expecting(Coffee.class)
 * .at("http://aroma.coffee/orders?orderNumber=99");
 * ```
 *
 * @see .newBuilder
 * @see AlchemyHttpBuilder
 *
 * @see [https://github.com/SirWellington/alchemy-http](https://github.com/SirWellington/alchemy-http)
 *
 * @author SirWellington
 */
@Immutable
@ThreadSafe
@FluidAPIDesign
@BuilderPattern(role = PRODUCT)
interface AlchemyHttp
{

    /**
     * Get the Default Headers used by this instance.
     *
     * @return Headers, never `null`.
     */
    @get:Required
    val defaultHeaders: Map<String, String>

    /**
     * Sets a default header on this instance. This default header will be included with every request.
     *
     * @param key
     * @param value
     *
     * @return
     */
    fun usingDefaultHeader(@NonEmpty key: String, value: String): AlchemyHttp

    /**
     * Begins a new Alchemy HTTP Request.
     *
     * @return
     */
    fun go(): AlchemyRequest.Step1

    companion object
    {

        /**
         * Creates a new [AlchemyHttp] using the default settings for the [Apache HTTP Client][HttpClient]
         * and a [Single-Threaded Executor][Executors.newWorkStealingPool] for Async requests.
         *
         * @return
         */
        @FactoryMethodPattern(role = Role.FACTORY_METHOD)
        fun newDefaultInstance(): AlchemyHttp
        {
            return AlchemyHttpBuilder.newInstance()
                    .disableAsyncCallbacks()
                    .build()
        }


        /**
         * Creates a new [AlchemyHttp] instance.
         *
         * @param apacheHttpClient The [Apache Http Client][HttpClient] to use when making requests.
         * @param executorService  For Async requests, this [ExecutorService] will be used.
         * @param defaultHeaders   Default Headers are included in every request, unless otherwise specified.
         *
         * @return
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        @JvmOverloads
        fun newInstance(apacheHttpClient: HttpClient,
                        executorService: ExecutorService,
                        defaultHeaders: Map<String, String>,
                        timeout: Int = Constants.DEFAULT_TIMEOUT.toInt(),
                        timeUnit: TimeUnit = MILLISECONDS): AlchemyHttp
        {

            return AlchemyHttpBuilder.newInstance()
                                     .usingExecutorService(executorService)
                                     .usingDefaultHeaders(defaultHeaders)
                                     .usingTimeout(timeout, timeUnit)
                                     .build()
        }

        /**
         * Creates a new [AlchemyHttpBuilder] instance that allows additional customization of the [AlchemyHttp]
         * instance created.
         *
         * @return
         */
        @FactoryMethodPattern(role = Role.FACTORY_METHOD)
        fun newBuilder(): AlchemyHttpBuilder
        {
            return AlchemyHttpBuilder.newInstance()
        }
    }

}
