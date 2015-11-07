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

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

/**
 *
 * @author SirWellington
 */
final class Step6Impl<ResponseType> implements AlchemyRequest.Step6<ResponseType>
{

    private final static Logger LOG = LoggerFactory.getLogger(Step6Impl.class);

    private final AlchemyHttpStateMachine stateMachine;
    private final HttpRequest request;
    private final Class<ResponseType> classOfResponseType;
    private final AlchemyRequest.OnSuccess<ResponseType> successCallback;
    private final AlchemyRequest.OnFailure failureCallback;

    Step6Impl(AlchemyHttpStateMachine stateMachine,
              HttpRequest request,
              Class<ResponseType> classOfResponseType,
              AlchemyRequest.OnSuccess<ResponseType> successCallback,
              AlchemyRequest.OnFailure failureCallback)
    {
        checkThat(stateMachine, request, classOfResponseType, successCallback, failureCallback)
                .are(notNull());
        
        checkThat(classOfResponseType)
                .is(validResponseClass());

        this.stateMachine = stateMachine;
        this.request = request;
        this.classOfResponseType = classOfResponseType;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    @Override
    public void at(URL url)
    {
        checkThat(url)
                .usingMessage("missing URL")
                .is(notNull());

        HttpRequest requestCopy = HttpRequest.Builder.from(request)
                .usingUrl(url)
                .build();

        stateMachine.executeAsync(requestCopy,
                                  classOfResponseType,
                                  successCallback,
                                  failureCallback);
    }

    @Override
    public String toString()
    {
        return "Step6Impl{" + "stateMachine=" + stateMachine + ", request=" + request + ", classOfResponseType=" + classOfResponseType + ", successCallback=" + successCallback + ", failureCallback=" + failureCallback + '}';
    }

}
