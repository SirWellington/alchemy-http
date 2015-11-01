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
package tech.sirwellington.alchemy.http.verb;

import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
final class VerbAssertions
{

    private VerbAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }

    static final AlchemyAssertion<String> validContentType()
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
}
