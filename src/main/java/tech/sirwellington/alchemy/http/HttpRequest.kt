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

import com.google.gson.JsonElement
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.concurrency.Immutable
import tech.sirwellington.alchemy.annotations.concurrency.Mutable
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonEmptyMap
import tech.sirwellington.alchemy.kotlin.extensions.notNull
import java.net.URL


/**
 * Represents the RequestMethod that the request should use.
 *
 * @author SirWellington
 */
enum class RequestMethod
{

    GET,
    POST,
    HEAD,
    OPTIONS,
    PUT,
    DELETE,
    TRACE
    ;

    val asString = this.toString()
}


/**
 *
 * @author SirWellington
 */
@Immutable
@BuilderPattern(role = PRODUCT)
interface HttpRequest
{

    val requestHeaders: Map<String, String>?
    val queryParams: Map<String, String>?
    val url: URL?
    val body: JsonElement?
    val method: RequestMethod

    fun hasBody() = body.notNull

    fun hasMethod() = method.notNull

    fun hasQueryParams(): Boolean
    {
        val queryParams = queryParams
        return queryParams != null && !queryParams.isEmpty()
    }

    fun equals(other: HttpRequest?): Boolean
    {
        if (other == null)
        {
            return false
        }

        if (this === other)
        {
            return true
        }

        if (this.requestHeaders != other.requestHeaders)
        {
            return false
        }

        if (this.queryParams != other.queryParams)
        {
            return false
        }

        if (this.url != other.url)
        {
            return false
        }

        if (this.body != other.body)
        {
            return false
        }

        if (this.method != other.method)
        {
            return false
        }

        return true

    }

    @BuilderPattern(role = BUILDER)
    @FactoryMethodPattern(role = Role.PRODUCT)
    @Mutable
    class Builder
    {

        private var requestHeaders: MutableMap<String, String> = Maps.create()
        private var queryParams: MutableMap<String, String> = Maps.create()

        private var url: URL? = null
        private var body: JsonElement? = null
        private var requestMethod: RequestMethod = Constants.DEFAULT_REQUEST_METHOD

        @Throws(IllegalArgumentException::class)
        fun usingRequestHeaders(requestHeaders: Map<String, String>): Builder
        {
            checkThat(requestHeaders).isA(nonEmptyMap())

            this.requestHeaders.clear()
            this.requestHeaders.putAll(requestHeaders)
            return this
        }

        @Throws(IllegalArgumentException::class)
        fun usingQueryParams(queryParams: Map<String, String>): Builder
        {
            checkThat(queryParams).isA(nonEmptyMap())

            this.queryParams.clear()
            this.queryParams.putAll(queryParams)
            return this
        }

        fun usingUrl(url: URL): Builder
        {
            this.url = url
            return this
        }

        fun usingBody(body: JsonElement): Builder
        {
            this.body = body
            return this
        }

        fun usingRequestMethod(method: RequestMethod): Builder
        {
            this.requestMethod = method
            return this
        }

        @Throws(IllegalArgumentException::class)
        fun build(): HttpRequest
        {
            val url =  this.url
            val body = this.body
            val method = this.requestMethod

            return ActualRequestObject(requestHeaders = this.requestHeaders,
                                       queryParams = this.queryParams,
                                       url = url,
                                       body = body,
                                       method = method)
        }


        override fun toString(): String
        {
            return "Builder(requestHeaders=$requestHeaders, queryParams=$queryParams, url=$url, body=$body, requestMethod=$requestMethod)"
        }
        companion object
        {

            @JvmStatic
            @FactoryMethodPattern(role = Role.FACTORY_METHOD)
            fun newInstance(): Builder
            {
                return Builder()
            }

            @JvmStatic
            @FactoryMethodPattern(role = Role.FACTORY_METHOD)
            fun from(other: HttpRequest?): Builder
            {
                val builder = newInstance()

                if (other == null)
                {
                    return builder
                }

                other.requestHeaders?.let { builder.requestHeaders = it.toMutableMap() }
                other.queryParams?.let { builder.queryParams = it.toMutableMap() }

                builder.url = other.url
                builder.body = other.body
                builder.requestMethod = other.method

                return builder
            }
        }


        @Immutable
        @BuilderPattern(role = PRODUCT)
        private data class ActualRequestObject(override val requestHeaders: Map<String, String>,
                                               override val queryParams: Map<String, String>,
                                               override val url: URL?,
                                               override val body: JsonElement?,
                                               override val method: RequestMethod): HttpRequest



    }

    companion object
    {

        @FactoryMethodPattern(role = Role.FACTORY_METHOD)
        fun copyOf(other: HttpRequest): HttpRequest
        {
            return Builder.from(other).build()
        }
    }

}
