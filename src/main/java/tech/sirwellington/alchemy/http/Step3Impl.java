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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.STEP;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;
import static tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass;

/**
 *
 * @author SirWellington
 */
@StepMachineDesign(role = STEP)
final class Step3Impl implements AlchemyRequest.Step3
{

    private final static Logger LOG = LoggerFactory.getLogger(Step3Impl.class);

    private HttpRequest request;
    private final AlchemyHttpStateMachine stateMachine;

    Step3Impl(AlchemyHttpStateMachine stateMachine, HttpRequest request)
    {
        checkThat(stateMachine, request)
                .are(notNull());

        this.stateMachine = stateMachine;
        this.request = request;
    }

    @Override
    public AlchemyRequest.Step3 usingHeader(String key, String value) throws IllegalArgumentException
    {
        checkThat(key)
                .usingMessage("missing key")
                .is(nonEmptyString());

        //Value of an HTTP Header can be empty ?
        value = Strings.nullToEmpty(value);

        Map<String, String> requestHeaders = Maps.create();

        //Keep existing headers
        if (request.getRequestHeaders() != null)
        {
            requestHeaders.putAll(request.getRequestHeaders());
        }

        requestHeaders.put(key, value);

        this.request = HttpRequest.Builder.from(request)
                .usingRequestHeaders(requestHeaders)
                .build();

        return this;
    }

    @Override
    public AlchemyRequest.Step3 usingQueryParam(String name, String value) throws IllegalArgumentException
    {
        checkThat(name, value)
                .usingMessage("missing name or value")
                .are(nonEmptyString());

        Map<String, String> queryParams = Maps.create();

        //Keep existing Query Params
        if (request.getQueryParams() != null)
        {
            queryParams.putAll(request.getQueryParams());
        }

        queryParams.put(name, value);

        request = HttpRequest.Builder.from(request)
                .usingQueryParams(queryParams)
                .build();

        return this;
    }

    @Override
    public AlchemyRequest.Step3 followRedirects(int maxNumberOfTimes) throws IllegalArgumentException
    {
        checkThat(maxNumberOfTimes).is(greaterThanOrEqualTo(1));
        //TODO:
        //Not doing anything with this yet.
        return this;
    }

    @Override
    public HttpResponse at(URL url) throws AlchemyHttpException
    {
        checkThat(url).is(notNull());

        //Ready to do a sync request
        HttpRequest requestCopy = HttpRequest.Builder.from(request)
                .usingUrl(url)
                .build();

        //Tell the Step Machine to perform a Sync Request
        return stateMachine.executeSync(requestCopy);
    }

    @Override
    public AlchemyRequest.Step5<HttpResponse> onSuccess(AlchemyRequest.OnSuccess<HttpResponse> onSuccessCallback)
    {
        checkThat(onSuccessCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        return stateMachine.jumpToStep5(request, HttpResponse.class, onSuccessCallback);
    }

    @Override
    public <ResponseType> AlchemyRequest.Step4<ResponseType> expecting(Class<ResponseType> classOfResponseType) throws IllegalArgumentException
    {
        checkThat(classOfResponseType)
                .is(validResponseClass());

        return stateMachine.jumpToStep4(request, classOfResponseType);
    }

    @Override
    public String toString()
    {
        return "Step3Impl{" + "request=" + request + ", stateMachine=" + stateMachine + '}';
    }

}
