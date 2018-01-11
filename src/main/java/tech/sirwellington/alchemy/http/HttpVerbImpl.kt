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
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.slf4j.LoggerFactory
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.FACTORY_METHOD
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CLIENT
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonNullReference
import tech.sirwellington.alchemy.http.HttpResponse.Builder
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.http.exceptions.JsonException
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException
import tech.sirwellington.alchemy.kotlin.extensions.isNull
import tech.sirwellington.alchemy.kotlin.extensions.notEmptyOrNull
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = CLIENT)
@Internal
internal class HttpVerbImpl(private val requestMapper: AlchemyRequestMapper) : HttpVerb
{


    @Throws(AlchemyHttpException::class)
    override fun execute(apacheHttpClient: HttpClient, gson: Gson, request: HttpRequest): HttpResponse
    {

        val apacheRequest = requestMapper.convertToApacheRequest(request)

        checkThat(apacheRequest)
                .throwing<Throwable> { ex -> AlchemyHttpException("Could not map HttpRequest: " + request) }
                .isA(nonNullReference())

        request.requestHeaders
               .forEach { s, s1 -> apacheRequest.addHeader(s, s1) }

        val apacheResponse = try
        {
            apacheHttpClient.execute(apacheRequest)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to execute GET Request on {}", apacheRequest.uri, ex)
            throw AlchemyHttpException(ex)
        }

        checkThat(apacheResponse)
                .throwing<Throwable> { ex -> AlchemyHttpException(request, "Missing Apache Client Response") }
                .isA(nonNullReference())

        val json= try
        {
            extractJsonFromResponse(request, apacheResponse, gson)
        }
        catch (ex: JsonParseException)
        {
            LOG.error("Could not parse Response from Request {} as JSON", request, ex)
            throw JsonException(request, "Failed to parse Json", ex)
        }
        catch (ex: Exception)
        {
            LOG.error("Could not parse Response from Request {}", request, ex)
            throw OperationFailedException(request, ex)
        }
        finally
        {
            if (apacheResponse is CloseableHttpResponse)
            {
                try
                {
                    apacheResponse.close()
                }
                catch (ex: IOException)
                {
                    LOG.error("Failed to close HTTP Response.", ex)
                    throw OperationFailedException(request, "Could not close Http Response")
                }

            }
        }

        return Builder.newInstance()
                      .withResponseBody(json)
                      .withStatusCode(apacheResponse.statusLine.statusCode)
                      .withResponseHeaders(extractHeadersFrom(apacheResponse))
                      .usingGson(gson)
                      .build()
    }

    @Throws(JsonException::class, JsonParseException::class)
    private fun extractJsonFromResponse(matchingRequest: HttpRequest,
                                        apacheResponse: org.apache.http.HttpResponse,
                                        gson: Gson): JsonElement
    {

        val entity = apacheResponse.entity ?: return JsonNull.INSTANCE
        val contentType = entity.contentType.value

        /*
         * We used to care what the content type was, and had a check for it here.
         * But perhaps it's better if we don't care what the content type is, as long as we can read it as a String.
         */
        var responseString: String = try
        {
            entity.content.use {
                val rawBytes = it.readBytes()
                String(rawBytes, UTF_8)
            }
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to read entity from request", ex)
            throw AlchemyHttpException(matchingRequest, "Failed to read response from server", ex)
        }

        return if (Strings.isNullOrEmpty(responseString))
        {
            JsonNull.INSTANCE
        }
        else
        {
            if (contentType.contains("application/json"))
            {
                gson.fromJson(responseString, JsonElement::class.java)
            }
            else
            {
                gson.toJsonTree(responseString)
            }
        }
    }

    private fun extractHeadersFrom(apacheResponse: org.apache.http.HttpResponse): Map<String, String>
    {
        if (apacheResponse.allHeaders.isNull)
        {
            return emptyMap()
        }

        val headers = Maps.create<String, String>()

        for (header in apacheResponse.allHeaders)
        {
            val headerName = header.name
            val headerValue = header.value

            var existingValue = headers[headerName]

            val valueToWrite = if (existingValue.notEmptyOrNull)
            {
                "$existingValue, $headerValue"
            }
            else
            {
                headerValue
            }

            headers[headerName] = valueToWrite
        }

        return headers
    }


    companion object
    {

        private val LOG = LoggerFactory.getLogger(HttpVerbImpl::class.java)

        @FactoryMethodPattern(role = FACTORY_METHOD)
        @JvmStatic
        fun using(requestMapper: AlchemyRequestMapper): HttpVerbImpl
        {
            return HttpVerbImpl(requestMapper)
        }
    }

}
