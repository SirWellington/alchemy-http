/*
 * Copyright © 2019. Sir Wellington.
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

import java.net.URL;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

final class Step4Impl<ResponseType> implements AlchemyRequestSteps.Step4<ResponseType>
{
    private final AlchemyHttpStateMachine stateMachine;
    private final HttpRequest request;
    private final Class<ResponseType> classOfResponseType;

    Step4Impl(AlchemyHttpStateMachine stateMachine, HttpRequest request, Class<ResponseType> classOfResponseType)
    {
        Arguments.checkThat(classOfResponseType).isA(validResponseClass());

        this.stateMachine = stateMachine;
        this.request = request;
        this.classOfResponseType = classOfResponseType;
    }

    @Override
    public ResponseType at(URL url) throws AlchemyHttpException
    {
        HttpRequest newRequest = HttpRequest.Builder
                                            .from(request)
                                            .usingUrl(url)
                                            .build();

        return stateMachine.executeSync(newRequest, classOfResponseType);
    }

    @Override
    public AlchemyRequestSteps.Step5<ResponseType> onSuccess(AlchemyRequestSteps.OnSuccess<ResponseType> onSuccessCallback)
    {
        return stateMachine.jumpToStep5(request, classOfResponseType, onSuccessCallback);
    }

    @Override
    public String toString()
    {
        return "Step4Impl{stateMachine=" + stateMachine + ", request=" + request + ", classOfResponseType=" + classOfResponseType + "}";
    }
}
