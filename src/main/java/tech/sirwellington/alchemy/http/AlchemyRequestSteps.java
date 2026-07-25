/*
 * Copyright © 2026. Sir Wellington.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;

import tech.sirwellington.alchemy.arguments.Arguments;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

public interface AlchemyRequestSteps
{
    interface Step1
    {
        default byte[] download(String url)
        {
            URL _url;
            try { _url = new URL(url); }
            catch (Exception ex) { throw new IllegalArgumentException("not a valid URL: [" + url + "]"); }
            return download(_url);
        }

        default byte[] download(URL url)
        {
            try
            {
                try (var stream = url.openStream())
                {
                    return stream.readAllBytes();
                }
            }
            catch (Exception ex)
            {
                throw new AlchemyHttpException("Could not download from URL: " + url, ex);
            }
        }

        Step3 get();
        Step2 post();
        Step2 put();
        Step2 delete();
        Step2 method(RequestMethod requestMethod);
    }

    interface Step2
    {
        Step3 noBody();
        default Step3 nothing() { return noBody(); }
        Step3 body(String jsonString);
        Step3 body(Object pojo);
    }

    interface Step3
    {
        Step3 usingHeader(String key, String value);
        Step3 usingQueryParam(String name, String value);

        default Step3 accept(String mediaType, String... others)
        {
            Arguments.checkThat(mediaType).isA(nonEmptyString());
            var contentTypes = new LinkedHashSet<String>();
            contentTypes.add(mediaType);
            for (String other : others) { contentTypes.add(other); }
            String accepts = String.join(",", contentTypes);
            return usingHeader("Accept", accepts);
        }

        default Step3 usingQueryParam(String name, Number value) { return usingQueryParam(name, value.toString()); }
        default Step3 usingQueryParam(String name, boolean value) { return usingQueryParam(name, String.valueOf(value)); }
        Step3 followRedirects(int maxNumberOfTimes);
        default Step3 followRedirects() { return followRedirects(5); }
        HttpResponse at(URL url) throws AlchemyHttpException;

        default HttpResponse at(String url) throws AlchemyHttpException, MalformedURLException
        {
            Arguments.checkThat(url).isA(validURL());
            return at(new URL(url));
        }

        Step5<HttpResponse> onSuccess(OnSuccess<HttpResponse> onSuccessCallback);
        <ResponseType> Step4<ResponseType> expecting(Class<ResponseType> classOfResponseType);
    }

    interface Step4<ResponseType>
    {
        ResponseType at(URL url) throws AlchemyHttpException;

        default ResponseType at(String url) throws AlchemyHttpException, MalformedURLException
        {
            Arguments.checkThat(url).isA(validURL());
            return at(new URL(url));
        }

        Step5<ResponseType> onSuccess(OnSuccess<ResponseType> onSuccessCallback);
    }

    interface Step5<ResponseType>
    {
        Step6<ResponseType> onFailure(OnFailure onFailureCallback);
    }

    interface Step6<ResponseType>
    {
        void at(URL url);

        default void at(String url) throws MalformedURLException
        {
            Arguments.checkThat(url).isA(validURL());
            at(new URL(url));
        }
    }

    @FunctionalInterface
    interface OnSuccess<ResponseType>
    {
        void processResponse(ResponseType response);

        @SuppressWarnings("rawtypes")
        OnSuccess NO_OP = response -> {};

        static <ResponseType> OnSuccess<ResponseType> create(java.util.function.Consumer<ResponseType> block)
        {
            return block::accept;
        }
    }

    @FunctionalInterface
    interface OnFailure
    {
        void handleError(AlchemyHttpException ex);

        OnFailure NO_OP = ex -> {};

        static OnFailure create(java.util.function.Consumer<AlchemyHttpException> block)
        {
            return block::accept;
        }
    }
}
