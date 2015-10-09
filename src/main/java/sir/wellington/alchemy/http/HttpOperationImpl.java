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
import static sir.wellington.alchemy.arguments.assertions.Assertions.notNull;
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
class HttpOperationImpl implements Step1,
                                   Step2,
                                   Step3
{

    private final static Logger LOG = LoggerFactory.getLogger(HttpOperationImpl.class);
    private final static Gson GSON = new Gson();

    private OnSuccess successCallback;
    private OnFailure failureCallback;

    private HttpRequest request;
    private HttpClient httpClient;
    private ExecutorService async;
    
    private HttpOperationImpl()
    {

    }

    HttpOperationImpl(OnSuccess successCallback,
                      OnFailure failureCallback,
                      HttpRequest request,
                      HttpClient httpClient,
                      ExecutorService async)
    {
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
        this.request = request;
        this.httpClient = httpClient;
        this.async = async;
    }

    @Override
    public Step1 usingHeader(String key, String value)
    {
        Map<String, String> newRequestHeaders = Maps.newHashMap(request.getRequestHeaders());
        newRequestHeaders.put(key, value);

        this.request = HttpRequest.Builder.from(request)
                .usingRequestHeaders(newRequestHeaders)
                .build();

        return this;
    }

    private boolean isAsync()
    {
        return failureCallback != null || successCallback != null;
    }

    @Override
    public Step1 followRedirects(int maxNumberOfTimes) throws IllegalArgumentException
    {
        return this;
    }

    @Override
    public Step2 then()
    {
        return this;
    }

    @Override
    public HttpResponse post(String jsonBody) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse post(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse put(String jsonBody) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse put(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse delete(String jsonBody) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse delete(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse get() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse post() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse put() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse delete() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HttpResponse customVerb(HttpVerb verb) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Step3 onSuccess(OnSuccess onSuccessCallback)
    {
        checkThat(onSuccessCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        this.successCallback = onSuccessCallback;
        return this;
    }

    @Override
    public Step2 onFailure(OnFailure onFailureCallback)
    {
        checkThat(onFailureCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        this.failureCallback = onFailureCallback;
        return this;
    }

    private Step2 bodyString(String jsonString) throws IllegalArgumentException
    {
        checkThat(jsonString)
                .usingMessage("jsonString is empty")
                .is(nonEmptyString());

        return body(jsonString);
    }

    private Step2 body(Object body) throws IllegalArgumentException
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
        this.request = newRequest;

        return this;
    }
}
