/*
 * Copyright 2015 Sir Wellington.
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
package sir.wellington.alchemy.http.operations;

import static com.google.common.base.Charsets.UTF_8;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import sir.wellington.alchemy.arguments.Assertion;
import sir.wellington.alchemy.arguments.FailedAssertionException;
import static sir.wellington.alchemy.arguments.assertions.Assertions.notNull;
import sir.wellington.alchemy.http.HttpResponse;
import sir.wellington.alchemy.http.exceptions.HttpException;
import sir.wellington.alchemy.http.exceptions.JsonParseException;

/**
 *
 * @author SirWellington
 */
class BaseVerb implements HttpVerb
{
    
    private final static Logger LOG = LoggerFactory.getLogger(BaseVerb.class);
    private final JsonParser jsonParser;
    private final Function<HttpRequest, HttpUriRequest> requestMapper;
    
    BaseVerb(JsonParser jsonParser, Function<HttpRequest, HttpUriRequest> requestMapper)
    {
        this.jsonParser = jsonParser;
        this.requestMapper = requestMapper;
    }
    
    @Override
    public HttpResponse execute(HttpClient apacheHttpClient, HttpRequest request) throws HttpException
    {
        checkThat(request)
                .usingMessage("missing request")
                .is(notNull());
        
        checkThat(apacheHttpClient)
                .usingMessage("missing http client")
                .is(notNull());
        
        HttpUriRequest apacheRequest = requestMapper.apply(request);
        
        request.getRequestHeaders().forEach(apacheRequest::addHeader);
        
        org.apache.http.HttpResponse apacheResponse;
        try
        {
            apacheResponse = apacheHttpClient.execute(apacheRequest);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute GET Request on {}", request);
            throw new HttpException(ex);
        }
        
        JsonElement json;
        try
        {
            json = extractJsonFromResponse(apacheResponse);
        }
        catch (com.google.gson.JsonParseException ex)
        {
            throw new JsonParseException("Failed to parse Json", ex);
        }
        
        HttpResponse response = HttpResponse.Builder.newInstance()
                .withResponse(json)
                .withStatusCode(apacheResponse.getStatusLine().getStatusCode())
                .withResponseHeaders(extractHeadersFrom(apacheResponse))
                .build();
        
        if (!response.isOk())
        {
            throw new HttpException(response, "Http Response not OK");
        }
        
        return response;
        
    }
    
    private JsonElement extractJsonFromResponse(org.apache.http.HttpResponse apacheResponse)
    {
        
        HttpEntity entity = null;
        String entityString = null;
        try
        {
            entity = apacheResponse.getEntity();
            Header contentType = entity.getContentType();
            
            checkThat(contentType)
                    .is(validContentType);
            
            try (InputStream istream = entity.getContent();)
            {
                byte[] bytes = ByteStreams.toByteArray(istream);
                entityString = new String(bytes, UTF_8);
            }
            
        }
        catch (FailedAssertionException ex)
        {
            throw new HttpException("Unexpected Content Type", ex);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to read entity from request", ex);
            throw new HttpException("Failed to read response from server", ex);
        }
        finally
        {
            if (entity != null)
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
        }
        
        if (Strings.isNullOrEmpty(entityString))
        {
            return JsonNull.INSTANCE;
        }
        else
        {
            return this.jsonParser.parse(entityString);
        }
        
    }
    private final Assertion<Header> validContentType = header ->
    {
        String contentType = header.getValue();
        switch (contentType)
        {
            case "application/json":
            case "text/plain":
                return;
            default:
                throw new FailedAssertionException("Not a valid HTTP content Type");
        }
    };
    
    private Map<String, String> extractHeadersFrom(org.apache.http.HttpResponse apacheResponse)
    {
        return Arrays.asList(apacheResponse.getAllHeaders())
                .stream()
                .collect(Collectors.toMap(Header::getName, Header::getValue));
    }
    
}
