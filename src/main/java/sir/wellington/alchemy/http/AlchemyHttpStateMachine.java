/*
 * Copyright 2015 Sir Wellington.
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
package sir.wellington.alchemy.http;

import java.util.Collections;
import sir.wellington.alchemy.annotations.access.Internal;
import sir.wellington.alchemy.http.exceptions.AlchemyHttpException;
import sir.wellington.alchemy.http.operations.HttpOperation;
import sir.wellington.alchemy.http.operations.HttpOperation.Step1;
import sir.wellington.alchemy.http.operations.HttpOperation.Step2;
import sir.wellington.alchemy.http.operations.HttpOperation.Step3;
import sir.wellington.alchemy.http.operations.HttpOperation.Step4;
import sir.wellington.alchemy.http.operations.HttpOperation.Step5;
import sir.wellington.alchemy.http.operations.HttpRequest;

/**
 * This is an internal state machine for managing the transitions of an Alchemy Http Request.
 *
 * @author SirWellington
 */
@Internal
interface AlchemyHttpStateMachine
{

    default Step1 begin()
    {
        HttpRequest request = HttpRequest.Builder.newInstance()
                .usingRequestHeaders(Collections.EMPTY_MAP)
                .build();

        return begin(request);
    }

    Step1 begin(HttpRequest initialRequest);

    Step2 getStep2(HttpRequest request) throws IllegalArgumentException;

    <ResponseType> Step3<ResponseType> getStep3(HttpRequest request,
                                                Class<ResponseType> classOfResponseType) throws IllegalArgumentException;

    <ResponseType> Step4<ResponseType> getStep4(HttpRequest request,
                                                Class<ResponseType> classOfResponseType,
                                                HttpOperation.OnSuccess<ResponseType> successCallback) throws IllegalArgumentException;

    <ResponseType> Step5<ResponseType> getStep5(HttpRequest request,
                                                Class<ResponseType> classOfResponseType,
                                                HttpOperation.OnSuccess<ResponseType> successCallback,
                                                HttpOperation.OnFailure failureCallback);

    default HttpResponse executeSync(HttpRequest request) throws AlchemyHttpException
    {
        return executeSync(request, HttpResponse.class);
    }

    <ResponseType> ResponseType executeSync(HttpRequest request,
                                            Class<ResponseType> classOfResponseType) throws AlchemyHttpException;

    <ResponseType> void executeAsync(HttpRequest request,
                                     Class<ResponseType> classOfResponseType,
                                     HttpOperation.OnSuccess<ResponseType> successCallback,
                                     HttpOperation.OnFailure failureCallback);

}
