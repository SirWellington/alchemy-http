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

import com.google.gson.JsonElement;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.concurrency.Immutable;
import sir.wellington.alchemy.annotations.patterns.BuilderPattern;
import static sir.wellington.alchemy.annotations.patterns.BuilderPattern.Role.BUILDER;
import static sir.wellington.alchemy.annotations.patterns.BuilderPattern.Role.PRODUCT;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.assertions.Assertions.nonEmptyMap;
import static sir.wellington.alchemy.arguments.assertions.Assertions.notNull;
import sir.wellington.alchemy.collections.maps.MapOperations;

/**
 *
 * @author SirWellington
 */
@Immutable
@BuilderPattern(role = PRODUCT)
public interface HttpRequest
{

    Map<String, String> getRequestHeaders();

    URL getUrl();

    JsonElement getBody();

    default boolean hasBody()
    {
        return getBody() != null;
    }

    @BuilderPattern(role = BUILDER)
    class Builder
    {

        private final static Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

        private Map<String, String> requestHeaders;
        private URL url;
        private JsonElement body;

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
            builder.requestHeaders = other.getRequestHeaders();
            builder.body = other.getBody();

            return builder;
        }

        public Builder usingRequestHeaders(Map<String, String> requestHeaders) throws IllegalArgumentException
        {
            checkThat(requestHeaders).is(nonEmptyMap());
            this.requestHeaders = MapOperations.immutableCopyOf(requestHeaders);
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
            this.body = body;
            return this;
        }

        public HttpRequest build() throws IllegalArgumentException
        {
            checkThat(url)
                    .usingMessage("missing url")
                    .is(notNull());

            Impl instance = new Impl();

            instance.body = this.body;
            instance.requestHeaders = this.requestHeaders;
            instance.url = this.url;

            return instance;
        }

        @Immutable
        @BuilderPattern(role = PRODUCT)
        private static class Impl implements HttpRequest
        {

            private Map<String, String> requestHeaders;
            private URL url;
            private JsonElement body;

            @Override
            public Map<String, String> getRequestHeaders()
            {
                return requestHeaders;
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
            public int hashCode()
            {
                int hash = 3;
                hash = 97 * hash + Objects.hashCode(this.requestHeaders);
                hash = 97 * hash + Objects.hashCode(this.url);
                hash = 97 * hash + Objects.hashCode(this.body);
                return hash;
            }

            @Override
            public boolean equals(Object obj)
            {
                if (obj == null)
                {
                    return false;
                }
                if (getClass() != obj.getClass())
                {
                    return false;
                }
                final Impl other = (Impl) obj;
                if (!Objects.equals(this.requestHeaders, other.requestHeaders))
                {
                    return false;
                }
                if (!Objects.equals(this.url, other.url))
                {
                    return false;
                }
                if (!Objects.equals(this.body, other.body))
                {
                    return false;
                }
                return true;
            }

            @Override
            public String toString()
            {
                return "Impl{" + "requestHeaders=" + requestHeaders + ", url=" + url + ", body=" + body + '}';
            }

        }

    }

}
