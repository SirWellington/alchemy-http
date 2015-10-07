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

import java.util.Map;
import sir.wellington.alchemy.http.operations.HttpOperation.OnFailure;
import sir.wellington.alchemy.http.operations.HttpOperation.OnSuccess;

class HttpOperationImplBuilder<ResponseType, CallbackResponseType>
{

    private Class<ResponseType> classOfResponseType;
    private Class<CallbackResponseType> classOfCallbackResponseType;
    private Map<String, String> initialRequestHeaders;
    private OnSuccess<CallbackResponseType> successCallback;
    private OnFailure failureCallback;

    HttpOperationImplBuilder()
    {
    }

    HttpOperationImplBuilder<ResponseType, CallbackResponseType> usingClassOfResponseType(Class<ResponseType> classOfResponseType)
    {
        this.classOfResponseType = classOfResponseType;
        return this;
    }

    HttpOperationImplBuilder<ResponseType, CallbackResponseType> usingClassOfCallbackResponseType(Class<CallbackResponseType> classOfCallbackResponseType)
    {
        this.classOfCallbackResponseType = classOfCallbackResponseType;
        return this;
    }

    HttpOperationImplBuilder<ResponseType, CallbackResponseType> usingInitialRequestHeaders(Map<String, String> initialRequestHeaders)
    {
        this.initialRequestHeaders = initialRequestHeaders;
        return this;
    }

    HttpOperationImplBuilder<ResponseType, CallbackResponseType> usingSuccessCallback(OnSuccess<CallbackResponseType> successCallback)
    {
        this.successCallback = successCallback;
        return this;
    }

    HttpOperationImplBuilder<ResponseType, CallbackResponseType> usingFailureCallback(OnFailure failureCallback)
    {
        this.failureCallback = failureCallback;
        return this;
    }

    HttpOperationImpl<ResponseType, CallbackResponseType> build()
    {
        return new HttpOperationImpl<>(classOfResponseType, classOfCallbackResponseType, initialRequestHeaders, successCallback, failureCallback);
    }

}
