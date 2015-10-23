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
package tech.sirwellington.alchemy.http.exceptions;

import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.HttpRequest;
import tech.sirwellington.alchemy.http.HttpResponse;

/**
 * Parent to all Exceptions thrown by {@link AlchemyHttp}.
 *
 * @author SirWellington
 */
public class AlchemyHttpException extends RuntimeException
{

    private HttpRequest request;
    private HttpResponse response;

    public AlchemyHttpException()
    {
    }

    public AlchemyHttpException(String message)
    {
        super(message);
    }

    public AlchemyHttpException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AlchemyHttpException(Throwable cause)
    {
        super(cause);
    }

    public AlchemyHttpException(HttpRequest request)
    {
        this.request = request;
    }

    public AlchemyHttpException(HttpRequest request, String message)
    {
        super(message);
        this.request = request;
    }

    public AlchemyHttpException(HttpRequest request, String message, Throwable cause)
    {
        super(message, cause);
        this.request = request;
    }

    public AlchemyHttpException(HttpRequest request, Throwable cause)
    {
        super(cause);
        this.request = request;
    }

    public AlchemyHttpException(HttpResponse response)
    {
        this.response = response;
    }

    public AlchemyHttpException(HttpResponse response, String message)
    {
        super(message);
        this.response = response;
    }

    public AlchemyHttpException(HttpResponse response, String message, Throwable cause)
    {
        super(message, cause);
        this.response = response;
    }

    public AlchemyHttpException(HttpResponse response, Throwable cause)
    {
        super(cause);
        this.response = response;
    }

    public AlchemyHttpException(HttpRequest request, HttpResponse response)
    {
        this.request = request;
        this.response = response;
    }

    public AlchemyHttpException(HttpRequest request, HttpResponse response, String message)
    {
        super(message);
        this.request = request;
        this.response = response;
    }

    public AlchemyHttpException(HttpRequest request, HttpResponse response, String message, Throwable cause)
    {
        super(message, cause);
        this.request = request;
        this.response = response;
    }

    public AlchemyHttpException(HttpRequest request, HttpResponse response, Throwable cause)
    {
        super(cause);
        this.request = request;
        this.response = response;
    }

    public boolean hasRequest()
    {
        return request != null;
    }

    public boolean hasResponse()
    {
        return response != null;
    }

    public HttpRequest getRequest()
    {
        return request;
    }

    public HttpResponse getResponse()
    {
        return response;
    }

}
