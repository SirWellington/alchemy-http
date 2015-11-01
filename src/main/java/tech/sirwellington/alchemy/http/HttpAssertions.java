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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.arguments.Assertions;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.Assertions.lessThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.Assertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Assertions.not;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.Assertions.sameInstance;
import static tech.sirwellington.alchemy.arguments.Assertions.stringThatStartsWith;

/**
 *
 * @author SirWellington
 */
@Internal
final class HttpAssertions
{

    private HttpAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate class");
    }

    private final static Logger LOG = LoggerFactory.getLogger(HttpAssertions.class);

    static final AlchemyAssertion<Integer> validHttpStatusCode()
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
    static final <Response> AlchemyAssertion<Class<Response>> validResponseClass()
    {
        AlchemyAssertion<Class<Response>> notNull = Assertions.<Class<Response>>notNull();

        return notNull
                .and(not(sameInstance(Void.class)));
    }

    static final AlchemyAssertion<HttpRequest> requestReady()
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
                    .is(stringThatStartsWith("http"));
        };
    }

}
