/*
 * Copyright 2015 Wellington.
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
package sir.wellington.alchemy.http.operations;

import sir.wellington.alchemy.http.exceptions.HttpException;

/**
 *
 * @param <ResponseType>
 *
 * @author SirWellington
 *
 */
public interface HttpOperation<ResponseType>
{

    HttpOperation<ResponseType> usingHeader(String key, String value);

    <NewType> HttpOperation<NewType> expecting(Class<NewType> classOfNewType);

    HttpOperation<Void> onSuccess(OnSuccess<ResponseType> onSuccessCallback);

    HttpOperation<Void> onFailure(OnFailure onFailureCallback);

    ResponseType get() throws HttpException;

    ResponseType post() throws HttpException;

    ResponseType post(String jsonString) throws HttpException;

    ResponseType post(Object body) throws HttpException;

    ResponseType put() throws HttpException;

    ResponseType put(String jsonString) throws HttpException;

    ResponseType put(Object body) throws HttpException;

    ResponseType delete() throws HttpException;

    ResponseType delete(String jsonString) throws HttpException;

    ResponseType delete(Object body) throws HttpException;

    ResponseType customVerb(HttpVerb verb) throws HttpException;

    interface OnSuccess<ResponseType>
    {

        void processResponse(ResponseType response);
    }

    interface OnFailure
    {

        void handleError(HttpException ex);
    }
}
