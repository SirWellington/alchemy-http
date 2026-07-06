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
package tech.sirwellington.alchemy.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptyMap;

/**
 * Captures a complete Http Request made using Alchemy,
 * including:
 * <ol>
 *   <li>HTTP Request Headers</li>
 *   <li>Query Parameters (if any)</li>
 *   <li>The request body to send with the request (if any)</li>
 *   <li>The Request Method (one of {@link RequestMethod})</li>
 *   <li>The {@link URL} of the endpoint.</li>
 * </ol>
 *
 * @author SirWellington
 */
public interface HttpRequest
{

    Map<String, String> requestHeaders();

    Map<String, String> queryParams();

    URL url();

    JsonElement body();

    RequestMethod method();

    default boolean hasBody()
    {
        JsonElement body = body();
        return body != null && !(body instanceof JsonNull);
    }

    default boolean hasMethod()
    {
        return method() != null;
    }

    default boolean hasQueryParams()
    {
        Map<String, String> queryParams = queryParams();
        return queryParams != null && !queryParams.isEmpty();
    }

    default boolean equals(HttpRequest other)
    {
        if (other == null)
        {
            return false;
        }

        if (this == other)
        {
            return true;
        }

        if (!Objects.equals(this.requestHeaders(), other.requestHeaders()))
        {
            return false;
        }

        if (!Objects.equals(this.queryParams(), other.queryParams()))
        {
            return false;
        }

        if (!Objects.equals(this.url(), other.url()))
        {
            return false;
        }

        if (!Objects.equals(this.body(), other.body()))
        {
            return false;
        }

        if (!Objects.equals(this.method(), other.method()))
        {
            return false;
        }

        return true;
    }

    /**
     * Creates a defensive copy of the given {@link HttpRequest}.
     *
     * @param other The request to copy.
     * @return A new {@link HttpRequest} with the same data.
     */
    static HttpRequest copyOf(HttpRequest other)
    {
        return Builder.from(other).build();
    }

    /**
     * Builder for constructing {@link HttpRequest} instances.
     */
    public static class Builder
    {

        private Map<String, String> requestHeaders = new HashMap<>();
        private Map<String, String> queryParams = new HashMap<>();
        private URL url = null;
        private JsonElement body = null;
        private RequestMethod requestMethod = Constants.DEFAULT_REQUEST_METHOD;

        private Builder()
        {
        }

        /**
         * Sets the request headers to use.
         *
         * @param requestHeaders A non-empty map of headers.
         * @return this
         * @throws IllegalArgumentException if the map is null or empty.
         */
        public Builder usingRequestHeaders(Map<String, String> requestHeaders) throws IllegalArgumentException
        {
            Arguments.checkThat(requestHeaders).isA(nonEmptyMap());

            this.requestHeaders.clear();
            this.requestHeaders.putAll(requestHeaders);
            return this;
        }

        /**
         * Sets the query parameters to use.
         *
         * @param queryParams A non-empty map of query parameters.
         * @return this
         * @throws IllegalArgumentException if the map is null or empty.
         */
        public Builder usingQueryParams(Map<String, String> queryParams) throws IllegalArgumentException
        {
            Arguments.checkThat(queryParams).isA(nonEmptyMap());

            this.queryParams.clear();
            this.queryParams.putAll(queryParams);
            return this;
        }

        public Builder usingUrl(URL url)
        {
            this.url = url;
            return this;
        }

        public Builder usingBody(JsonElement body)
        {
            this.body = body;
            return this;
        }

        public Builder usingRequestMethod(RequestMethod method)
        {
            this.requestMethod = method;
            return this;
        }

        /**
         * Builds the {@link HttpRequest}.
         *
         * @return A new {@link HttpRequest} instance.
         * @throws IllegalArgumentException if the request is invalid.
         */
        public HttpRequest build() throws IllegalArgumentException
        {
            URL url = this.url;
            JsonElement body = this.body;
            RequestMethod method = this.requestMethod != null ? this.requestMethod : Constants.DEFAULT_REQUEST_METHOD;

            return new ActualRequestObject(
                    Map.copyOf(this.requestHeaders),
                    Map.copyOf(this.queryParams),
                    url,
                    body,
                    method
            );
        }

        @Override
        public String toString()
        {
            return "Builder(requestHeaders=" + requestHeaders
                    + ", queryParams=" + queryParams
                    + ", url=" + url
                    + ", body=" + body
                    + ", requestMethod=" + requestMethod + ")";
        }

        /**
         * Creates a new empty {@link Builder}.
         */
        public static Builder newInstance()
        {
            return new Builder();
        }

        /**
         * Creates a new {@link Builder} pre-populated from the given {@link HttpRequest}.
         *
         * @param other The request to copy from, or null.
         * @return A new Builder.
         */
        public static Builder from(HttpRequest other)
        {
            Builder builder = newInstance();

            if (other == null)
            {
                return builder;
            }

            if (other.requestHeaders() != null)
            {
                builder.requestHeaders = new HashMap<>(other.requestHeaders());
            }

            if (other.queryParams() != null)
            {
                builder.queryParams = new HashMap<>(other.queryParams());
            }

            builder.url = other.url();
            builder.body = other.body();
            builder.requestMethod = other.method();

            return builder;
        }

        /**
         * Internal implementation of {@link HttpRequest}.
         */
        private record ActualRequestObject(
                Map<String, String> requestHeaders,
                Map<String, String> queryParams,
                URL url,
                JsonElement body,
                RequestMethod method) implements HttpRequest
        {

            @Override
            public boolean equals(Object obj)
            {
                if (obj instanceof HttpRequest other)
                {
                    return HttpRequest.super.equals(other);
                }
                return false;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(requestHeaders, queryParams, url, body, method);
            }

            @Override
            public String toString()
            {
                return "HttpRequest(requestHeaders=" + requestHeaders
                        + ", queryParams=" + queryParams
                        + ", url=" + url
                        + ", body=" + body
                        + ", method=" + method + ")";
            }
        }
    }
}
