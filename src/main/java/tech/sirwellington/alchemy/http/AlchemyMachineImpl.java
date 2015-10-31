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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnFailure;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;

import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

import static tech.sirwellington.alchemy.http.InternalAssertions.requestReady;
import static tech.sirwellington.alchemy.http.InternalAssertions.validResponseClass;

/**
 *
 * @author SirWellington
 */
@Internal
final class AlchemyMachineImpl implements AlchemyHttpStateMachine
{

    private final static Logger LOG = LoggerFactory.getLogger(AlchemyMachineImpl.class);

    private final HttpClient apacheHttpClient;
    private final ExecutorService async;
    private final Gson gson;

    AlchemyMachineImpl(HttpClient apacheHttpClient, ExecutorService async, Gson gson)
    {
        checkThat(apacheHttpClient, async, gson)
                .are(notNull());

        this.apacheHttpClient = apacheHttpClient;
        this.async = async;
        this.gson = gson;
    }

    @Override
    public AlchemyRequest.Step1 begin(HttpRequest initialRequest) throws IllegalArgumentException
    {
        checkThat(initialRequest).is(notNull());

        HttpRequest requestCopy = HttpRequest.copyOf(initialRequest);
        LOG.debug("Beginning HTTP request {}", requestCopy);
        return new Step1Impl(this, requestCopy);
    }

    @Override
    public AlchemyRequest.Step2 jumpToStep2(HttpRequest request) throws IllegalArgumentException
    {
        checkThat(request).is(notNull());

        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step2Impl(requestCopy, this, gson);
    }

    @Override
    public AlchemyRequest.Step3 jumpToStep3(HttpRequest request) throws IllegalArgumentException
    {
        checkThat(request).is(notNull());

        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step3Impl(this, requestCopy);
    }

    @Override
    public <ResponseType> AlchemyRequest.Step4<ResponseType> jumpToStep4(HttpRequest request,
                                                                         Class<ResponseType> classOfResponseType) throws IllegalArgumentException
    {
        checkThat(classOfResponseType).is(validResponseClass());

        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step4Impl<>(this, requestCopy, classOfResponseType);
    }

    @Override
    public <ResponseType> AlchemyRequest.Step5<ResponseType> jumpToStep5(HttpRequest request,
                                                                         Class<ResponseType> classOfResponseType,
                                                                         OnSuccess<ResponseType> successCallback) throws IllegalArgumentException
    {
        checkThat(classOfResponseType).is(validResponseClass());

        checkThat(request, successCallback)
                .are(notNull());

        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step5Impl<>(this, requestCopy, classOfResponseType, successCallback);
    }

    @Override
    public <ResponseType> AlchemyRequest.Step6<ResponseType> jumpToStep6(HttpRequest request,
                                                                         Class<ResponseType> classOfResponseType,
                                                                         OnSuccess<ResponseType> successCallback,
                                                                         OnFailure failureCallback)
    {
        checkThat(classOfResponseType).is(validResponseClass());

        checkThat(request, successCallback, failureCallback)
                .are(notNull());

        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step6Impl<>(this, requestCopy, classOfResponseType, successCallback, failureCallback);
    }

    @Override
    public <ResponseType> ResponseType executeSync(HttpRequest request, Class<ResponseType> classOfResponseType)
    {
        LOG.debug("Executing synchronous HTTP Request {}", request);

        checkThat(classOfResponseType).is(validResponseClass());

        checkThat(request)
                .is(notNull())
                .is(requestReady());

        checkThat(apacheHttpClient).is(notNull());

        HttpVerb verb = request.getVerb();
        checkThat(verb)
                .throwing(ex -> new IllegalStateException("Request missing verb: " + request)).
                is(notNull());

        HttpResponse response = null;
        try
        {
            response = verb.execute(apacheHttpClient, request);
        }
        catch (AlchemyHttpException ex)
        {
            LOG.info("Encountered AlchemyHttpException when running verb {} on request {}", verb, request, ex);
            throw ex;
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute verb {} on request {}", verb, request, ex);
            throw new AlchemyHttpException(request, ex);
        }

        if (response == null)
        {
            LOG.error("HTTP Verb {} returned null response", verb);
            throw new AlchemyHttpException(request, "HTTP Verb returned null response");
        }

        LOG.debug("HTTP Request {} successfully executed: {}", request, response);

        if (!response.isOk())
        {
            throw new AlchemyHttpException(request, response, "Http Response not OK. Status Code: " + response.statusCode());
        }

        if (classOfResponseType == HttpResponse.class)
        {
            return (ResponseType) response;
        }
        else if (classOfResponseType == String.class)
        {
            return (ResponseType) response.asString();
        }
        else
        {
            LOG.trace("Attempting to parse response {} as {}", response, classOfResponseType);
            try
            {
                return response.as(classOfResponseType);
            }
            catch (JsonParseException ex)
            {
                throw new JsonException(request, response, "Failed to marshal JSON into class of type: " + classOfResponseType, ex);
            }
        }
    }

    @Override
    public <ResponseType> void executeAsync(HttpRequest request,
                                            Class<ResponseType> classOfResponseType,
                                            OnSuccess<ResponseType> successCallback,
                                            OnFailure failureCallback)
    {
        checkThat(request)
                .is(notNull())
                .is(requestReady());

        checkThat(classOfResponseType)
                .is(validResponseClass());

        checkThat(successCallback, failureCallback)
                .are(notNull());

        LOG.debug("Submitting Async HTTP Request {}", request);

        Runnable asyncTask = () ->
        {
            LOG.debug("Starting Async HTTP Request {}", request);

            ResponseType response;
            try
            {
                response = executeSync(request, classOfResponseType);

                checkThat(response)
                        .throwing(ex -> new IllegalStateException("Received unexpected null response"))
                        .is(notNull());
            }
            catch (Exception ex)
            {
                LOG.trace("Async request failed", ex);
                failureCallback.handleError(new AlchemyHttpException(ex));
                return;
            }

            try
            {
                successCallback.processResponse(response);
            }
            catch (Exception ex)
            {
                String message = "Success Callback threw exception";
                LOG.warn(message, ex);
                failureCallback.handleError(new AlchemyHttpException(message, ex));
            }
        };

        async.submit(asyncTask);
    }

    @Override
    public String toString()
    {
        return "AlchemyMachineImpl{" + "apacheHttpClient=" + apacheHttpClient + ", async=" + async + ", gson=" + gson + '}';
    }

}
