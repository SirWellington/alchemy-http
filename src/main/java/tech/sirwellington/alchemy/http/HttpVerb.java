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
package tech.sirwellington.alchemy.http;

import static com.google.common.base.Charsets.UTF_8;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import sir.wellington.alchemy.arguments.AlchemyAssertion;
import static sir.wellington.alchemy.arguments.Assertions.nonEmptyString;
import static sir.wellington.alchemy.arguments.Assertions.notNull;
import sir.wellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException;

/**
 *
 * @author SirWellington
 */
public interface HttpVerb
{

    HttpResponse execute(HttpClient apacheHttpClient, HttpRequest request) throws AlchemyHttpException;

    static HttpVerb get()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);

        Function<HttpRequest, HttpUriRequest> requetMapper = r ->
        {
            try
            {
                HttpGet get = new HttpGet(r.getUrl().toURI());
                return get;
            }
            catch (Exception ex)
            {
                LOG.error("Could not convert to Apache GET Request", ex);
                return null;
            }
        };
        return new BaseVerb(requetMapper);
    }

    static HttpVerb post()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);

        Function<HttpRequest, HttpUriRequest> requestMapper = r ->
        {
            try
            {
                HttpPost post = new HttpPost(r.getUrl().toURI());
                if (r.hasBody())
                {
                    HttpEntity entity = new StringEntity(r.getBody().toString(), UTF_8);
                    post.setEntity(entity);
                }
                return post;
            }
            catch (Exception ex)
            {
                LOG.error("Failed to convert {} to Apache HTTP POST Request", r, ex);
                return null;
            }

        };

        return new BaseVerb(requestMapper);
    }

    static HttpVerb put()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);

        Function<HttpRequest, HttpUriRequest> requestMapper = r ->
        {
            try
            {
                HttpPut put = new HttpPut(r.getUrl().toURI());
                if (r.hasBody())
                {
                    HttpEntity entity = new StringEntity(r.getBody().toString());
                    put.setEntity(entity);
                }

                return put;
            }
            catch (Exception ex)
            {
                LOG.error("Failed to convery {} to Apache HTTP PUT Request", r, ex);
                return null;
            }
        };

        return new BaseVerb(requestMapper);
    }

    static HttpVerb delete()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);
        Function<HttpRequest, HttpUriRequest> requestMapper = r ->
        {
            try
            {
                HttpDelete delete = new HttpDelete(r.getUrl().toURI());
                return delete;
            }
            catch (Exception ex)
            {
                LOG.error("Failed to convert {} to Apache Http DELETE Request", r, ex);
                return null;
            }
        };

        return new BaseVerb(requestMapper);
    }

    class BaseVerb implements HttpVerb
    {

        private final static Logger LOG = LoggerFactory.getLogger(BaseVerb.class);
        private final Gson gson = new Gson();
        private final Function<HttpRequest, HttpUriRequest> requestMapper;

        BaseVerb(Function<HttpRequest, HttpUriRequest> requestMapper)
        {
            this.requestMapper = requestMapper;
        }

        @Override
        public HttpResponse execute(HttpClient apacheHttpClient, HttpRequest request) throws AlchemyHttpException
        {
            checkThat(request)
                    .usingMessage("missing request")
                    .is(notNull());

            checkThat(apacheHttpClient)
                    .usingMessage("missing http client")
                    .is(notNull());

            HttpUriRequest apacheRequest = requestMapper.apply(request);

            if (apacheRequest == null)
            {
                throw new AlchemyHttpException("Could not map HttpRequest: " + request);
            }

            request.getRequestHeaders().forEach(apacheRequest::addHeader);

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

        private JsonElement extractJsonFromResponse(org.apache.http.HttpResponse apacheResponse)
        {

            HttpEntity entity = apacheResponse.getEntity();
            String contentType = entity.getContentType().getValue();

            checkThat(contentType).is(validContentType);

            String entityString = null;
            try
            {

                try (InputStream istream = entity.getContent();)
                {
                    byte[] bytes = ByteStreams.toByteArray(istream);
                    entityString = new String(bytes, UTF_8);
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
                if (contentType.contains("application/json"))
                {
                    return gson.fromJson(entityString, JsonElement.class);
                }
                else
                {
                    return gson.toJsonTree(entityString);
                }
            }

        }
        private final AlchemyAssertion<String> validContentType = contentType ->
        {

            checkThat(contentType)
                    .usingMessage("missing Content-Type")
                    .is(nonEmptyString());

            if (contentType.contains("application/json"))
            {
                return;
            }

            if (contentType.contains("text/plain"))
            {
                return;
            }

            throw new FailedAssertionException("Not a valid HTTP content Type: " + contentType);

        };

        private Map<String, String> extractHeadersFrom(org.apache.http.HttpResponse apacheResponse)
        {
            return Arrays.asList(apacheResponse.getAllHeaders())
                    .stream()
                    .collect(Collectors.toMap(Header::getName, Header::getValue));
        }

        @Override
        public String toString()
        {
            return "BaseVerb{" + "requestMapper=" + requestMapper + '}';
        }

    }

}
