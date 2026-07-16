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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static tech.sirwellington.alchemy.http.HttpAssertions.validHttpStatusCode;
import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

/**
 * @author SirWellington
 * @see HttpResponse.Builder
 */
public interface HttpResponse
{

    /**
     * @return The HTTP Status code of the request.
     */
    int statusCode();

    /**
     * @return The response headers returned by the REST Service.
     */
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
     * @throws JsonException
     */
    JsonElement body() throws JsonException;

    /**
     * Get the Response Body as a custom POJO Type. (Plain Old Java Object)
     * Ensure that the POJO is styled in typical Java Bean/Value object style.
     * Getters and setters are not required, although {@link Object#hashCode()}
     * and {@link Object#equals(Object)} is recommended for any value type.
     *
     * @param <T>      The type of the POJO
     * @param classOfT The Class of the POJO.
     * @return An instance of {@code T}, mapped from the JSON Body.
     * @throws JsonException If the JSON Body could not be parsed.
     */
    <T> T bodyAs(Class<T> classOfT) throws JsonException;

    /**
     * Use in cases where you expect the Response Body to be a JSON Array.
     * A {@link List} is returned instead of an Array.
     *
     * @param <T>      The type of the POJO
     * @param classOfT The Class of the POJO.
     * @return A List of T, parsed from the JSON Body.
     * @throws JsonException
     */
    <T> List<T> bodyAsArrayOf(Class<T> classOfT) throws JsonException;

    /**
     * HTTP OK are 200-208 or the 226 status code.
     *
     * @return true if the status code is "OK", false otherwise.
     * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#2xx_Success">HTTP 2xx Success</a>
     */
    default boolean isOk()
    {
        int statusCode = statusCode();
        return (statusCode >= 200 && statusCode <= 208) || statusCode == 226;
    }

    /**
     * @return The {@link HttpStatusCode} corresponding to the status code.
     */
    default HttpStatusCode status()
    {
        return HttpStatusCode.forCode(statusCode());
    }

    /**
     * Whether the response corresponds to a {@link HttpStatusCode#NOT_FOUND} status code.
     */
    default boolean notFound()
    {
        return status() == HttpStatusCode.NOT_FOUND;
    }

    /**
     * Tells you whether {@code this} and {@code other} are equal to each
     * other, according to:
     * <ol>
     *   <li>The status code</li>
     *   <li>Response Headers</li>
     *   <li>Response Body</li>
     * </ol>
     */
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

    /**
     * Creates a new {@link Builder}.
     */
    static Builder builder()
    {
        return Builder.newInstance();
    }

    //==============================================================================================
    // Builder Implementation
    //==============================================================================================

    /**
     * Builder for constructing {@link HttpResponse} instances.
     */
    public static class Builder
    {

        // Start negative to make sure that status code was set
        private int statusCode = -100;
        private Map<String, String> responseHeaders = Map.of();
        private Gson gson = new GsonBuilder()
                .setDateFormat(Constants.DATE_FORMAT)
                .create();
        private JsonElement responseBody = JsonNull.INSTANCE;

        private Builder()
        {
        }

        public Builder copyFrom(HttpResponse other)
        {
            return this.withResponseBody(other.body())
                       .withStatusCode(other.statusCode())
                       .withResponseHeaders(other.responseHeaders());
        }

        /**
         * @throws IllegalArgumentException if the status code is not valid.
         */
        public Builder withStatusCode(int statusCode) throws IllegalArgumentException
        {
            Arguments.checkThat(statusCode).isA(validHttpStatusCode());

            this.statusCode = statusCode;
            return this;
        }

        public Builder withStatusCode(HttpStatusCode statusCode)
        {
            this.statusCode = statusCode.getCode();
            return this;
        }

        public Builder withResponseHeaders(Map<String, String> responseHeaders)
        {
            Map<String, String> headers = responseHeaders != null ? responseHeaders : Map.of();
            this.responseHeaders = headers;
            return this;
        }

        public Builder withResponseBody(JsonElement json)
        {
            this.responseBody = json;
            return this;
        }

        public Builder usingGson(Gson gson)
        {
            this.gson = gson;
            return this;
        }

        /**
         * Builds the {@link HttpResponse}.
         *
         * @return A new {@link HttpResponse} instance.
         * @throws IllegalStateException if the status code was not set or is invalid.
         */
        public HttpResponse build() throws IllegalStateException
        {
            Arguments.checkThat(statusCode)
                    .throwing(ex -> new IllegalStateException("Invalid status code supplied", ex))
                    .isA(validHttpStatusCode());

            return new ActualResponseObject(
                    statusCode,
                    Collections.unmodifiableMap(responseHeaders),
                    gson,
                    responseBody
            );
        }

        /**
         * Creates a new empty {@link Builder}.
         */
        public static Builder newInstance()
        {
            return new Builder();
        }

        //==============================================================================================
        // Implementation
        //==============================================================================================

        private static final class ActualResponseObject implements HttpResponse
        {

            private final int statusCode;
            private final Map<String, String> responseHeaders;
            private final Gson gson;
            private final JsonElement responseBody;

            ActualResponseObject(int statusCode,
                                 Map<String, String> responseHeaders,
                                 Gson gson,
                                 JsonElement responseBody)
            {
                this.statusCode = statusCode;
                this.responseHeaders = responseHeaders;
                this.gson = gson;
                this.responseBody = responseBody;
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
            public JsonElement body()
            {
                return responseBody;
            }

            @Override
            public <T> T bodyAs(Class<T> classOfT) throws JsonException
            {
                Arguments.checkThat(classOfT).isA(validResponseClass());

                try
                {
                    if (responseBody.isJsonPrimitive())
                    {
                        String json = responseBody.getAsString();
                        return gson.fromJson(json, classOfT);
                    }
                    else
                    {
                        return gson.fromJson(responseBody, classOfT);
                    }
                }
                catch (Exception ex)
                {
                    throw new JsonException("Failed to parse json to class: " + classOfT, ex);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> List<T> bodyAsArrayOf(Class<T> classOfT) throws JsonException
            {
                Arguments.checkThat(classOfT).isA(validResponseClass());

                Object emptyArray = Array.newInstance(classOfT, 0);
                Class<?> arrayType = emptyArray.getClass();

                try
                {
                    T[] array = (T[]) gson.fromJson(responseBody, arrayType);

                    if (array == null)
                    {
                        return List.of();
                    }

                    return Arrays.asList(array);
                }
                catch (Exception ex)
                {
                    throw new JsonException("Failed to parse json to class: " + classOfT, ex);
                }
            }

            @Override
            public boolean equals(Object obj)
            {
                if (obj instanceof HttpResponse other)
                {
                    return HttpResponse.super.equals(other);
                }
                return false;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(statusCode, responseHeaders, responseBody);
            }

            @Override
            public String toString()
            {
                return "ActualResponseObject(statusCode=" + statusCode
                        + ", responseHeaders=" + responseHeaders
                        + ", responseBody=" + responseBody + ")";
            }
        }
    }
}
