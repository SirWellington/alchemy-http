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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.concurrency.Immutable;
import sir.wellington.alchemy.annotations.concurrency.ThreadSafe;
import sir.wellington.alchemy.annotations.patterns.FluidAPIPattern;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.Assertions.notNull;
import static sir.wellington.alchemy.collections.maps.MapOperations.nullToEmpty;
import sir.wellington.alchemy.http.exceptions.HttpException;
import sir.wellington.alchemy.http.operations.HttpOperation;
import sir.wellington.alchemy.http.operations.HttpVerb;

/**
 *
 * @author SirWellington
 */
@Immutable
@ThreadSafe
@FluidAPIPattern
class HttpOperationImpl<ResponseType, CallbackResponseType> implements HttpOperation<ResponseType>
{

    private final static Logger LOG = LoggerFactory.getLogger(HttpOperationImpl.class);

    private final Map<String, String> requestHeaders;
    private final Class<ResponseType> classOfResponseType;
    private final Class<CallbackResponseType> classOfCallbackResponseType;
    private final OnSuccess<CallbackResponseType> successCallback;
    private final OnFailure failureCallback;
    private URL url;
    private HttpClient httpClient;
    private ExecutorService async;

    HttpOperationImpl(Class<ResponseType> classOfResponseType,
                      Class<CallbackResponseType> classOfCallbackResponseType,
                      Map<String, String> initialRequestHeaders,
                      OnSuccess<CallbackResponseType> successCallback,
                      OnFailure failureCallback)
    {
        initialRequestHeaders = nullToEmpty(initialRequestHeaders);

        this.requestHeaders = ImmutableMap.copyOf(initialRequestHeaders);
        this.classOfResponseType = classOfResponseType;
        this.classOfCallbackResponseType = classOfCallbackResponseType;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    @Override
    public HttpOperation<ResponseType> usingHeader(String key, String value)
    {
        Map<String, String> newRequestHeaders = Maps.newHashMap(this.requestHeaders);
        newRequestHeaders.put(key, value);

        return new HttpOperationImpl<>(classOfResponseType,
                                       classOfCallbackResponseType,
                                       newRequestHeaders,
                                       successCallback,
                                       failureCallback);
    }

    @Override
    public <NewType> HttpOperation<NewType> expecting(Class<NewType> classOfNewType)
    {
        checkThat(classOfNewType).is(notNull());

        return new HttpOperationImpl<>(classOfNewType,
                                       classOfCallbackResponseType,
                                       requestHeaders,
                                       null,
                                       null);
    }

    @Override
    public HttpOperation<Void> onSuccess(OnSuccess<ResponseType> onSuccessCallback)
    {
        checkThat(onSuccessCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        return new HttpOperationImpl<>(Void.class,
                                       classOfResponseType,
                                       this.requestHeaders,
                                       onSuccessCallback,
                                       failureCallback);
    }

    @Override
    public HttpOperation<Void> onFailure(OnFailure onFailureCallback)
    {
        checkThat(onFailureCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        return new HttpOperationImpl<>(Void.class,
                                       classOfCallbackResponseType,
                                       this.requestHeaders,
                                       successCallback,
                                       onFailureCallback);
    }

    @Override
    public ResponseType get() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType post() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType post(String jsonString) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType post(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType put() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType put(String jsonString) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType put(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType delete() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType delete(String jsonString) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType delete(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType customVerb(HttpVerb verb) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean isAsync()
    {
        return failureCallback != null || successCallback != null;
    }

}
