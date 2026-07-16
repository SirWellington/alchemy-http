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

import com.google.gson.JsonElement;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.BasicAssertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringBeginningWith;

/**
 * @author SirWellington
 */
final class HttpAssertions
{

    private HttpAssertions()
    {
        throw new AssertionError("non-instantiable");
    }

    static AlchemyAssertion<Integer> validHttpStatusCode()
    {
        /*
         * See https://www.whoishostingthis.com/resources/http-status-codes
         */
        return greaterThanOrEqualTo(100).and(lessThanOrEqualTo(600));
    }

    /*
     * TODO: Add check to see if the class structure is that of a POJO.
     */
    static <Response> AlchemyAssertion<Class<Response>> validResponseClass()
    {
        return klass ->
        {
            checkThat(klass).isA(notNull());

            if (klass == Void.class)
            {
                throw new FailedAssertionException("Response class cannot be Void");
            }
        };
    }

    static AlchemyAssertion<HttpRequest> ready()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("Request missing")
                    .isA(notNull());

            checkThat(request.method())
                    .usingMessage("Request missing HTTP Method")
                    .isA(notNull());

            checkThat(request.url())
                    .usingMessage("Request missing URL")
                    .isA(notNull());

            checkThat(request.url().getProtocol())
                    .isA(stringBeginningWith("http"));
        };
    }

    static AlchemyAssertion<String> validContentType()
    {
        return contentType ->
        {
            checkThat(contentType)
                    .usingMessage("missing Content-Type")
                    .isA(nonEmptyString());

            if (contentType.contains(ContentTypes.APPLICATION_JSON))
            {
                return;
            }

            if (contentType.contains(ContentTypes.PLAIN_TEXT))
            {
                return;
            }

            throw new FailedAssertionException("Not a valid JSON content Type: " + contentType);
        };
    }

    static AlchemyAssertion<HttpRequest> validRequest()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("missing HTTP Request")
                    .isA(notNull());

            checkThat(request.url())
                    .usingMessage("missing request URL")
                    .isA(notNull());
        };
    }

    static AlchemyAssertion<JsonElement> jsonArray()
    {
        return json ->
        {
            checkThat(json).isA(notNull());

            if (!json.isJsonArray())
            {
                throw new FailedAssertionException("Expecting JSON Array, instead: " + json);
            }
        };
    }

    static AlchemyAssertion<HttpResponse> okResponse()
    {
        return response ->
        {
            checkThat(response).isA(notNull());

            if (!response.isOk())
            {
                throw new FailedAssertionException("Http Response not OK. Status Code: " + response.statusCode());
            }
        };
    }
}
