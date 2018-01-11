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

import java.net.URL;
import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.Mutable;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptyMap;

/**
 *
 * @author SirWellington
 */
@Immutable
@BuilderPattern(role = PRODUCT)
public interface HttpRequest
{

    Map<String, String> getRequestHeaders();

    Map<String, String> getQueryParams();

    URL getUrl();

    JsonElement getBody();

    HttpVerb getVerb();

    default boolean hasBody()
    {
        return getBody() != null;
    }

    default boolean hasQueryParams()
    {
        Map<String, String> queryParams = getQueryParams();
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

        if (!Objects.equals(this.getRequestHeaders(), other.getRequestHeaders()))
        {
            return false;
        }

        if (!Objects.equals(this.getQueryParams(), other.getQueryParams()))
        {
            return false;
        }

        if (!Objects.equals(this.getUrl(), other.getUrl()))
        {
            return false;
        }

        if (!Objects.equals(this.getBody(), other.getBody()))
        {
            return false;
        }

        if (!Objects.equals(this.getVerb(), other.getVerb()))
        {
            return false;
        }

        return true;

    }

    static HttpRequest copyOf(HttpRequest other)
    {
        return Builder.from(other).build();
    }

    @BuilderPattern(role = BUILDER)
    @Mutable
    class Builder
    {

        private final static Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

        private Map<String, String> requestHeaders = Maps.create();
        private Map<String, String> queryParams = Maps.create();

        private URL url;
        private JsonElement body;
        private HttpVerb verb;

        public static Builder newInstance()
        {
            return new Builder();
        }

        public static Builder from(HttpRequest other)
        {
            Builder builder = newInstance();

            if (other == null)
            {
                return builder;
            }

            builder.url = other.getUrl();

            if (other.getRequestHeaders() != null)
            {
                builder.requestHeaders = Maps.mutableCopyOf(other.getRequestHeaders());
            }

            if (other.getQueryParams() != null)
            {
                builder.queryParams = Maps.mutableCopyOf(other.getQueryParams());
            }

            builder.body = other.getBody();
            builder.verb = other.getVerb();

            return builder;
        }

        public Builder usingRequestHeaders(Map<String, String> requestHeaders) throws IllegalArgumentException
        {
            checkThat(requestHeaders).is(nonEmptyMap());

            this.requestHeaders.clear();
            this.requestHeaders.putAll(requestHeaders);
            return this;
        }

        public Builder usingQueryParams(Map<String, String> queryParams) throws IllegalArgumentException
        {
            checkThat(queryParams).is(nonEmptyMap());

            this.queryParams.clear();
            this.queryParams.putAll(queryParams);
            return this;
        }

        public Builder usingUrl(URL url) throws IllegalArgumentException
        {
            checkThat(url).is(notNull());

            this.url = url;
            return this;
        }

        public Builder usingBody(JsonElement body)
        {
            checkThat(body)
                    .usingMessage("Use JsonNull instead of null")
                    .is(notNull());

            this.body = body;
            return this;
        }

        public Builder usingVerb(HttpVerb verb) throws IllegalArgumentException
        {
            checkThat(verb).usingMessage("missing verb").is(notNull());

            this.verb = verb;
            return this;
        }

        public HttpRequest build() throws IllegalArgumentException
        {
            Impl instance = new Impl();

            instance.requestHeaders = Maps.immutableCopyOf(this.requestHeaders);
            instance.queryParams = Maps.immutableCopyOf(this.queryParams);
            instance.body = this.body;
            instance.url = this.url;
            instance.verb = this.verb;

            return instance;
        }

        @Override
        public String toString()
        {
            return "Builder{" + "requestHeaders=" + requestHeaders + ", queryParams=" + queryParams + ", url=" + url + ", body=" + body + ", verb=" + verb + '}';
        }

        @Immutable
        @BuilderPattern(role = PRODUCT)
        private static class Impl implements HttpRequest
        {

            private Map<String, String> requestHeaders;
            private Map<String, String> queryParams;

            private URL url;
            private JsonElement body;
            private HttpVerb verb;

            @Override
            public Map<String, String> getRequestHeaders()
            {
                return requestHeaders;
            }

            @Override
            public Map<String, String> getQueryParams()
            {
                return queryParams;
            }

            @Override
            public URL getUrl()
            {
                return url;
            }

            @Override
            public JsonElement getBody()
            {
                return body;
            }

            @Override
            public HttpVerb getVerb()
            {
                return verb;
            }

            @Override
            public int hashCode()
            {
                int hash = 7;
                hash = 83 * hash + Objects.hashCode(this.requestHeaders);
                hash = 83 * hash + Objects.hashCode(this.queryParams);
                hash = 83 * hash + Objects.hashCode(this.url);
                hash = 83 * hash + Objects.hashCode(this.body);
                hash = 83 * hash + Objects.hashCode(this.verb);
                return hash;
            }

            @Override
            public boolean equals(Object obj)
            {
                if (obj == null)
                {
                    return false;
                }
                if (!(obj instanceof HttpRequest))
                {
                    return false;
                }

                return this.equals((HttpRequest) obj);
            }

            @Override
            public String toString()
            {
                return "Impl{" + "requestHeaders=" + requestHeaders + ", queryParams=" + queryParams + ", url=" + url + ", body=" + body + ", verb=" + verb + '}';
            }

        }

    }

}
