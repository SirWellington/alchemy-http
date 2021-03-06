/*
 * Copyright © 2019. Sir Wellington.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.sirwellington.alchemy.http

import com.google.gson.*
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.FACTORY_METHOD
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR
import tech.sirwellington.alchemy.arguments.assertions.positiveLong
import tech.sirwellington.alchemy.arguments.checkThat
import tech.sirwellington.alchemy.http.HttpResponse.Builder
import tech.sirwellington.alchemy.http.exceptions.AlchemyConnectionException
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.http.exceptions.JsonException
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
@Internal
internal class HttpRequestExecutorImpl(private val requestMapper: HttpConnectionPreparer) : HttpRequestExecutor
{

    @Throws(AlchemyHttpException::class)
    override fun execute(request: HttpRequest, gson: Gson, timeoutMillis: Long): HttpResponse
    {
        checkThat(timeoutMillis).isA(positiveLong())

        val http = requestMapper.map(request)
        http.connectTimeout = timeoutMillis.toInt()

        if (request.hasBody())
        {
            http.doOutput = true
            http.setBody(request)
        }

        val json = try
        {
            performRequestForJson(request, http, gson)
        }
        catch (ex: Exception)
        {
            LOG.error("Could not parse Response from Request {}", request, ex)

            // If it already is one of our exception types,
            // don't wrap it and just pass it up.
            if (ex is AlchemyHttpException)
            {
                throw ex
            }

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
    private fun performRequestForJson(request: HttpRequest,
                                      http: HttpURLConnection,
                                      gson: Gson): JsonElement
    {
        val responseString: String = try
        {
            val rawResponse = http.inputStream ?: return JsonNull.INSTANCE

            val responseString = rawResponse.use()
            {
                it.bufferedReader(Charsets.UTF_8).readText()
            }

            responseString
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to make request [$request]", ex)

            when (ex)
            {
                is SocketTimeoutException ->
                    throw AlchemyConnectionException(request, "HTTP request to [${request.url}] timed out", ex)

                is SocketException ->
                    throw AlchemyConnectionException(request, "Could not connect to server @[${request.url}]", ex)

                is UnknownHostException ->
                    throw AlchemyConnectionException(request, "Could not connect to server @[${request.url}]", ex)
            }

            val errorMessage = http.errorStream?.use { it.bufferedReader().readText() }

            errorMessage ?: throw OperationFailedException(request, "Request failed [$request] |", ex)
        }
        finally
        {
            http.disconnect()
        }

        val contentType = http.contentType ?: ""

        if (Strings.isNullOrEmpty(responseString))
        {
            return JsonNull.INSTANCE
        }

        return try
        {
            if (contentType.contains(ContentTypes.APPLICATION_JSON))
            {
                gson.fromJson(responseString, JsonElement::class.java)
            }
            else
            {
                gson.toJsonTree(responseString)
            }
        }
        catch (ex: Exception)
        {
            throw when (ex)
            {
                is JsonSyntaxException, is JsonParseException -> JsonException(request, ex)
                else                                          -> OperationFailedException(request, ex)
            }
        }
    }

    private fun extractHeadersFrom(http: HttpURLConnection): Map<String, String>
    {
        val headers = http.headerFields?.toMutableMap() ?: return emptyMap()

        return headers.map { it.key to it.value.joinToString(separator = ", ") }
                .toMap()
    }


    private fun HttpURLConnection.setBody(request: HttpRequest)
    {
        val jsonString = request.body?.toString() ?: return

        try
        {
            this.outputStream.use { it ->
                val bytes = jsonString.toByteArray(Charsets.UTF_8)
                it.write(bytes)
            }
        }
        catch (ex: SocketException)
        {
            throw AlchemyConnectionException(request, "Could not connect to server @[${request.url}]", ex)
        }
        catch (ex: UnknownHostException)
        {
            throw AlchemyConnectionException(request, "Could not connect to server @[${request.url}]", ex)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to set json request body [{}]", jsonString, ex)
            throw OperationFailedException(request, "Failed to set json request body [$jsonString]", ex)
        }
    }


    companion object
    {

        private val LOG = LoggerFactory.getLogger(HttpRequestExecutorImpl::class.java)

        @FactoryMethodPattern(role = FACTORY_METHOD)
        @JvmStatic
        @JvmOverloads
        fun create(requestMapper: HttpConnectionPreparer = HttpConnectionPreparer.create()): HttpRequestExecutorImpl
        {
            return HttpRequestExecutorImpl(requestMapper)
        }
    }


}
