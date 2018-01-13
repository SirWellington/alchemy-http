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
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.FACTORY_METHOD
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CLIENT
import tech.sirwellington.alchemy.http.HttpResponse.Builder
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.http.exceptions.JsonException
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = CLIENT)
@Internal
internal class HttpExecutorImpl(private val requestMapper: AlchemyRequestMapper) : HttpExecutor
{

    @Throws(AlchemyHttpException::class)
    override fun execute(request: HttpRequest, gson: Gson, timeoutMillis: Long): HttpResponse
    {
        val http = requestMapper.map(request)
        http.connectTimeout = timeoutMillis.toInt()

        val json = try
        {
            extractJsonFromResponse(request, http, gson)
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
            http.disconnect()
        }

        return Builder.newInstance()
                .withResponseBody(json)
                .withStatusCode(http.responseCode)
                .withResponseHeaders(extractHeadersFrom(http))
                .usingGson(gson)
                .build()
    }

    @Throws(AlchemyHttpException::class)
    private fun extractJsonFromResponse(request: HttpRequest,
                                        http: HttpURLConnection,
                                        gson: Gson): JsonElement
    {
        val response = try
       {
           http.inputStream
       }
       catch (ex: SocketTimeoutException)
       {
           throw OperationFailedException("HTTP request to [${request.url}] timed out", ex)
       }
        catch (ex: Exception)
        {
            LOG.error("Failed to make request [$request]", ex)
            throw OperationFailedException("Request failed [$request]", ex)
        }

        if (response == null) return JsonNull.INSTANCE

        val responseString = try
        {
            response.use {
                it.bufferedReader(Charsets.UTF_8).readText()
            }
        }
        catch (ex: Exception)
        {
            throw OperationFailedException("Failed to read response from server", ex)
        }

        val contentType = http.contentType ?: ""

        if (Strings.isNullOrEmpty(responseString))
        {
            return JsonNull.INSTANCE
        }

        return if (contentType.contains("application/json"))
        {
            gson.fromJson(responseString, JsonElement::class.java)
        }
        else
        {
            gson.toJsonTree(responseString)
        }
    }

    private fun extractHeadersFrom(http: HttpURLConnection): Map<String, String>
    {
        val headers = http.headerFields?.toMutableMap() ?: return emptyMap()

        return headers.map { it.key to it.value.joinToString(separator = ", ") }
                      .toMap()
    }


    companion object
    {

        private val LOG = LoggerFactory.getLogger(HttpExecutorImpl::class.java)

        @FactoryMethodPattern(role = FACTORY_METHOD)
        @JvmStatic
        @JvmOverloads
        fun create(requestMapper: AlchemyRequestMapper = AlchemyRequestMapper.create()): HttpExecutorImpl
        {
            return HttpExecutorImpl(requestMapper)
        }
    }



}
