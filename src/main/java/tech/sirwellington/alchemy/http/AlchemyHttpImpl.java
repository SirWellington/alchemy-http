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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import tech.sirwellington.alchemy.arguments.Arguments;

import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

final class AlchemyHttpImpl implements AlchemyHttp
{
    private final Map<String, String> defaultHeaders;
    private final AlchemyHttpStateMachine stateMachine;

    AlchemyHttpImpl(Map<String, String> defaultHeaders, AlchemyHttpStateMachine stateMachine)
    {
        this.defaultHeaders = Collections.unmodifiableMap(new HashMap<>(defaultHeaders));
        this.stateMachine = stateMachine;
    }

    @Override
    public Map<String, String> getDefaultHeaders()
    {
        return defaultHeaders;
    }

    @Override
    public AlchemyHttp usingDefaultHeader(String key, String value)
    {
        Arguments.checkThat(key)
                .usingMessage("Key is empty")
                .isA(nonEmptyString());

        var copy = new HashMap<>(defaultHeaders);
        copy.put(key, value);

        return new AlchemyHttpImpl(copy, stateMachine);
    }

    @Override
    public AlchemyRequestSteps.Step1 go()
    {
        var initialRequest = HttpRequest.Builder
                                                  .newInstance()
                                                  .usingRequestHeaders(defaultHeaders)
                                                  .build();

        return stateMachine.begin(initialRequest);
    }

    @Override
    public String toString()
    {
        return "AlchemyHttp{defaultHeaders=" + defaultHeaders + ", stateMachine=" + stateMachine + "}";
    }
}
