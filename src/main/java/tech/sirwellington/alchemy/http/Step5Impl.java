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

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

/**
 *
 * @author SirWellington
 */
final class Step5Impl<ResponseType> implements AlchemyRequest.Step5<ResponseType>
{

    private final static Logger LOG = LoggerFactory.getLogger(Step5Impl.class);

    private final AlchemyHttpStateMachine stateMachine;
    private final HttpRequest request;
    private final Class<ResponseType> classOfResponseType;
    private final AlchemyRequest.OnSuccess<ResponseType> successCallback;

    public Step5Impl(AlchemyHttpStateMachine stateMachine,
                     HttpRequest request,
                     Class<ResponseType> classOfResponseType,
                     AlchemyRequest.OnSuccess<ResponseType> successCallback)
    {
        checkThat(stateMachine, request, classOfResponseType, successCallback)
                .are(notNull());
        
        checkThat(classOfResponseType)
                .is(validResponseClass());

        this.stateMachine = stateMachine;
        this.request = request;
        this.classOfResponseType = classOfResponseType;
        this.successCallback = successCallback;
    }

    @Override
    public AlchemyRequest.Step6<ResponseType> onFailure(AlchemyRequest.OnFailure onFailureCallback)
    {
        checkThat(onFailureCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        return stateMachine.jumpToStep6(request, classOfResponseType, successCallback, onFailureCallback);
    }

    @Override
    public String toString()
    {
        return "Step5Impl{" + "stateMachine=" + stateMachine + ", request=" + request + ", classOfResponseType=" + classOfResponseType + ", successCallback=" + successCallback + '}';
    }

}
