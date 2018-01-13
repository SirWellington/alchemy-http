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

import io.mikael.urlbuilder.UrlBuilder
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.arguments.Required
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.FACTORY_METHOD
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.PRODUCT
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.http.HttpAssertions.validRequest
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
@Internal
internal interface AlchemyRequestMapper
{

    fun map(request: HttpRequest) : HttpURLConnection

    companion object
    {
        @Throws(AlchemyHttpException::class, URISyntaxException::class, MalformedURLException::class)
        fun expandUrlFromRequest(@Required request: HttpRequest): URL
        {
            checkThat(request).isA(validRequest())

            val url = request.url ?: throw OperationFailedException("request is missing URL")

            return if (!request.hasQueryParams())
            {
                url
            }
            else
            {
                val uriBuilder = UrlBuilder.fromUrl(url)

                request.queryParams?.
                        forEach { param, value -> uriBuilder.addParameter(param, value) }

                uriBuilder.toUrl()
            }
        }

        @FactoryMethodPattern(role = FACTORY_METHOD)
        fun create(): AlchemyRequestMapper
        {
            return AlchemyRequestMapperImpl
        }
    }
}

@FactoryMethodPattern(role = PRODUCT)
@StrategyPattern(role = Role.CONCRETE_BEHAVIOR)
private object AlchemyRequestMapperImpl: AlchemyRequestMapper
{

    private val LOG = LoggerFactory.getLogger(this::class.java)

    override fun map(request: HttpRequest): HttpURLConnection
    {
        val url = AlchemyRequestMapper.expandUrlFromRequest(request)
        val connection = url.openConnection()

        val http = connection as? HttpURLConnection ?: throw OperationFailedException("URL is not an HTTP URL: [$url]")

        http.doInput = true
        http.requestMethod = request.method.asString

        request.requestHeaders?.forEach { key, value -> http.setRequestProperty(key, value) }

        if (request.hasBody())
        {
            http.doOutput = true
            http.setBody(request)
        }

        return http
    }

    private fun HttpURLConnection.setBody(request: HttpRequest)
    {
        val jsonString = request.body?.toString() ?: return

        try
        {
            this.outputStream.use { it ->
                it.bufferedWriter(Charsets.UTF_8).write(jsonString)
            }
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to set json request body [{}]", jsonString, ex)
            throw OperationFailedException(request, "Failed to set json request body [$jsonString]", ex)
        }
    }
}