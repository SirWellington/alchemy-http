/*
 * Copyright 2015 SirWellington Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.arguments.Assertions;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import static tech.sirwellington.alchemy.http.InternalAssertions.validContentType;

/**
 *
 * @author SirWellington
 */
final class BaseVerb implements HttpVerb
{

    private final static Logger LOG = LoggerFactory.getLogger(BaseVerb.class);

    private final Gson gson = new Gson();
    private final AlchemyRequestMapper requestMapper;

    BaseVerb(AlchemyRequestMapper requestMapper)
    {
        checkThat(requestMapper).is(notNull());

        this.requestMapper = requestMapper;
    }

    static BaseVerb using(AlchemyRequestMapper requestMapper)
    {
        return new BaseVerb(requestMapper);
    }

    @Override
    public HttpResponse execute(HttpClient apacheHttpClient, HttpRequest request) throws AlchemyHttpException
    {
        checkThat(apacheHttpClient, request)
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
            LOG.error("Failed to execute GET Request on {}", request, ex);
            throw new AlchemyHttpException(ex);
        }

        JsonElement json;
        try
        {
            json = extractJsonFromResponse(apacheResponse);
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

        HttpResponse response = HttpResponse.Builder.newInstance()
                .withResponse(json)
                .withStatusCode(apacheResponse.getStatusLine().getStatusCode())
                .withResponseHeaders(extractHeadersFrom(apacheResponse))
                .build();
        
        return response;
    }

    private JsonElement extractJsonFromResponse(org.apache.http.HttpResponse apacheResponse) throws JsonException
    {
        if (apacheResponse == null || apacheResponse.getEntity() == null)
        {
            return JsonNull.INSTANCE;
        }

        HttpEntity entity = apacheResponse.getEntity();

        String contentType = entity.getContentType().getValue();
        checkThat(contentType)
                .throwing(JsonException.class)
                .is(validContentType());

        String responseString = null;
        try
        {
            try (final InputStream istream = entity.getContent())
            {
                byte[] rawBytes = ByteStreams.toByteArray(istream);
                responseString = new String(rawBytes, Charsets.UTF_8);
            }
        }
        catch (FailedAssertionException ex)
        {
            throw new AlchemyHttpException("Unexpected Content Type", ex);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to read entity from request", ex);
            throw new AlchemyHttpException("Failed to read response from server", ex);
        }
        finally
        {
            try
            {
                EntityUtils.consume(entity);
            }
            catch (IOException ex)
            {
                LOG.error("Failed to finish consuming HTTP Entity", ex);
            }
        }

        if (Strings.isNullOrEmpty(responseString))
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
        return Arrays.asList(apacheResponse.getAllHeaders())
                .stream()
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }

}
