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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.access.Internal;
import sir.wellington.alchemy.annotations.patterns.DecoratorPattern;
import static sir.wellington.alchemy.annotations.patterns.DecoratorPattern.Role.CONCRETE_COMPONENT;
import static sir.wellington.alchemy.annotations.patterns.DecoratorPattern.Role.CONCRETE_DECORATOR;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.assertions.Assertions.notNull;
import sir.wellington.alchemy.http.exceptions.HttpException;
import sir.wellington.alchemy.http.operations.HttpOperation;
import sir.wellington.alchemy.http.operations.HttpOperation.OnFailure;
import sir.wellington.alchemy.http.operations.HttpOperation.OnSuccess;
import sir.wellington.alchemy.http.operations.HttpRequest;
import sir.wellington.alchemy.http.operations.HttpVerb;

/**
 *
 * @author SirWellington
 */
@Internal
interface OperationExecutor<ResponseType> extends Callable<ResponseType>
{

    @Override
    public ResponseType call() throws Exception;

    static <ResponseType> OperationExecutor<ResponseType> newSyncRunner(HttpClient apacheHttpClient,
                                                                      HttpRequest request,
                                                                      HttpVerb verb,
                                                                      Class<ResponseType> classOfResponseType)
    {
        checkThat(apacheHttpClient).is(notNull());
        checkThat(request).is(notNull());
        checkThat(verb).is(notNull());
        checkThat(classOfResponseType).is(notNull());

        return new Sync(request, apacheHttpClient, verb, classOfResponseType);
    }

    static <ResponseType> OperationExecutor<Void> newAsyncRunner(HttpClient apacheHttpClient,
                                                               HttpRequest request,
                                                               HttpVerb verb,
                                                               Class<ResponseType> classOfResponseType,
                                                               OnSuccess<ResponseType> successCallback,
                                                               OnFailure failureCallback,
                                                               ExecutorService executorService)
    {
        OperationExecutor<ResponseType> syncRunner = newSyncRunner(apacheHttpClient, request, verb, classOfResponseType);

        return new Async<>(syncRunner, executorService, successCallback, failureCallback);
    }

    @DecoratorPattern(role = CONCRETE_COMPONENT)
    class Sync<ResponseType> implements OperationExecutor<ResponseType>
    {

        private static final Logger LOG = LoggerFactory.getLogger(Sync.class);

        private final HttpRequest request;
        private final HttpClient apacheHttpClient;
        private final HttpVerb verb;
        private final Class<ResponseType> classOfResponseType;

        Sync(HttpRequest request, HttpClient apacheHttpClient, HttpVerb verb, Class<ResponseType> classOfResponseType)
        {
            this.request = request;
            this.apacheHttpClient = apacheHttpClient;
            this.verb = verb;
            this.classOfResponseType = classOfResponseType;
        }

        @Override
        public ResponseType call() throws Exception
        {
            LOG.debug("Running HTTP Request {}", request);

            HttpResponse response = null;

            try
            {
                response = verb.execute(apacheHttpClient, request);
            }
            catch (HttpException ex)
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
                throw new HttpException("HTTP Verb returned null response");
            }

            LOG.debug("HTTP Request {} successfully executed: {}", request, response);
            if (clientWants(HttpResponse.class))
            {
                return (ResponseType) response;
            }
            else if (clientWants(String.class))
            {
                return (ResponseType) response.asString();
            }
            else
            {
                return response.as(classOfResponseType);
            }
        }

        private boolean clientWants(Class<?> aClass)
        {
            return this.classOfResponseType == aClass;
        }

    }

    @DecoratorPattern(role = CONCRETE_DECORATOR)
    class Async<ResponseType> implements OperationExecutor<Void>
    {

        private static final Logger LOG = LoggerFactory.getLogger(Async.class);

        private final OperationExecutor<ResponseType> synchronousDelegate;

        private final ExecutorService async;
        private final HttpOperation.OnSuccess<ResponseType> successCallback;
        private final HttpOperation.OnFailure failureCallback;

        Async(OperationExecutor<ResponseType> synchronousDelegate,
              ExecutorService async,
              HttpOperation.OnSuccess<ResponseType> successCallback,
              HttpOperation.OnFailure failureCallback)
        {
            this.synchronousDelegate = synchronousDelegate;
            this.async = async;
            this.successCallback = successCallback;
            this.failureCallback = failureCallback;
        }

        @Override
        public Void call() throws Exception
        {
            async.submit(this::run);

            return null;
        }

        private void run()
        {
            LOG.debug("Starting Async HTTP Request using delegate {}", synchronousDelegate);

            ResponseType response;

            try
            {
                response = synchronousDelegate.call();
            }
            catch (HttpException ex)
            {
                LOG.error("Http Operation Failed", ex);
                failureCallback.handleError(ex);
                return;
            }
            catch (Exception ex)
            {
                LOG.error("Async request failed", ex);
                failureCallback.handleError(new HttpException(ex));
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
                failureCallback.handleError(new HttpException(message, ex));
            }
        }

    }
}
