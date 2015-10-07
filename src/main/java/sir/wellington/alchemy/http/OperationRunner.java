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
import sir.wellington.alchemy.http.exceptions.HttpException;
import sir.wellington.alchemy.http.operations.HttpOperation;
import sir.wellington.alchemy.http.operations.HttpRequest;
import sir.wellington.alchemy.http.operations.HttpVerb;

/**
 *
 * @author SirWellington
 */
interface OperationRunner<ResponseType> extends Callable<ResponseType>
{

    @Override
    public ResponseType call() throws Exception;

    class Sync<ResponseType> implements OperationRunner<ResponseType>
    {

        private static final Logger LOG = LoggerFactory.getLogger(Sync.class);

        private HttpRequest request;
        private HttpClient apacheHttpClient;
        private HttpVerb verb;
        Class<ResponseType> classOfResponseType;

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

    class Async<ResponseType> implements OperationRunner<Void>
    {

        private static final Logger LOG = LoggerFactory.getLogger(Async.class);

        private OperationRunner<ResponseType> synchronousDelegate;

        private ExecutorService async;
        private HttpOperation.OnSuccess<ResponseType> successCallback;
        private HttpOperation.OnFailure failureCallback;

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
