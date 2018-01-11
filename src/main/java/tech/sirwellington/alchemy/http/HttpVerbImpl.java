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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.google.gson.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.http.exceptions.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CLIENT;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = CLIENT)
@Internal
final class HttpVerbImpl implements HttpVerb
{

    private final static Logger LOG = LoggerFactory.getLogger(HttpVerbImpl.class);

    private final AlchemyRequestMapper requestMapper;

    HttpVerbImpl(AlchemyRequestMapper requestMapper)
    {
        checkThat(requestMapper).is(notNull());

        this.requestMapper = requestMapper;
    }

    static HttpVerbImpl using(AlchemyRequestMapper requestMapper)
    {
        return new HttpVerbImpl(requestMapper);
    }

    @Override
    public HttpResponse execute(HttpClient apacheHttpClient, Gson gson, HttpRequest request) throws AlchemyHttpException
    {
        checkThat(apacheHttpClient, gson, request)
                .usingMessage("null arguments")
                .are(notNull());

        HttpUriRequest apacheRequest = requestMapper.convertToApacheRequest(request);

        checkThat(apacheRequest)
                .throwing(ex -> new AlchemyHttpException("Could not map HttpRequest: " + request))
                .is(notNull());

        request.getRequestHeaders()
                .forEach(apacheRequest::addHeader);

        org.apache.http.HttpResponse apacheResponse;
        try
        {
            apacheResponse = apacheHttpClient.execute(apacheRequest);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute GET Request on {}", apacheRequest.getURI(), ex);
            throw new AlchemyHttpException(ex);
        }

        checkThat(apacheResponse)
                .throwing(ex -> new AlchemyHttpException(request, "Missing Apache Client Response"))
                .is(notNull());

        JsonElement json;
        try
        {
            json = extractJsonFromResponse(request, apacheResponse, gson);
        }
        catch (JsonParseException ex)
        {
            LOG.error("Could not parse Response from Request {} as JSON", request, ex);
            throw new JsonException(request, "Failed to parse Json", ex);
        }
        catch (Exception ex)
        {
            LOG.error("Could not parse Response from Request {}", request, ex);
            throw new OperationFailedException(request, ex);
        }
        finally
        {
            if (apacheResponse instanceof CloseableHttpResponse)
            {
                try
                {
                    ((CloseableHttpResponse) apacheResponse).close();
                }
                catch (IOException ex)
                {
                    LOG.error("Failed to close HTTP Response.", ex);
                    throw new OperationFailedException(request, "Could not close Http Response");
                }
            }
        }

        HttpResponse response = HttpResponse.Builder.newInstance()
                .withResponseBody(json)
                .withStatusCode(apacheResponse.getStatusLine().getStatusCode())
                .withResponseHeaders(extractHeadersFrom(apacheResponse))
                .usingGson(gson)
                .build();

        return response;
    }

    private JsonElement extractJsonFromResponse(HttpRequest matchingRequest, org.apache.http.HttpResponse apacheResponse, Gson gson) throws JsonException, JsonParseException
    {
        if (apacheResponse.getEntity() == null)
        {
            return JsonNull.INSTANCE;
        }

        HttpEntity entity = apacheResponse.getEntity();

        String contentType = entity.getContentType().getValue();

        /*
         * We used to care what the content type was, and had a check for it here.
         * But perhaps it's better if we don't care what the content type is, as long as we can read it as a String.
         */
        String responseString = null;
        try (final InputStream istream = entity.getContent())
        {
            byte[] rawBytes = ByteStreams.toByteArray(istream);
            responseString = new String(rawBytes, UTF_8);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to read entity from request", ex);
            throw new AlchemyHttpException(matchingRequest, "Failed to read response from server", ex);
        }

        if (Strings.INSTANCE.isNullOrEmpty(responseString))
        {
            return JsonNull.INSTANCE;
        }
        else
        {
            if (contentType.contains("application/json"))
            {
                return gson.fromJson(responseString, JsonElement.class);
            }
            else
            {
                return gson.toJsonTree(responseString);
            }
        }
    }

    private Map<String, String> extractHeadersFrom(org.apache.http.HttpResponse apacheResponse)
    {
        if (apacheResponse.getAllHeaders() == null)
        {
            return Collections.emptyMap();
        }

        Map<String,String> headers = Maps.create();

        for(Header header : apacheResponse.getAllHeaders())
        {
            String headerName = header.getName();
            String headerValue = header.getValue();

            String existingValue = headers.get(headerName);

            if(!Strings.INSTANCE.isNullOrEmpty(existingValue))
            {
                existingValue = joinValues(existingValue, headerValue);
            }
            else
            {
                existingValue = headerValue;
            }

            headers.put(headerName, existingValue);
        }

        return headers;
    }

    private String joinValues(String first, String second)
    {
        return String.format("%s, %s", first, second);
    }

}
