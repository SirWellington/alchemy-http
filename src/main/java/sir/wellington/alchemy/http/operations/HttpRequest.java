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

    @BuilderPattern(role = BUILDER)
    class Builder
    {

        private final static Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

        private Map<String, String> requestHeaders;
        private URL url;
        private JsonElement body;
        private Class<?> responseClass;

        public static Builder newInstance()
        {
            return new Builder();
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

        public Builder usingResponseClass(Class<?> responseClass)
        {
            checkThat(responseClass).is(notNull());
            this.responseClass = responseClass;
            return this;
        }

        public HttpRequest build() throws IllegalArgumentException
        {
            checkThat(url)
                    .usingMessage("missing url")
                    .is(notNull());

            checkThat(responseClass)
                    .usingMessage("missing response Class")
                    .is(notNull());

            return new Impl();
        }

        @Immutable
        @BuilderPattern(role = PRODUCT)
        private static class Impl implements HttpRequest
        {
        }

    }

}
