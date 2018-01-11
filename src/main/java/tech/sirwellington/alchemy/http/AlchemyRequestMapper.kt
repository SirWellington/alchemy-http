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

import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.http.HttpAssertions.validRequest
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8

/**
 * `AlchemyRequestMappers` convert [Alchemy Requests][HttpRequest] into a
 * corresponding [Apache Request][org.apache.http.HttpRequest].
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
@Internal
internal interface AlchemyRequestMapper
{

    @Throws(AlchemyHttpException::class)
    fun convertToApacheRequest(@Required alchemyRequest: HttpRequest): HttpUriRequest

    /**
     * The stock [Apache Delete Request][HttpDelete] does not allow a Body. This one does.
     */
    @Internal
    class HttpDeleteWithBody : HttpEntityEnclosingRequestBase
    {

        override fun getMethod(): String
        {
            return METHOD_NAME
        }

        constructor(uri: String) : super()
        {
            setURI(URI.create(uri))
        }

        constructor(uri: URI) : super()
        {
            setURI(uri)
        }

        companion object
        {

            @JvmStatic
            val METHOD_NAME = HttpDelete().method ?: "DELETE"
        }

    }

    companion object
    {

        private fun createFrom(block: (HttpRequest) -> (HttpUriRequest)): AlchemyRequestMapper
        {
            return object: AlchemyRequestMapper
            {
                override fun convertToApacheRequest(alchemyRequest: HttpRequest): HttpUriRequest
                {
                    return block(alchemyRequest)
                }
            }
        }

        @Internal
        @Throws(URISyntaxException::class, MalformedURLException::class)
        fun expandUrlFromRequest(@Required request: HttpRequest): URL
        {
            checkThat(request).isA(validRequest())

            return if (!request.hasQueryParams())
            {
                request.url
            }
            else
            {
                val uri = request.url.toURI()
                val uriBuilder = URIBuilder(uri)

                request.queryParams
                       .forEach { param, value -> uriBuilder.addParameter(param, value) }

                uriBuilder.build().toURL()
            }
        }

        @StrategyPattern(role = CONCRETE_BEHAVIOR)
        val GET = createFrom { request ->

            checkThat(request).isA(validRequest())

            try
            {
                val url = expandUrlFromRequest(request)
                HttpGet(url.toURI())
            }
            catch (ex: Exception)
            {
                throw AlchemyHttpException(request, "Could not convert to Apache GET Request", ex)
            }
        }

        @StrategyPattern(role = CONCRETE_BEHAVIOR)
        val POST = createFrom { request ->

            checkThat(request).isA(validRequest())

            try
            {
                val url = expandUrlFromRequest(request)
                val post = HttpPost(url.toURI())

                if (request.hasBody())
                {
                    val entity = StringEntity(request.body.toString(), UTF_8)
                    post.entity = entity
                }

                post
            }
            catch (ex: Exception)
            {
                throw AlchemyHttpException(request, "Could not convert to Apache POST Request", ex)
            }
        }

        @StrategyPattern(role = CONCRETE_BEHAVIOR)
        val PUT = createFrom { request ->

            checkThat(request).isA(validRequest())

            try
            {
                val url = expandUrlFromRequest(request)
                val put = HttpPut(url.toURI())

                if (request.hasBody())
                {
                    val entity = StringEntity(request.getBody().toString(), UTF_8)
                    put.entity = entity
                }

                put
            }
            catch (ex: Exception)
            {
                throw AlchemyHttpException(request, "Could not convert to Apache PUT Request", ex)
            }
        }

        @StrategyPattern(role = CONCRETE_BEHAVIOR)
        val DELETE = createFrom { request ->

            checkThat(request).isA(validRequest())

            try
            {
                val url = expandUrlFromRequest(request)

                if (request.hasBody())
                {
                    //Custom Delete that allows a Body in the request
                    val delete = HttpDeleteWithBody(url.toURI())
                    val entity = StringEntity(request.body.toString(), UTF_8)
                    delete.entity = entity
                    delete
                }
                else
                {
                    //The stock Apache Method
                    HttpDelete(url.toURI())
                }
            }
            catch (ex: Exception)
            {
                throw AlchemyHttpException(request, "Could not convert to Apache DELETE Request", ex)
            }
        }
    }

}
