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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

final class Step3Impl implements AlchemyRequestSteps.Step3
{
    private final AlchemyHttpStateMachine stateMachine;
    private HttpRequest request;

    Step3Impl(AlchemyHttpStateMachine stateMachine, HttpRequest request)
    {
        this.stateMachine = stateMachine;
        this.request = request;
    }

    @Override
    public AlchemyRequestSteps.Step3 usingHeader(String key, String value)
    {
        Arguments.checkThat(key)
                .usingMessage("missing key")
                .isA(nonEmptyString());

        Map<String, String> newHeaders = new HashMap<>(request.requestHeaders());
        newHeaders.put(key, value);

        this.request = HttpRequest.Builder
                                  .from(request)
                                  .usingRequestHeaders(newHeaders)
                                  .build();

        return this;
    }

    @Override
    public AlchemyRequestSteps.Step3 usingQueryParam(String name, String value)
    {
        Arguments.checkThat(name)
                .usingMessage("missing name or value")
                .isA(nonEmptyString());

        Arguments.checkThat(value)
                .usingMessage("missing name or value")
                .isA(nonEmptyString());

        Map<String, String> queryParams = new HashMap<>(request.queryParams());
        queryParams.put(name, value);

        this.request = HttpRequest.Builder
                                  .from(request)
                                  .usingQueryParams(queryParams)
                                  .build();

        return this;
    }

    @Override
    public AlchemyRequestSteps.Step3 followRedirects(int maxNumberOfTimes)
    {
        Arguments.checkThat(maxNumberOfTimes).isA(greaterThanOrEqualTo(1));

        //TODO: Implement this
        //Not doing anything with this yet.

        return this;
    }

    @Override
    public HttpResponse at(URL url) throws AlchemyHttpException
    {
        HttpRequest requestCopy = HttpRequest.Builder
                                              .from(request)
                                              .usingUrl(url)
                                              .build();

        return stateMachine.executeSync(requestCopy);
    }

    @Override
    public AlchemyRequestSteps.Step5<HttpResponse> onSuccess(AlchemyRequestSteps.OnSuccess<HttpResponse> onSuccessCallback)
    {
        return stateMachine.jumpToStep5(request, HttpResponse.class, onSuccessCallback);
    }

    @Override
    public <ResponseType> AlchemyRequestSteps.Step4<ResponseType> expecting(Class<ResponseType> classOfResponseType)
    {
        Arguments.checkThat(classOfResponseType).isA(validResponseClass());

        return stateMachine.jumpToStep4(request, classOfResponseType);
    }

    @Override
    public String toString()
    {
        return "Step3Impl{request=" + request + ", stateMachine=" + stateMachine + "}";
    }
}
