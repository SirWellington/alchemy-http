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

import java.util.concurrent.Executor;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveLong;
import static tech.sirwellington.alchemy.http.HttpAssertions.*;

final class AlchemyMachineImpl implements AlchemyHttpStateMachine {
    private static final Logger LOG = LoggerFactory.getLogger(AlchemyMachineImpl.class);

    private final Executor async;
    private final Gson gson;
    private final HttpRequestExecutor requestExecutor;
    private final long timeoutMillis;

    AlchemyMachineImpl(
        Executor async,
        Gson gson,
        HttpRequestExecutor requestExecutor
    ) {
        this(
            async,
            gson,
            requestExecutor,
            Constants.DEFAULT_TIMEOUT
        );
    }

    AlchemyMachineImpl(
        Executor async,
        Gson gson,
        HttpRequestExecutor requestExecutor,
        long timeoutMillis
    ) {
        checkThat(timeoutMillis)
            .isA(positiveLong());

        this.async = async;
        this.gson = gson;
        this.requestExecutor = requestExecutor;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public AlchemyRequestSteps.Step1 begin(HttpRequest initialRequest) {
        var copy = HttpRequest.copyOf(initialRequest);
        LOG.debug(
            "Beginning HTTP request {}",
            copy
        );
        return new Step1Impl(
            this,
            copy
        );
    }

    @Override
    public AlchemyRequestSteps.Step2 jumpToStep2(HttpRequest request) {
        var copy = HttpRequest.copyOf(request);
        return new Step2Impl(
            copy,
            this,
            gson
        );
    }

    @Override
    public AlchemyRequestSteps.Step3 jumpToStep3(HttpRequest request) {
        var copy = HttpRequest.copyOf(request);
        return new Step3Impl(
            this,
            copy
        );
    }

    @Override
    public <ResponseType> AlchemyRequestSteps.Step4<ResponseType> jumpToStep4(
        HttpRequest request,
        Class<ResponseType> classOfResponseType
    ) {
        checkThat(classOfResponseType)
            .isA(validResponseClass());

        var copy = HttpRequest.copyOf(request);
        return new Step4Impl<>(
            this,
            copy,
            classOfResponseType
        );
    }

    @Override
    public <ResponseType> AlchemyRequestSteps.Step5<ResponseType> jumpToStep5(
        HttpRequest request,
        Class<ResponseType> classOfResponseType,
        AlchemyRequestSteps.OnSuccess<ResponseType> successCallback
    ) {
        checkThat(classOfResponseType)
            .isA(validResponseClass());

        var copy = HttpRequest.copyOf(request);
        return new Step5Impl<>(
            this,
            copy,
            classOfResponseType,
            successCallback
        );
    }

    @Override
    public <ResponseType> AlchemyRequestSteps.Step6<ResponseType> jumpToStep6(
        HttpRequest request,
        Class<ResponseType> classOfResponseType,
        AlchemyRequestSteps.OnSuccess<ResponseType> successCallback,
        AlchemyRequestSteps.OnFailure failureCallback
    ) {
        checkThat(classOfResponseType)
            .isA(validResponseClass());

        var copy = HttpRequest.copyOf(request);
        return new Step6Impl<>(
            this,
            copy,
            classOfResponseType,
            successCallback,
            failureCallback
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <ResponseType> ResponseType executeSync(
        HttpRequest request,
        Class<ResponseType> classOfResponseType
    ) throws AlchemyHttpException {
        LOG.debug(
            "Executing synchronous HTTP Request {}",
            request
        );

        checkThat(classOfResponseType)
            .isA(validResponseClass());
        checkThat(request)
            .is(ready());

        HttpResponse response;
        try {
            response = requestExecutor.execute(
                request,
                gson,
                timeoutMillis
            );
        }
        catch (AlchemyHttpException ex) {
            throw ex;
        }
        catch (Exception ex) {
            LOG.error(
                "Failed to execute request {}",
                request,
                ex
            );
            throw new AlchemyHttpException(
                request,
                ex
            );
        }

        checkThat(response)
            .throwing(_ -> new AlchemyHttpException(
                request,
                response,
                "Http Response not OK."
            ))
            .isA(okResponse());

        LOG.trace(
            "HTTP Request {} successfully executed: {}",
            request,
            response
        );

        if (classOfResponseType == HttpResponse.class) {
            return (ResponseType) response;
        }
        else if (classOfResponseType == String.class) {
            return (ResponseType) response.bodyAsString();
        }
        else {
            LOG.trace(
                "Attempting to parse response {} as {}",
                response,
                classOfResponseType
            );
            return response.bodyAs(classOfResponseType);
        }
    }

    @Override
    public <ResponseType> void executeAsync(
        HttpRequest request,
        Class<ResponseType> classOfResponseType,
        AlchemyRequestSteps.OnSuccess<ResponseType> successCallback,
        AlchemyRequestSteps.OnFailure failureCallback
    ) {
        checkThat(request)
            .isA(ready());
        checkThat(classOfResponseType)
            .isA(validResponseClass());

        LOG.debug(
            "Submitting Async HTTP Request {}",
            request
        );

        async.execute(() ->
        {
            LOG.debug(
                "Starting Async HTTP Request {}",
                request
            );

            ResponseType response;
            try {
                response = executeSync(
                    request,
                    classOfResponseType
                );
            }
            catch (AlchemyHttpException ex) {
                LOG.trace(
                    "Async request failed",
                    ex
                );
                failureCallback.handleError(ex);
                return;
            }
            catch (Exception ex) {
                LOG.trace(
                    "Async request failed",
                    ex
                );
                failureCallback.handleError(new AlchemyHttpException(ex));
                return;
            }

            try {
                successCallback.processResponse(response);
            }
            catch (Exception ex) {
                var message = "Success Callback threw exception";
                LOG.warn(
                    message,
                    ex
                );
                failureCallback.handleError(new AlchemyHttpException(
                    message,
                    ex
                ));
            }
        });
    }

    @Override
    public String toString() {
        return "AlchemyMachineImpl(async=" + async + ", timeoutMillis=" + timeoutMillis + ")";
    }
}
