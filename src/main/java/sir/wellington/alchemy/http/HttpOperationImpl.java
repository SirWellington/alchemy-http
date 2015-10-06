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
class HttpOperationImpl<ReturnType, ExpectedResponseType> implements HttpOperation<ReturnType>
{

    private final static Logger LOG = LoggerFactory.getLogger(HttpOperationImpl.class);

    private final Map<String, String> requestHeaders;
    private final Class<ExpectedResponseType> classOfResponseType;
    private final OnSuccess<ExpectedResponseType> successCallback;
    private final OnFailure failureCallback;

    HttpOperationImpl(Class<ExpectedResponseType> classOfResponseType)
    {
        this(classOfResponseType, null, null, null);
    }

    HttpOperationImpl(Class<ExpectedResponseType> classOfResponseType,
                      Map<String, String> initialRequestHeaders,
                      OnSuccess<ExpectedResponseType> successCallback,
                      OnFailure failureCallback)
    {
        initialRequestHeaders = nullToEmpty(initialRequestHeaders);

        this.requestHeaders = ImmutableMap.copyOf(initialRequestHeaders);
        this.classOfResponseType = classOfResponseType;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    @Override
    public HttpOperation<ReturnType> usingHeader(String key, String value)
    {
        Map<String, String> newRequestHeaders = Maps.newHashMap(this.requestHeaders);
        newRequestHeaders.put(key, value);

        return new HttpOperationImpl<>(classOfResponseType, newRequestHeaders, successCallback, failureCallback);
    }

    @Override
    public <NewType> HttpOperation<NewType> expecting(Class<NewType> classOfNewType)
    {
        checkThat(classOfNewType).is(notNull());
        return new HttpOperationImpl<>(classOfNewType, requestHeaders, null, null);
    }

    @Override
    public HttpOperation<Void> onSuccess(OnSuccess<ReturnType> onSuccessCallback)
    {
        checkThat(onSuccessCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());
        
        return new HttpOperationImpl<Void>(Class<Void> voidClass, requestHeaders, onSuccessCallback, failureCallback);
    }

    @Override
    public HttpOperation<Void> onFailure(OnFailure onFailureCallback)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType at(URL url) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType get() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType post() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType post(String jsonString) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType post(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType put() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType put(String jsonString) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType put(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType delete() throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType delete(String jsonString) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType delete(Object body) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ReturnType customVerb(HttpVerb verb) throws HttpException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
