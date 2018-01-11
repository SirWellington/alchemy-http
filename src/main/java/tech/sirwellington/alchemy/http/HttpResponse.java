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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jdk.nashorn.internal.ir.annotations.Immutable;
import tech.sirwellington.alchemy.annotations.arguments.Nullable;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

import static java.util.Collections.unmodifiableMap;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.http.HttpAssertions.validHttpStatusCode;
import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

/**
 *
 * @see HttpResponse.Builder
 *
 * @author SirWellington
 */
@Immutable
public interface HttpResponse
{

    /**
     * @return The HTTP Status code of the request.
     */
    int statusCode();

    /**
     * HTTP OK are 200-208 or the 226 status code.
     *
     * @return true if the status code is "OK", false otherwise.
     *
     * @see
     * <a href="https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#2xx_Success">https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#2xx_Success</a>
     */
    default boolean isOk()
    {
        int statusCode = statusCode();
        //Valid HTTP "OK" level Status Codes
        return (statusCode >= 200 && statusCode <= 208) || statusCode == 226;
    }

    /**
     * @return The response headers returned by the REST Service.
     */
    @Nullable
    Map<String, String> responseHeaders();

    /**
     * Get the Response Body as a String.
     *
     * @return The Response Body as a String
     */
    String bodyAsString();

    /**
     * Get the Response Body in JSON format.
     *
     * @return The JSON Response Body
     *
     * @throws JsonException
     */
    JsonElement body() throws JsonException;

    /**
     * Get the Response Body as a custom POJO Type. (Plain Old Java Object) Ensure that the POJO is styled in typical
     * Java Bean/Value object style. Getters and setters are not required, although {@link Object#hashCode() }
     * and {@link Object#equals(java.lang.Object)} is recommended for any value type.
     *
     * @param <T>      The type of the POJO
     * @param classOfT The Class of the POJO.
     *
     * @return An instance of {@code T}, mapped from the JSON Body.
     *
     * @throws JsonException If the {@linkplain #body() JSON Body} could not be parsed.
     */
    <T> T bodyAs(Class<T> classOfT) throws JsonException;

    /**
     * Use in cases where you expect the {@linkplain #body() Response Body} to be a JSON Array. A {@link List} is
     * returned instead of an Array.
     *
     * @param <T>      The type of the POJO
     * @param classOfT The Class of the POJO.
     *
     * @return A List of T, parse from the JSON Body.
     *
     * @throws JsonException
     */
    <T> List<T> bodyAsArrayOf(Class<T> classOfT) throws JsonException;

    default boolean equals(HttpResponse other)
    {
        if (other == null)
        {
            return false;
        }

        if (this.statusCode() != other.statusCode())
        {
            return false;
        }

        if (!Objects.equals(this.responseHeaders(), other.responseHeaders()))
        {
            return false;
        }

        if (!Objects.equals(this.bodyAsString(), other.bodyAsString()))
        {
            return false;
        }

        return true;
    }

    static Builder builder()
    {
        return Builder.newInstance();
    }

    //==============================================================================================
    // Builder Implementation
    //==============================================================================================
    @BuilderPattern(role = BUILDER)
    static class Builder
    {

        //Start negative to make sure that status code was set
        private int statusCode = -100;
        private Map<String, String> responseHeaders = Collections.emptyMap();
        private Gson gson = new GsonBuilder()
                .setDateFormat(Constants.DATE_FORMAT)
                .create();

        private JsonElement responseBody = JsonNull.INSTANCE;

        public static Builder newInstance()
        {
            return new Builder();
        }

        public Builder mergeFrom(HttpResponse other)
        {
            checkThat(other).is(notNull());

            return this.withResponseBody(other.body())
                    .withStatusCode(other.statusCode())
                    .withResponseHeaders(other.responseHeaders());
        }

        public Builder withStatusCode(int statusCode) throws IllegalArgumentException
        {
            //TODO: Also add check that status code is in the HTTP Range
            checkThat(statusCode).is(validHttpStatusCode());

            this.statusCode = statusCode;
            return this;
        }

        public Builder withResponseHeaders(Map<String, String> responseHeaders) throws IllegalArgumentException
        {
            if (responseHeaders == null)
            {
                responseHeaders = Collections.emptyMap();
            }

            this.responseHeaders = responseHeaders;
            return this;
        }

        public Builder withResponseBody(JsonElement json) throws IllegalArgumentException
        {
            checkThat(json).is(notNull());

            this.responseBody = json;
            return this;
        }

        public Builder usingGson(Gson gson) throws IllegalArgumentException
        {
            checkThat(gson).is(notNull());

            this.gson = gson;
            return this;
        }

        public HttpResponse build() throws IllegalStateException
        {
            checkThat(statusCode)
                    .throwing(ex -> new IllegalStateException("No status code supplied"))
                    .is(validHttpStatusCode());

            checkThat(responseBody)
                    .usingMessage("missing Response Body")
                    .is(notNull());

            return new Impl(statusCode,
                            unmodifiableMap(responseHeaders),
                            gson,
                            responseBody);
        }

        //==============================================================================================
        // Implementation
        //==============================================================================================
        @Immutable
        @BuilderPattern(role = PRODUCT)
        private static class Impl implements HttpResponse
        {

            private final int statusCode;
            private final Map<String, String> responseHeaders;
            private final Gson gson;
            private final JsonElement responseBody;

            private Impl(int statusCode,
                         Map<String, String> responseHeaders,
                         Gson gson,
                         JsonElement response)
            {
                this.statusCode = statusCode;
                this.responseHeaders = responseHeaders;
                this.gson = gson;
                this.responseBody = response;
            }

            @Override
            public int statusCode()
            {
                return statusCode;
            }

            @Override
            public Map<String, String> responseHeaders()
            {
                return responseHeaders;
            }

            @Override
            public String bodyAsString()
            {
                if (responseBody.isJsonPrimitive())
                {
                    return responseBody.getAsString();
                }
                else
                {
                    return responseBody.toString();
                }
            }

            @Override
            public JsonElement body() throws JsonParseException
            {
                JsonElement copy = gson.toJsonTree(responseBody);
                return copy;
            }

            @Override
            public <T> T bodyAs(Class<T> classOfT) throws JsonParseException
            {
                checkThat(classOfT).is(validResponseClass());

                try
                {
                    T instance = gson.fromJson(responseBody, classOfT);
                    return instance;
                }
                catch (Exception ex)
                {
                    throw new JsonException("Failed to parse json to class: " + classOfT, ex);
                }
            }

            @Override
            public <T> List<T> bodyAsArrayOf(Class<T> classOfT) throws JsonException
            {
                checkThat(classOfT).is(validResponseClass());

                T[] array = (T[]) Array.newInstance(classOfT, 0);
                Class<T[]> arrayClass = (Class<T[]>) array.getClass();

                try
                {
                    array = gson.fromJson(responseBody, arrayClass);
                    return array == null ? Collections.emptyList() : Arrays.asList(array);
                }
                catch (Exception ex)
                {
                    throw new JsonException("Failed to parse json to class: " + classOfT, ex);
                }
            }

            @Override
            public int hashCode()
            {
                int hash = 3;
                hash = 41 * hash + this.statusCode;
                hash = 41 * hash + Objects.hashCode(this.responseHeaders);
                hash = 41 * hash + Objects.hashCode(this.responseBody);
                return hash;
            }

            @Override
            public boolean equals(Object obj)
            {
                if (obj == null)
                {
                    return false;
                }
                if (!(obj instanceof HttpResponse))
                {
                    return false;
                }

                return this.equals((HttpResponse) obj);
            }

            @Override
            public String toString()
            {
                return "HttpResponse{" + "statusCode=" + statusCode + ", responseHeaders=" + responseHeaders + ", response=" + responseBody + '}';
            }

        }

    }

}
