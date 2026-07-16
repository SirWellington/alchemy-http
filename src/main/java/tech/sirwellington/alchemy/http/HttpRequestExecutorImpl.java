/*
 * Copyright © 2026. Sir Wellington.
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
package tech.sirwellington.alchemy.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.http.exceptions.AlchemyConnectionException;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveLong;

/**
 * @author SirWellington
 */
final class HttpRequestExecutorImpl implements HttpRequestExecutor
{

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestExecutorImpl.class);

    private final HttpConnectionPreparer requestMapper;

    HttpRequestExecutorImpl(HttpConnectionPreparer requestMapper)
    {
        this.requestMapper = requestMapper;
    }

    @Override
    public HttpResponse execute(HttpRequest request, Gson gson, long timeoutMillis) throws AlchemyHttpException
    {
        checkThat(timeoutMillis).isA(positiveLong());

        HttpURLConnection http = requestMapper.map(request);
        http.setConnectTimeout((int) timeoutMillis);
        http.setReadTimeout((int) timeoutMillis);

        if (request.hasBody())
        {
            http.setDoOutput(true);
            setBody(http, request);
        }

        JsonElement json;

        try
        {
            json = performRequestForJson(request, http, gson);
        }
        catch (AlchemyHttpException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            LOG.error("Could not parse Response from Request {}", request, ex);
            throw new OperationFailedException(request, ex);
        }

        try
        {
            return HttpResponse.Builder.newInstance()
                    .withResponseBody(json)
                    .withStatusCode(http.getResponseCode())
                    .withResponseHeaders(extractHeadersFrom(http))
                    .usingGson(gson)
                    .build();
        }
        catch (IOException ex)
        {
            throw new OperationFailedException(request, "Failed to read response code", ex);
        }
    }

    private JsonElement performRequestForJson(HttpRequest request,
                                              HttpURLConnection http,
                                              Gson gson) throws AlchemyHttpException
    {
        String responseString;

        try
        {
            InputStream rawResponse = http.getInputStream();

            if (rawResponse == null)
            {
                return JsonNull.INSTANCE;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(rawResponse, StandardCharsets.UTF_8)))
            {
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }

                responseString = sb.toString();
            }
        }
        catch (SocketTimeoutException ex)
        {
            LOG.error("Failed to make request [{}]", request, ex);
            throw new AlchemyConnectionException(request, "HTTP request to [" + request.url() + "] timed out", ex);
        }
        catch (SocketException ex)
        {
            LOG.error("Failed to make request [{}]", request, ex);
            throw new AlchemyConnectionException(request, "Could not connect to server @[" + request.url() + "]", ex);
        }
        catch (UnknownHostException ex)
        {
            LOG.error("Failed to make request [{}]", request, ex);
            throw new AlchemyConnectionException(request, "Could not connect to server @[" + request.url() + "]", ex);
        }
        catch (IOException ex)
        {
            LOG.error("Failed to make request [{}]", request, ex);

            try
            {
                InputStream errorStream = http.getErrorStream();

                if (errorStream != null)
                {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8)))
                    {
                        StringBuilder sb = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null)
                        {
                            sb.append(line);
                        }

                        responseString = sb.toString();
                    }
                }
                else
                {
                    throw new OperationFailedException(request, "Request failed [" + request + "] |", ex);
                }
            }
            catch (AlchemyHttpException ahe)
            {
                throw ahe;
            }
            catch (IOException errorEx)
            {
                throw new OperationFailedException(request, "Request failed [" + request + "] |", ex);
            }
        }

        String contentType = http.getContentType();

        if (contentType == null)
        {
            contentType = "";
        }

        if (Strings.isNullOrEmpty(responseString))
        {
            return JsonNull.INSTANCE;
        }

        try
        {
            if (contentType.contains(ContentTypes.APPLICATION_JSON))
            {
                return gson.fromJson(responseString, JsonElement.class);
            }
            else
            {
                return gson.toJsonTree(responseString);
            }
        }
        catch (JsonSyntaxException | JsonParseException ex)
        {
            throw new JsonException(request, ex);
        }
        catch (Exception ex)
        {
            throw new OperationFailedException(request, ex);
        }
    }

    private Map<String, String> extractHeadersFrom(HttpURLConnection http)
    {
        Map<String, List<String>> headerFields = http.getHeaderFields();

        if (headerFields == null)
        {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : headerFields.entrySet())
        {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            if (key != null && values != null)
            {
                result.put(key, String.join(", ", values));
            }
        }

        return result;
    }

    private static void setBody(HttpURLConnection http, HttpRequest request) throws AlchemyHttpException
    {
        JsonElement body = request.body();

        if (body == null)
        {
            return;
        }

        String jsonString = body.toString();

        try (OutputStream outputStream = http.getOutputStream())
        {
            byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
        }
        catch (SocketException ex)
        {
            throw new AlchemyConnectionException(request, "Could not connect to server @[" + request.url() + "]", ex);
        }
        catch (UnknownHostException ex)
        {
            throw new AlchemyConnectionException(request, "Could not connect to server @[" + request.url() + "]", ex);
        }
        catch (IOException ex)
        {
            LOG.error("Failed to set json request body [{}]", jsonString, ex);
            throw new OperationFailedException(request, "Failed to set json request body [" + jsonString + "]", ex);
        }
    }

    static HttpRequestExecutorImpl create()
    {
        return new HttpRequestExecutorImpl(HttpConnectionPreparer.create());
    }

    static HttpRequestExecutorImpl create(HttpConnectionPreparer mapper)
    {
        return new HttpRequestExecutorImpl(mapper);
    }
}
