/*
 * Copyright © 2019. Sir Wellington.
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
package tech.sirwellington.alchemy.http.exceptions;

import tech.sirwellington.alchemy.http.HttpRequest;
import tech.sirwellington.alchemy.http.HttpResponse;

/**
 * Thrown when a connection could not be established
 * to the server.
 *
 * @author SirWellington
 */
public class AlchemyConnectionException extends AlchemyHttpException
{

    public AlchemyConnectionException()
    {
    }

    public AlchemyConnectionException(String message)
    {
        super(message);
    }

    public AlchemyConnectionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AlchemyConnectionException(Throwable cause)
    {
        super(cause);
    }

    public AlchemyConnectionException(HttpRequest request)
    {
        super(request);
    }

    public AlchemyConnectionException(HttpRequest request, String message)
    {
        super(request, message);
    }

    public AlchemyConnectionException(HttpRequest request, String message, Throwable cause)
    {
        super(request, message, cause);
    }

    public AlchemyConnectionException(HttpRequest request, Throwable cause)
    {
        super(request, cause);
    }

    public AlchemyConnectionException(HttpResponse response)
    {
        super(response);
    }

    public AlchemyConnectionException(HttpResponse response, String message)
    {
        super(response, message);
    }

    public AlchemyConnectionException(HttpResponse response, String message, Throwable cause)
    {
        super(response, message, cause);
    }

    public AlchemyConnectionException(HttpResponse response, Throwable cause)
    {
        super(response, cause);
    }

    public AlchemyConnectionException(HttpRequest request, HttpResponse response)
    {
        super(request, response);
    }

    public AlchemyConnectionException(HttpRequest request, HttpResponse response, String message)
    {
        super(request, response, message);
    }

    public AlchemyConnectionException(HttpRequest request, HttpResponse response, String message, Throwable cause)
    {
        super(request, response, message, cause);
    }

    public AlchemyConnectionException(HttpRequest request, HttpResponse response, Throwable cause)
    {
        super(request, response, cause);
    }
}
