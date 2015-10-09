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

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.concurrency.Immutable;
import sir.wellington.alchemy.annotations.concurrency.ThreadSafe;
import sir.wellington.alchemy.annotations.patterns.FluidAPIPattern;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.assertions.Assertions.nonEmptyString;
import static sir.wellington.alchemy.arguments.assertions.Assertions.not;
import static sir.wellington.alchemy.arguments.assertions.Assertions.notNull;
import static sir.wellington.alchemy.arguments.assertions.Assertions.sameInstance;
import sir.wellington.alchemy.http.exceptions.HttpException;
import sir.wellington.alchemy.http.exceptions.JsonParseException;
import sir.wellington.alchemy.http.operations.HttpOperation.OnFailure;
import sir.wellington.alchemy.http.operations.HttpOperation.OnSuccess;
import sir.wellington.alchemy.http.operations.HttpOperation.Step1;
import sir.wellington.alchemy.http.operations.HttpOperation.Step2;
import sir.wellington.alchemy.http.operations.HttpOperation.Step3;
import sir.wellington.alchemy.http.operations.HttpRequest;
import sir.wellington.alchemy.http.operations.HttpVerb;

/**
 *
 * @author SirWellington
 */
@Immutable
@ThreadSafe
@FluidAPIPattern
class HttpOperationImpl<ResponseType, CallbackResponseType> implements Step1<ResponseType>,
                                                                       Step2<ResponseType>,
                                                                       Step3<ResponseType>
{

    private final static Logger LOG = LoggerFactory.getLogger(HttpOperationImpl.class);
    private final static Gson GSON = new Gson();

    private Class<ResponseType> classOfResponseType;
    private Class<CallbackResponseType> classOfCallbackResponseType;

    private OnSuccess<CallbackResponseType> successCallback;
    private OnFailure failureCallback;

    private HttpRequest request;
    private HttpClient httpClient;
    private ExecutorService async;

    private HttpOperationImpl()
    {

    }

    HttpOperationImpl(Class<ResponseType> classOfResponseType,
                      Class<CallbackResponseType> classOfCallbackResponseType,
                      OnSuccess<CallbackResponseType> successCallback,
                      OnFailure failureCallback,
                      HttpRequest request,
                      HttpClient httpClient,
                      ExecutorService async)
    {
        this.classOfResponseType = classOfResponseType;
        this.classOfCallbackResponseType = classOfCallbackResponseType;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
        this.request = request;
        this.httpClient = httpClient;
        this.async = async;
    }

    @Override
    public Step1<ResponseType> usingHeader(String key, String value)
    {
        Map<String, String> newRequestHeaders = Maps.newHashMap(request.getRequestHeaders());
        newRequestHeaders.put(key, value);

        return new HttpOperationImpl<>(classOfResponseType,
                                       classOfCallbackResponseType,
                                       successCallback,
                                       failureCallback,
                                       request,
                                       httpClient,
                                       async);
    }

    @Override
    public <NewType> Step1<NewType> expecting(Class<NewType> classOfNewType)
    {
        checkThat(classOfNewType).is(notNull());

        checkThat(classOfNewType)
                .usingMessage("Cannot expect Void")
                .is(not(sameInstance(Void.class)));

        return new HttpOperationImpl<>(classOfNewType,
                                       Void.class,
                                       null,
                                       null,
                                       request,
                                       httpClient,
                                       async);
    }

    private boolean isAsync()
    {
        return failureCallback != null || successCallback != null;
    }

    @Override
    public Step1<ResponseType> followRedirects(int maxNumberOfTimes) throws IllegalArgumentException
    {
        return this;
    }

    @Override
    public Step2<ResponseType> then()
    {
        return this;
    }

    @Override
    public ResponseType post(String jsonBody) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType post(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType put(String jsonBody) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType put(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType delete(String jsonBody) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType delete(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public ResponseType put() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType delete() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResponseType customVerb(HttpVerb verb) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static <S, A> HttpOperationImpl<S, A> copyFrom(HttpOperationImpl<S, A> other)
    {
        HttpOperationImpl<S, A> instance = new HttpOperationImpl<>();
        instance.async = other.async;
        instance.classOfCallbackResponseType = other.classOfCallbackResponseType;
        instance.classOfResponseType = other.classOfResponseType;
        instance.failureCallback = other.failureCallback;
        instance.successCallback = other.successCallback;
        instance.request = other.request;
        instance.httpClient = other.httpClient;

        return instance;
    }

    @Override
    public Step3<ResponseType> onSuccess(OnSuccess<ResponseType> onSuccessCallback)
    {
        checkThat(onSuccessCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        checkThat(this.successCallback)
                .usingMessage("Only one callback is allowed per request")
                .is(not(notNull()));

        return new HttpOperationImpl<>(classOfResponseType, classOfResponseType, onSuccessCallback, failureCallback, request, httpClient, async);

    }

    @Override
    public Step2<Void> onFailure(OnFailure onFailureCallback)
    {
        return new HttpOperationImpl<>(Void.class, classOfCallbackResponseType, successCallback, onFailureCallback, request, httpClient, async);
    }

    private Step2<ResponseType> body(String jsonString) throws IllegalArgumentException
    {
        checkThat(jsonString)
                .usingMessage("jsonString is empty")
                .is(nonEmptyString());

        this.request = HttpRequest.Builder.from(request)
                .usingBody(GSON.toJsonTree(jsonString))
                .build();

        return this;
    }

    private Step2<ResponseType> body(Object body) throws IllegalArgumentException
    {
        checkThat(body)
                .usingMessage("body cannot be null")
                .is(notNull());

        JsonElement jsonBody;
        try
        {
            jsonBody = GSON.toJsonTree(body);
        }
        catch (Exception ex)
        {
            LOG.error("failed to serialize {} as JSON", body);
            throw new JsonParseException(ex);
        }

        LOG.debug("Body {} stored as JSON {}", body, jsonBody);

        HttpRequest newRequest = HttpRequest.Builder.from(request)
                .usingBody(jsonBody)
                .build();

        HttpOperationImpl<ResponseType, CallbackResponseType> instance = copyFrom(this);

        instance.request = newRequest;

        return instance;
    }
}
