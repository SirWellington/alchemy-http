/*
 * Copyright 2015 SirWellington Tech.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.NonNull;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;

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
        
        default byte[] download(URL url) throws IllegalArgumentException, AlchemyHttpException
        {
            checkThat(url)
                    .usingMessage("missing URL")
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
        
        Step3 get();
        
        Step2 post();
        
        Step2 put();
        
        Step2 delete();
        
    }
    
    interface Step2
    {
        
        Step3 nothing();
        
        Step3 body(@NonEmpty String jsonBody) throws IllegalArgumentException;
        
        Step3 body(@NonNull Object pojo) throws IllegalArgumentException;
    }
    
    interface Step3
    {
        
        Step3 usingHeader(String key, String value) throws IllegalArgumentException;
        
        Step3 usingQueryParam(String name, String value) throws IllegalArgumentException;
        
        default Step3 accept(String mediaType, String... others) throws IllegalArgumentException
        {
            checkThat(mediaType).is(nonEmptyString());
            
            List<String> othersList = newArrayList(others);
            othersList.add(mediaType);
            
            String accepts = Joiner.on(",")
                    .join(othersList);
            
            return usingHeader("Accept", accepts);
        }
        
        default Step3 usingQueryParam(String name, Number value) throws IllegalArgumentException
        {
            return usingQueryParam(name, String.valueOf(value));
        }
        
        default Step3 usingQueryParam(String name, boolean value) throws IllegalArgumentException
        {
            return usingQueryParam(name, String.valueOf(value));
        }
        
        Step3 followRedirects(int maxNumberOfTimes) throws IllegalArgumentException;
        
        default Step3 followRedirects()
        {
            return followRedirects(5);
        }
        
        HttpResponse at(URL url) throws AlchemyHttpException;
        
        default HttpResponse at(String url) throws IllegalArgumentException, AlchemyHttpException, MalformedURLException
        {
            checkThat(url).is(nonEmptyString());
            
            return at(new URL(url));
        }
        
        Step5<HttpResponse> onSuccess(OnSuccess<HttpResponse> onSuccessCallback);
        
        <ResponseType> Step4<ResponseType> expecting(Class<ResponseType> classOfResponseType) throws IllegalArgumentException;
    }
    
    interface Step4<ResponseType>
    {
        
        ResponseType at(URL url) throws IllegalArgumentException, AlchemyHttpException;
        
        default ResponseType at(String url) throws AlchemyHttpException, MalformedURLException
        {
            checkThat(url).is(nonEmptyString());
            
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
        
        default void at(String url) throws IllegalArgumentException, MalformedURLException
        {
            checkThat(url).is(nonEmptyString());
            
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
