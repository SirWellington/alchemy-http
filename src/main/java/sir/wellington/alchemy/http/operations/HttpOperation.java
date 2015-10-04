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

import java.net.URL;
import sir.wellington.alchemy.http.exceptions.HttpException;

/**
 *
 * @param <Self>
 *
 * @author SirWellington
 *
 */
public interface HttpOperation<Self, ResponseType>
{

    Self usingHeader(String key, String value);

    <AsyncSelf extends HttpOperation<AsyncSelf, Void>> AsyncSelf onSuccess(OnSuccess<ResponseType> onSuccessCallback);

    <AsyncSelf extends HttpOperation<Self, Void>> AsyncSelf onFailure(OnFailure onFailureCallback);

    <NewType, NewOperation extends HttpOperation<Self, NewType>> NewOperation expecting(Class<NewType> classOfNewType);

    ResponseType at(URL url) throws HttpException;

    interface OnSuccess<ResponseType>
    {

        void processResponse(ResponseType response);
    }

    interface OnFailure
    {

        void handleError(HttpException ex);
    }
}
