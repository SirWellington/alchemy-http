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

import com.google.common.io.Resources;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import sir.wellington.alchemy.annotations.access.Internal;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.Assertions.notNull;
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

    default byte[] downloadAt(URL url) throws IOException, IllegalArgumentException
    {
        checkThat(url).is(notNull());
        return Resources.toByteArray(url);
    }

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

    class Builder
    {

        private HttpClient apacheHttpClient;
        private ExecutorService executor = MoreExecutors.newDirectExecutorService();

        static Builder newInstance()
        {
            return new Builder();
        }

        Builder withExecutorService(ExecutorService executor) throws IllegalArgumentException
        {
            checkThat(executor).is(notNull());
            this.executor = executor;
            return this;
        }

        Builder withApacheHttpClient(HttpClient apacheHttpClient) throws IllegalArgumentException
        {
            checkThat(apacheHttpClient).is(notNull());
            this.apacheHttpClient = apacheHttpClient;
            return this;
        }

        AlchemyHttpStateMachine build() throws IllegalStateException
        {
            checkThat(apacheHttpClient)
                    .usingException(ex -> new IllegalStateException("missing Apache HTTP Client"))
                    .is(notNull());

            return new AlchemyMachineImpl(apacheHttpClient, executor);
        }
    }

}
