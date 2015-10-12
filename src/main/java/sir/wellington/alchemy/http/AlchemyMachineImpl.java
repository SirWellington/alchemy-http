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

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.access.Internal;
import sir.wellington.alchemy.arguments.Arguments;
import sir.wellington.alchemy.arguments.Assertions;
import sir.wellington.alchemy.http.exceptions.AlchemyHttpException;
import sir.wellington.alchemy.http.exceptions.JsonException;
import sir.wellington.alchemy.http.operations.HttpOperation;
import sir.wellington.alchemy.http.operations.HttpRequest;
import sir.wellington.alchemy.http.operations.HttpVerb;

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

    AlchemyMachineImpl(HttpClient apacheHttpClient, ExecutorService async)
    {
        this.apacheHttpClient = apacheHttpClient;
        this.async = async;
    }

    private <ResponseType> void checkClass(Class<ResponseType> classOfResponseType)
    {
        Arguments.checkThat(classOfResponseType).is(Assertions.notNull()).usingMessage("expected class cannot be Void").is(Assertions.not(Assertions.sameInstance(Void.class)));
    }

    @Override
    public HttpOperation.Step1 begin(HttpRequest initialRequest) throws IllegalArgumentException
    {
        Arguments.checkThat(initialRequest).is(Assertions.notNull());
        HttpRequest requestCopy = HttpRequest.copyOf(initialRequest);
        LOG.debug("Beginning HTTP request {}", requestCopy);
        return new Step1Impl(this, requestCopy, new JsonParser());
    }

    @Override
    public HttpOperation.Step2 getStep2(HttpRequest request) throws IllegalArgumentException
    {
        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step2Impl(this, requestCopy);
    }

    @Override
    public <ResponseType> HttpOperation.Step3<ResponseType> getStep3(HttpRequest request, Class<ResponseType> classOfResponseType) throws IllegalArgumentException
    {
        checkClass(classOfResponseType);
        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step3Impl<>(this, requestCopy, classOfResponseType);
    }

    @Override
    public <ResponseType> HttpOperation.Step4<ResponseType> getStep4(HttpRequest request, Class<ResponseType> classOfResponseType, HttpOperation.OnSuccess<ResponseType> successCallback) throws IllegalArgumentException
    {
        checkClass(classOfResponseType);
        Arguments.checkThat(successCallback).is(Assertions.notNull());
        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step4Impl<>(this, requestCopy, classOfResponseType, successCallback);
    }

    @Override
    public <ResponseType> HttpOperation.Step5<ResponseType> getStep5(HttpRequest request, Class<ResponseType> classOfResponseType, HttpOperation.OnSuccess<ResponseType> successCallback, HttpOperation.OnFailure failureCallback)
    {
        checkClass(classOfResponseType);
        Arguments.checkThat(successCallback).is(Assertions.notNull());
        Arguments.checkThat(failureCallback).is(Assertions.notNull());
        HttpRequest requestCopy = HttpRequest.copyOf(request);
        return new Step5Impl<>(this, requestCopy, classOfResponseType, successCallback, failureCallback);
    }

    @Override
    public <ResponseType> ResponseType executeSync(HttpRequest request, Class<ResponseType> classOfResponseType)
    {
        checkClass(classOfResponseType);
        Arguments.checkThat(request).is(Assertions.notNull());
        HttpRequest requestCopy = HttpRequest.copyOf(request);
        LOG.debug("Executing synchronous HTTP Request {}", request);
        Arguments.checkThat(apacheHttpClient).is(Assertions.notNull());
        Arguments.checkThat(request).is(Assertions.notNull());
        Arguments.checkThat(classOfResponseType).is(Assertions.notNull()).is(Assertions.not(Assertions.sameInstance(Void.class)));
        LOG.debug("Running HTTP Request {}", request);
        HttpVerb verb = request.getVerb();
        Arguments.checkThat(verb).usingException((sir.wellington.alchemy.arguments.FailedAssertionException ex) -> new IllegalStateException("Request missing verb: " + request)).is(Assertions.notNull());
        HttpResponse response = null;
        try
        {
            response = verb.execute(apacheHttpClient, request);
        }
        catch (AlchemyHttpException ex)
        {
            LOG.error("Encountered HttpException when running verb {} on request {}", verb, request, ex);
            throw ex;
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute verb {} on request {}", verb, request, ex);
            throw ex;
        }
        if (response == null)
        {
            LOG.error("HTTP Verb {} returned null response", verb);
            throw new AlchemyHttpException("HTTP Verb returned null response");
        }
        LOG.debug("HTTP Request {} successfully executed: {}", request, response);
        if (classOfResponseType == HttpRequest.class)
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
                throw new JsonException(response, "Failed to marshal JSON into class of type: " + classOfResponseType, ex);
            }
        }
    }

    @Override
    public <ResponseType> void executeAsync(HttpRequest request, Class<ResponseType> classOfResponseType, HttpOperation.OnSuccess<ResponseType> successCallback, HttpOperation.OnFailure failureCallback)
    {
        LOG.debug("Executing Async HTTP Request {}", request);
        ResponseType response;
        try
        {
            response = executeSync(request, classOfResponseType);
        }
        catch (AlchemyHttpException ex)
        {
            LOG.error("Http Operation Failed", ex);
            failureCallback.handleError(ex);
            return;
        }
        catch (Exception ex)
        {
            LOG.error("Async request failed", ex);
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
    }

    @Override
    public String toString()
    {
        return "AlchemyMachineImpl{" + "apacheHttpClient=" + apacheHttpClient + ", async=" + async + '}';
    }

}
