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
import sir.wellington.alchemy.http.HttpResponse;
import sir.wellington.alchemy.http.exceptions.HttpException;

/**
 *
 *
 * @author SirWellington
 *
 */
public interface HttpOperation
{

    interface Step1
    {

        Step1 usingHeader(String key, String value) throws IllegalArgumentException;

        Step1 followRedirects(int maxNumberOfTimes) throws IllegalArgumentException;

        default Step1 followRedirects()
        {
            return followRedirects(10);
        }

        Step2 then();
    }

    interface Step2
    {

        HttpResponse get() throws HttpException;

        HttpResponse post() throws HttpException;

        HttpResponse post(@NonEmpty String jsonBody) throws HttpException;

        HttpResponse post(@NonNull Object body) throws HttpException;

        HttpResponse put() throws HttpException;

        HttpResponse put(@NonEmpty String jsonBody) throws HttpException;

        HttpResponse put(@NonNull Object body) throws HttpException;

        HttpResponse delete() throws HttpException;

        HttpResponse delete(@NonEmpty String jsonBody) throws HttpException;

        HttpResponse delete(@NonNull Object body) throws HttpException;

        HttpResponse customVerb(HttpVerb verb) throws HttpException;

        Step3 onSuccess(OnSuccess onSuccessCallback);
    }

    interface Step3
    {

        Step2 onFailure(OnFailure onFailureCallback);

    }

    interface OnSuccess
    {

        void processResponse(HttpResponse response);

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
