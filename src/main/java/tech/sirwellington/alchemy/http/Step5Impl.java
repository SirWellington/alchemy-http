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

import tech.sirwellington.alchemy.arguments.Arguments;

import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

final class Step5Impl<ResponseType> implements AlchemyRequestSteps.Step5<ResponseType>
{
    private final AlchemyHttpStateMachine stateMachine;
    private final HttpRequest request;
    private final Class<ResponseType> classOfResponseType;
    private final AlchemyRequestSteps.OnSuccess<ResponseType> successCallback;

    Step5Impl(AlchemyHttpStateMachine stateMachine, HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequestSteps.OnSuccess<ResponseType> successCallback)
    {
        Arguments.checkThat(classOfResponseType).isA(validResponseClass());

        this.stateMachine = stateMachine;
        this.request = request;
        this.classOfResponseType = classOfResponseType;
        this.successCallback = successCallback;
    }

    @Override
    public AlchemyRequestSteps.Step6<ResponseType> onFailure(AlchemyRequestSteps.OnFailure onFailureCallback)
    {
        return stateMachine.jumpToStep6(request, classOfResponseType, successCallback, onFailureCallback);
    }

    @Override
    public String toString()
    {
        return "Step5Impl{stateMachine=" + stateMachine + ", request=" + request + ", classOfResponseType=" + classOfResponseType + ", successCallback=" + successCallback + "}";
    }
}
