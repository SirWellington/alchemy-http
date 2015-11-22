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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@Internal
@BuilderPattern(role = PRODUCT)
@ThreadSafe
final class AlchemyHttpImpl implements AlchemyHttp
{
    @Immutable
    private final Map<String, String> defaultHeaders;
    private final AlchemyHttpStateMachine stateMachine;

    AlchemyHttpImpl(Map<String, String> defaultHeaders, AlchemyHttpStateMachine stateMachine)
    {
        checkThat(defaultHeaders, stateMachine)
                .are(notNull());

        this.defaultHeaders = ImmutableMap.copyOf(defaultHeaders);
        this.stateMachine = stateMachine;
    }

    @Override
    public AlchemyHttp usingDefaultHeader(String key, String value)
    {
        checkThat(key)
                .usingMessage("Key is empty")
                .is(nonEmptyString());

        Map<String, String> copy = Maps.newHashMap(defaultHeaders);
        copy.put(key, value);
        return new AlchemyHttpImpl(copy, stateMachine);
    }

    @Override
    public AlchemyRequest.Step1 go()
    {
        HttpRequest initialRequest = HttpRequest.Builder.newInstance()
                .usingRequestHeaders(defaultHeaders)
                .build();
        return stateMachine.begin(initialRequest);
    }

    @Override
    public String toString()
    {
        return "AlchemyHttp{" + "defaultHeaders=" + defaultHeaders + ", stateMachine=" + stateMachine + '}';
    }

    @Override
    public Map<String, String> getDefaultHeaders()
    {
        //Already immutable
        return defaultHeaders;
    }

}
