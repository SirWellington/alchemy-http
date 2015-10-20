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
package tech.sirwellington.alchemy.http.exceptions;

import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.HttpRequest;

/**
 *
 * @author SirWellington
 */
public class JsonException extends AlchemyHttpException
{

    public JsonException()
    {
    }

    public JsonException(String message)
    {
        super(message);
    }

    public JsonException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JsonException(Throwable cause)
    {
        super(cause);
    }

    public JsonException(HttpRequest request)
    {
        super(request);
    }

    public JsonException(HttpRequest request, String message)
    {
        super(request, message);
    }

    public JsonException(HttpRequest request, String message, Throwable cause)
    {
        super(request, message, cause);
    }

    public JsonException(HttpRequest request, Throwable cause)
    {
        super(request, cause);
    }

    public JsonException(HttpResponse response)
    {
        super(response);
    }

    public JsonException(HttpResponse response, String message)
    {
        super(response, message);
    }

    public JsonException(HttpResponse response, String message, Throwable cause)
    {
        super(response, message, cause);
    }

    public JsonException(HttpResponse response, Throwable cause)
    {
        super(response, cause);
    }

    public JsonException(HttpRequest request, HttpResponse response)
    {
        super(request, response);
    }

    public JsonException(HttpRequest request, HttpResponse response, String message)
    {
        super(request, response, message);
    }

    public JsonException(HttpRequest request, HttpResponse response, String message, Throwable cause)
    {
        super(request, response, message, cause);
    }

    public JsonException(HttpRequest request, HttpResponse response, Throwable cause)
    {
        super(request, response, cause);
    }

}
