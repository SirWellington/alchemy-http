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

import sir.wellington.alchemy.annotations.arguments.NonEmpty;
import sir.wellington.alchemy.annotations.arguments.NonNull;
import sir.wellington.alchemy.http.exceptions.HttpException;

/**
 *
 *
 * @author SirWellington
 *
 */
public interface HttpOperation
{

    interface Step1<ResponseType>
    {

        Step1<ResponseType> usingHeader(String key, String value) throws IllegalArgumentException;

        <NewType> Step1<NewType> expecting(Class<NewType> classOfNewType) throws IllegalArgumentException;

        Step1<ResponseType> followRedirects(int maxNumberOfTimes) throws IllegalArgumentException;

        default Step1<ResponseType> followRedirects()
        {
            return followRedirects(10);
        }

        Step2<ResponseType> then();
    }

    interface Step2<ResponseType>
    {

        ResponseType get() throws HttpException;

        ResponseType post() throws HttpException;

        ResponseType post(@NonEmpty String jsonBody) throws HttpException;

        ResponseType post(@NonNull Object body) throws HttpException;

        ResponseType put() throws HttpException;

        ResponseType put(@NonEmpty String jsonBody) throws HttpException;

        ResponseType put(@NonNull Object body) throws HttpException;

        ResponseType delete() throws HttpException;

        ResponseType delete(@NonEmpty String jsonBody) throws HttpException;

        ResponseType delete(@NonNull Object body) throws HttpException;

        ResponseType customVerb(HttpVerb verb) throws HttpException;

        Step3<ResponseType> onSuccess(OnSuccess<ResponseType> onSuccessCallback);
    }

    interface Step3<ResponseType>
    {

        Step2<Void> onFailure(OnFailure onFailureCallback);

    }

    interface OnSuccess<ResponseType>
    {

        void processResponse(ResponseType response);

        OnSuccess NO_OP = response ->
        {
        };
    }

    interface OnFailure
    {

        OnFailure NO_OP = ex ->
        {

        };

        void handleError(HttpException ex);
    }
}
