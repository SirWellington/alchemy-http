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

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step1;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step2;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step3;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step4;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step5;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step6;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.MACHINE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This is an internal state machine for managing the transitions of an Alchemy Http Request.
 *
 * @author SirWellington
 */
@Internal
@StepMachineDesign(role = MACHINE)
interface AlchemyHttpStateMachine
{

    default Step1 begin()
    {
        HttpRequest request = HttpRequest.Builder.newInstance()
                .build();

        return begin(request);
    }

    Step1 begin(HttpRequest initialRequest);

    Step2 jumpToStep2(HttpRequest request) throws IllegalArgumentException;

    Step3 jumpToStep3(HttpRequest request) throws IllegalArgumentException;

    <ResponseType> Step4<ResponseType> jumpToStep4(HttpRequest request,
                                                   Class<ResponseType> classOfResponseType) throws IllegalArgumentException;

    <ResponseType> Step5<ResponseType> jumpToStep5(HttpRequest request,
                                                   Class<ResponseType> classOfResponseType,
                                                   AlchemyRequest.OnSuccess<ResponseType> successCallback) throws IllegalArgumentException;

    <ResponseType> Step6<ResponseType> jumpToStep6(HttpRequest request,
                                                   Class<ResponseType> classOfResponseType,
                                                   AlchemyRequest.OnSuccess<ResponseType> successCallback,
                                                   AlchemyRequest.OnFailure failureCallback);

    default HttpResponse executeSync(HttpRequest request) throws AlchemyHttpException
    {
        return executeSync(request, HttpResponse.class);
    }

    <ResponseType> ResponseType executeSync(HttpRequest request,
                                            Class<ResponseType> classOfResponseType) throws AlchemyHttpException;

    <ResponseType> void executeAsync(HttpRequest request,
                                     Class<ResponseType> classOfResponseType,
                                     AlchemyRequest.OnSuccess<ResponseType> successCallback,
                                     AlchemyRequest.OnFailure failureCallback);

    class Builder
    {

        private HttpClient apacheHttpClient;
        private ExecutorService executor = MoreExecutors.newDirectExecutorService();
        private Gson gson = Constants.getDefaultGson();

        static Builder newInstance()
        {
            return new Builder();
        }

        Builder usingExecutorService(ExecutorService executor) throws IllegalArgumentException
        {
            checkThat(executor).is(notNull());

            this.executor = executor;
            return this;
        }

        Builder usingApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
        {
            checkThat(apacheHttpClient).is(notNull());

            this.apacheHttpClient = apacheHttpClient;
            return this;
        }
        
        Builder usingGson(@Required Gson gson) throws IllegalArgumentException
        {
            checkThat(gson).is(notNull());
            
            this.gson = gson;
            return this;
        }

        AlchemyHttpStateMachine build() throws IllegalStateException
        {
            checkThat(apacheHttpClient)
                    .throwing(ex -> new IllegalStateException("missing Apache HTTP Client"))
                    .is(notNull());

            return new AlchemyMachineImpl(apacheHttpClient, executor, gson);
        }
    }

}
