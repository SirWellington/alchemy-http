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
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import jdk.nashorn.internal.ir.annotations.Immutable
import tech.sirwellington.alchemy.annotations.arguments.Optional
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.FACTORY_METHOD
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.http.HttpAssertions.validHttpStatusCode
import tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass
import tech.sirwellington.alchemy.http.exceptions.JsonException
import java.util.Collections.unmodifiableMap
import java.util.Objects

/**
 *
 * @see HttpResponse.Builder
 *
 *
 * @author SirWellington
 */
@Immutable
@FactoryMethodPattern(role = FactoryMethodPattern.Role.PRODUCT)
interface HttpResponse
{

    /**
     * HTTP OK are 200-208 or the 226 status code.
     *
     * @return true if the status code is "OK", false otherwise.
     *
     * @see  [https://en.wikipedia.org/wiki/List_of_HTTP_status_codes.2xx_Success](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes.2xx_Success)
     */
    //Valid HTTP "OK" level Status Codes
    val isOk: Boolean
        get()
        {
            val statusCode = statusCode()
            return statusCode in 200..208 || statusCode == 226
        }

    /**
     * @return The HTTP Status code of the request.
     */
    fun statusCode(): Int

    /**
     * @return The response headers returned by the REST Service.
     */
    @Optional
    fun responseHeaders(): Map<String, String>

    /**
     * Get the Response Body as a String.
     *
     * @return The Response Body as a String
     */
    fun bodyAsString(): String

    /**
     * Get the Response Body in JSON format.
     *
     * @return The JSON Response Body
     *
     * @throws JsonException
     */
    @Throws(JsonException::class)
    fun body(): JsonElement

    /**
     * Get the Response Body as a custom POJO Type. (Plain Old Java Object) Ensure that the POJO is styled in typical
     * Java Bean/Value object style. Getters and setters are not required, although [Object.hashCode]
     * and [Object.equals] is recommended for any value type.
     *
     * @param <T>      The type of the POJO
     * @param classOfT The Class of the POJO.
     *
     * @return An instance of `T`, mapped from the JSON Body.
     *
     * @throws JsonException If the [JSON Body][.body] could not be parsed.
    </T> */
    @Throws(JsonException::class)
    fun <T> bodyAs(classOfT: Class<T>): T

    /**
     * Use in cases where you expect the [Response Body][.body] to be a JSON Array. A [List] is
     * returned instead of an Array.
     *
     * @param <T>      The type of the POJO
     * @param classOfT The Class of the POJO.
     *
     * @return A List of T, parse from the JSON Body.
     *
     * @throws JsonException
    </T> */
    @Throws(JsonException::class)
    fun <T> bodyAsArrayOf(classOfT: Class<T>): List<T>

    /**
     * Tells you whether [this] and [other] are equal to each
     * other, according to:
     *
     * 1. The [status code][statusCode]
     * 2. [Response Headers][responseHeaders]
     * 3. [Response Body][bodyAsString]
     */
    fun equals(other: HttpResponse?): Boolean
    {
        if (other == null)
        {
            return false
        }

        if (this.statusCode() != other.statusCode())
        {
            return false
        }

        if (this.responseHeaders() != other.responseHeaders())
        {
            return false
        }

        if (this.bodyAsString() != other.bodyAsString())
        {
            return false
        }

        return true
    }

    //==============================================================================================
    // Builder Implementation
    //==============================================================================================
    @BuilderPattern(role = BUILDER)
    @FactoryMethodPattern(role = FactoryMethodPattern.Role.PRODUCT)
    class Builder
    {

        //Start negative to make sure that status code was set
        private var statusCode = -100
        private var responseHeaders = emptyMap<String, String>()
        private var gson = GsonBuilder()
                .setDateFormat(Constants.DATE_FORMAT)
                .create()

        private var responseBody: JsonElement = JsonNull.INSTANCE

        fun copyFrom(other: HttpResponse): Builder
        {
            return this.withResponseBody(other.body())
                    .withStatusCode(other.statusCode())
                    .withResponseHeaders(other.responseHeaders())
        }

        @Throws(IllegalArgumentException::class)
        fun withStatusCode(statusCode: Int): Builder
        {
            checkThat(statusCode).isA(validHttpStatusCode())

            this.statusCode = statusCode
            return this
        }

        @Throws(IllegalArgumentException::class)
        fun withResponseHeaders(responseHeaders: Map<String, String>?): Builder
        {
            val headers = responseHeaders ?: emptyMap()
            this.responseHeaders = headers

            return this
        }

        @Throws(IllegalArgumentException::class)
        fun withResponseBody(@Required json: JsonElement): Builder
        {
            this.responseBody = json
            return this
        }

        @Throws(IllegalArgumentException::class)
        fun usingGson(@Required gson: Gson): Builder
        {
            this.gson = gson
            return this
        }

        @Throws(IllegalStateException::class)
        fun build(): HttpResponse
        {
            checkThat(statusCode)
                    .throwing { ex -> IllegalStateException("Invalid status code supplied", ex) }
                    .isA(validHttpStatusCode())

            return Impl(statusCode,
                        unmodifiableMap(responseHeaders),
                        gson,
                        responseBody)
        }

        //==============================================================================================
        // Implementation
        //==============================================================================================
        @Immutable
        @BuilderPattern(role = PRODUCT)
        private class Impl constructor(private val statusCode: Int,
                                       private val responseHeaders: Map<String, String>,
                                       private val gson: Gson,
                                       private val responseBody: JsonElement) : HttpResponse
        {

            override fun statusCode(): Int
            {
                return statusCode
            }

            override fun responseHeaders(): Map<String, String>
            {
                return responseHeaders
            }

            override fun bodyAsString(): String
            {
                return if (responseBody.isJsonPrimitive)
                {
                    responseBody.asString
                }
                else
                {
                    responseBody.toString()
                }
            }

            @Throws(JsonParseException::class)
            override fun body(): JsonElement
            {
                return gson.toJsonTree(responseBody)
            }

            @Throws(JsonParseException::class)
            override fun <T> bodyAs(classOfT: Class<T>): T
            {
                checkThat(classOfT).isA(validResponseClass())

                try
                {
                    return gson.fromJson(responseBody, classOfT)
                }
                catch (ex: Exception)
                {
                    throw JsonException("Failed to parse json to class: " + classOfT, ex)
                }

            }

            @Throws(JsonException::class)
            override fun <T> bodyAsArrayOf(classOfT: Class<T>): List<T>
            {
                checkThat(classOfT).isA(validResponseClass())

                val type = object : TypeToken<Array<T>>()
                {}

                try
                {
                    val array = gson.fromJson<Array<T>>(responseBody, type.rawType) ?: null
                    return array?.asList() ?: emptyList()
                }
                catch (ex: Exception)
                {
                    throw JsonException("Failed to parse json to class: " + classOfT, ex)
                }

            }

            override fun hashCode(): Int
            {
                var hash = 3
                hash = 41 * hash + this.statusCode
                hash = 41 * hash + Objects.hashCode(this.responseHeaders)
                hash = 41 * hash + Objects.hashCode(this.responseBody)
                return hash
            }

            override fun equals(obj: Any?): Boolean
            {
                if (obj == null)
                {
                    return false
                }

                if (obj !is HttpResponse)
                {
                    return false
                }

                return this.equals(obj)
            }

            override fun toString(): String
            {
                return "HttpResponse{statusCode=$statusCode, responseHeaders=$responseHeaders, response=$responseBody}"
            }

        }

        companion object
        {

            @FactoryMethodPattern(role = FACTORY_METHOD)
            fun newInstance(): Builder
            {
                return Builder()
            }
        }

    }

    companion object
    {

        @FactoryMethodPattern(role = FACTORY_METHOD)
        fun builder(): Builder
        {
            return Builder.newInstance()
        }
    }

}