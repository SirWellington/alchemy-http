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
package tech.sirwellington.alchemy.http;

import com.google.common.base.Joiner;
import static com.google.common.collect.Lists.newArrayList;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.NonNull;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.Assertions.nonEmptyString;
import static sir.wellington.alchemy.arguments.Assertions.notNull;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

/**
 *
 *
 * @author SirWellington
 *
 */
public interface AlchemyRequest
{

    interface Step1
    {

        default byte[] download(URL url) throws AlchemyHttpException
        {
            checkThat(url)
                    .usingException(ex -> new AlchemyHttpException("missing url"))
                    .is(notNull());
            try
            {
                return Resources.toByteArray(url);
            }
            catch (Exception ex)
            {
                throw new AlchemyHttpException("Could not download from URL" + url, ex);
            }
        }

        default Step1 bodyWithJsonObjectKeyValue(String key, String value) throws IllegalArgumentException
        {
            checkThat(key).usingMessage("missing key").is(nonEmptyString());
            JsonObject object = new JsonObject();
            object.addProperty(key, value);
            return body(object);
        }

        Step1 body(@NonEmpty String jsonBody) throws IllegalArgumentException;

        Step1 body(@NonNull Object body) throws IllegalArgumentException;

        Step2 get() throws AlchemyHttpException;

        Step2 post() throws AlchemyHttpException;

        Step2 put() throws AlchemyHttpException;

        Step2 delete() throws AlchemyHttpException;

        Step2 customVerb(@NonNull HttpVerb verb) throws AlchemyHttpException;

    }

    interface Step2
    {

        Step2 usingHeader(String key, String value) throws IllegalArgumentException;

        Step2 usingQueryParam(String name, String value) throws IllegalArgumentException;

        default Step2 accept(String mediaType, String... others) throws IllegalArgumentException
        {
            checkThat(mediaType).is(nonEmptyString());
            
            List<String> othersList = newArrayList(others);
            othersList.add(mediaType);
            
            String accepts = Joiner.on(",")
                    .join(othersList);
            
            return usingHeader("Accept", accepts);
        }

        default Step2 usingQueryParam(String name, Number value) throws IllegalArgumentException
        {
            return usingHeader(name, String.valueOf(value));
        }

        default Step2 usingQueryParam(String name, boolean value) throws IllegalArgumentException
        {
            return usingHeader(name, String.valueOf(value));
        }

        Step2 followRedirects(int maxNumberOfTimes) throws IllegalArgumentException;

        default Step2 followRedirects()
        {
            return followRedirects(10);
        }

        HttpResponse at(URL url) throws AlchemyHttpException;

        default HttpResponse at(String url) throws AlchemyHttpException, MalformedURLException
        {
            return at(new URL(url));
        }

        Step4<HttpResponse> onSuccess(OnSuccess<HttpResponse> onSuccessCallback);

        <ResponseType> Step3<ResponseType> expecting(Class<ResponseType> classOfResponseType) throws IllegalArgumentException;
    }

    interface Step3<ResponseType>
    {

        ResponseType at(URL url) throws AlchemyHttpException;

        default ResponseType at(String url) throws AlchemyHttpException, MalformedURLException
        {
            return at(new URL(url));
        }

        Step4<ResponseType> onSuccess(OnSuccess<ResponseType> onSuccessCallback);
    }

    interface Step4<ResponseType>
    {

        Step5<ResponseType> onFailure(OnFailure onFailureCallback);
    }

    interface Step5<ResponseType>
    {

        void at(URL url);

        default void at(String url) throws MalformedURLException
        {
            at(new URL(url));
        }

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

        void handleError(AlchemyHttpException ex);
    }
}
