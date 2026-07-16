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

import com.google.gson.Gson;
import java.util.concurrent.Executor;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveLong;

interface AlchemyHttpStateMachine
{
    default AlchemyRequestSteps.Step1 begin()
    {
        HttpRequest request = HttpRequest.Builder.newInstance().build();
        return begin(request);
    }

    AlchemyRequestSteps.Step1 begin(HttpRequest initialRequest);
    AlchemyRequestSteps.Step2 jumpToStep2(HttpRequest request);
    AlchemyRequestSteps.Step3 jumpToStep3(HttpRequest request);
    <ResponseType> AlchemyRequestSteps.Step4<ResponseType> jumpToStep4(HttpRequest request, Class<ResponseType> classOfResponseType);
    <ResponseType> AlchemyRequestSteps.Step5<ResponseType> jumpToStep5(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequestSteps.OnSuccess<ResponseType> successCallback);
    <ResponseType> AlchemyRequestSteps.Step6<ResponseType> jumpToStep6(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequestSteps.OnSuccess<ResponseType> successCallback, AlchemyRequestSteps.OnFailure failureCallback);

    default HttpResponse executeSync(HttpRequest request) throws AlchemyHttpException
    {
        return executeSync(request, HttpResponse.class);
    }

    <ResponseType> ResponseType executeSync(HttpRequest request, Class<ResponseType> classOfResponseType) throws AlchemyHttpException;
    <ResponseType> void executeAsync(HttpRequest request, Class<ResponseType> classOfResponseType, AlchemyRequestSteps.OnSuccess<ResponseType> successCallback, AlchemyRequestSteps.OnFailure failureCallback);

    class Builder
    {
        private Executor executor = SynchronousExecutor.newInstance();
        private Gson gson = Constants.DEFAULT_GSON;
        private HttpRequestExecutor requestExecutor = HttpRequestExecutorImpl.create();
        private long timeout = Constants.DEFAULT_TIMEOUT;

        Builder usingExecutorService(Executor executor) { this.executor = executor; return this; }
        Builder usingGson(Gson gson) { this.gson = gson; return this; }
        Builder usingTimeout(long timeoutMillis)
        {
            Arguments.checkThat(timeoutMillis).isA(positiveLong());
            this.timeout = timeoutMillis;
            return this;
        }

        AlchemyHttpStateMachine build() { return new AlchemyMachineImpl(executor, gson, requestExecutor, timeout); }

        static Builder newInstance() { return new Builder(); }
    }
}
