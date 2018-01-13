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

import com.google.gson.Gson
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty
import tech.sirwellington.alchemy.annotations.arguments.Optional
import tech.sirwellington.alchemy.annotations.arguments.Positive
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.FACTORY_METHOD
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonEmptyString
import tech.sirwellington.alchemy.arguments.assertions.nonNullReference
import tech.sirwellington.alchemy.arguments.assertions.positiveInteger
import tech.sirwellington.alchemy.http.Constants.DEFAULT_HEADERS
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Facilitates the creation of an [AlchemyHttp] instance.
 *
 * @author SirWellington
 */
@BuilderPattern(role = BUILDER)
class AlchemyHttpBuilder
{

    private var executor: Executor = SynchronousExecutor.newInstance()

    //Copy from DEFAULT HEADERS
    private val defaultHeaders = Maps.mutableCopyOf(DEFAULT_HEADERS)

    private var gson = Constants.DEFAULT_GSON
    private var timeoutMillis = Constants.DEFAULT_TIMEOUT

    /**
     * Directly sets the [Executor] to use for Asynchronous Requests.
     * Asynchronous requests only happen when a [Callback][AlchemyRequest.Step4.onSuccess]
     * is set on the Request.
     *
     * @param executor
     * @return
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class)
    fun usingExecutor(@Required executor: Executor): AlchemyHttpBuilder
    {
        this.executor = executor
        return this
    }

    /**
     * Sets the GSON to use to parse each request.
     * This is useful if you anticipate serialization issues with the default GSON configuration.
     *
     * @param gson The Gson to use to parse JsonObjects internally.
     * @return
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class)
    fun usingGson(@Required gson: Gson): AlchemyHttpBuilder
    {
        this.gson = gson
        return this
    }

    /**
     * Sets the Timeout to be used for each request.
     *
     * @param timeout Must be positive.
     * @param timeUnit The Unit of Time to use for the timeout.
     *
     * @return
     */
    fun usingTimeout(@Positive timeout: Int, timeUnit: TimeUnit): AlchemyHttpBuilder
    {
        checkThat(timeout)
                .usingMessage("timeout must be > 0")
                .isA(positiveInteger())

        this.timeoutMillis = timeUnit.toMillis(timeout.toLong())

        return this
    }

    fun enableAsyncCallbacks(): AlchemyHttpBuilder
    {
        return usingExecutor(Executors.newSingleThreadExecutor())
    }

    fun disableAsyncCallbacks(): AlchemyHttpBuilder
    {
        return usingExecutor(SynchronousExecutor.newInstance())
    }

    @Throws(IllegalArgumentException::class)
    fun usingDefaultHeaders(@Required defaultHeaders: Map<String, String>): AlchemyHttpBuilder
    {
        checkThat(defaultHeaders).isA(nonNullReference())

        this.defaultHeaders.putAll(defaultHeaders)
        return this
    }

    @Throws(IllegalArgumentException::class)
    fun usingDefaultHeader(@NonEmpty key: String, @Optional value: String): AlchemyHttpBuilder
    {
        checkThat(key)
                .usingMessage("missing key")
                .isA(nonEmptyString())

        this.defaultHeaders.put(key, value)
        return this
    }

    @Throws(IllegalStateException::class)
    fun build(): AlchemyHttp
    {
        checkThat(executor)
                .throwing { ex -> IllegalStateException("missing Executor Service") }
                .isA(nonNullReference())

        val stateMachine = buildTheStateMachine()

        return AlchemyHttpImpl(defaultHeaders, stateMachine)
    }

    private fun buildTheStateMachine(): AlchemyHttpStateMachine
    {
        return AlchemyHttpStateMachine.Builder
                                      .newInstance()
                                      .usingExecutorService(executor)
                                      .usingGson(gson)
                                      .usingTimeout(timeoutMillis)
                                      .build()
    }

    companion object
    {

        @JvmStatic
        @FactoryMethodPattern(role = FACTORY_METHOD)
        fun newInstance(): AlchemyHttpBuilder
        {
            return AlchemyHttpBuilder()
        }

    }

}
