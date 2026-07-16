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

final class Step1Impl implements AlchemyRequestSteps.Step1
{
    private final AlchemyHttpStateMachine stateMachine;
    private final HttpRequest request;

    Step1Impl(AlchemyHttpStateMachine stateMachine, HttpRequest request)
    {
        this.stateMachine = stateMachine;
        this.request = request;
    }

    @Override
    public AlchemyRequestSteps.Step3 get()
    {
        HttpRequest newRequest = HttpRequest.Builder
                                            .from(request)
                                            .usingRequestMethod(RequestMethod.GET)
                                            .build();

        return stateMachine.jumpToStep3(newRequest);
    }

    @Override
    public AlchemyRequestSteps.Step2 post()
    {
        HttpRequest newRequest = HttpRequest.Builder
                                            .from(request)
                                            .usingRequestMethod(RequestMethod.POST)
                                            .build();

        return stateMachine.jumpToStep2(newRequest);
    }

    @Override
    public AlchemyRequestSteps.Step2 put()
    {
        HttpRequest newRequest = HttpRequest.Builder
                                            .from(request)
                                            .usingRequestMethod(RequestMethod.PUT)
                                            .build();

        return stateMachine.jumpToStep2(newRequest);
    }

    @Override
    public AlchemyRequestSteps.Step2 delete()
    {
        HttpRequest newRequest = HttpRequest.Builder
                                            .from(request)
                                            .usingRequestMethod(RequestMethod.DELETE)
                                            .build();

        return stateMachine.jumpToStep2(newRequest);
    }

    @Override
    public AlchemyRequestSteps.Step2 method(RequestMethod requestMethod)
    {
        HttpRequest newRequest = HttpRequest.Builder
                                            .from(request)
                                            .usingRequestMethod(requestMethod)
                                            .build();

        return stateMachine.jumpToStep2(newRequest);
    }

    @Override
    public String toString()
    {
        return "Step1Impl{stateMachine=" + stateMachine + ", request=" + request + "}";
    }
}
