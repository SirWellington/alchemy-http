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

import com.google.gson.JsonElement;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.arguments.assertions.Assertions;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.not;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.sameInstanceAs;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringBeginningWith;

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
final class HttpAssertions
{

    private HttpAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }

    static AlchemyAssertion<Integer> validHttpStatusCode()
    {
        /*
         * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
         */

        return greaterThanOrEqualTo(100)
                .and(lessThanOrEqualTo(505));
    }

    /*
     * TODO: Add check to see if the class structure is that of a POJO.
     */
    static <Response> AlchemyAssertion<Class<Response>> validResponseClass()
    {
        AlchemyAssertion<Class<Response>> notNull = Assertions.<Class<Response>>notNull();

        return notNull
                .and(not(sameInstanceAs(Void.class)));
    }

    static AlchemyAssertion<HttpRequest> requestReady()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("Request missing")
                    .is(notNull());

            checkThat(request.getVerb())
                    .usingMessage("Request missing HTTP Verb")
                    .is(notNull());

            checkThat(request.getUrl())
                    .usingMessage("Request missing URL")
                    .is(notNull());

            checkThat(request.getUrl().getProtocol())
                    .is(stringBeginningWith("http"));
        };
    }

    static AlchemyAssertion<String> validContentType()
    {
        return contentType ->
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

            throw new FailedAssertionException("Not a valid JSON content Type: " + contentType);
        };
    }

    static AlchemyAssertion<HttpRequest> notNullAndHasURL()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("missing HTTP Request")
                    .is(notNull());

            checkThat(request.getUrl())
                    .usingMessage("missing request URL")
                    .is(notNull());
        };
    }

    static AlchemyAssertion<JsonElement> jsonArray()
    {
        return json ->
        {
            checkThat(json)
                    .is(notNull());

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
            checkThat(response)
                    .is(notNull());

            if (!response.isOk())
            {
                throw new FailedAssertionException("Http Response not OK. Status Code: " + response.statusCode());
            }
        };
    }
}
