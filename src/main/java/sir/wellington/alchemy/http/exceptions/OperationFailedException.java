/*
 * Copyright 2015 SirWellington.
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
package sir.wellington.alchemy.http.exceptions;

import sir.wellington.alchemy.http.HttpResponse;
import sir.wellington.alchemy.http.HttpRequest;

/**
 *
 * @author SirWellington
 */
public class OperationFailedException extends AlchemyHttpException
{

    public OperationFailedException()
    {
    }

    public OperationFailedException(String message)
    {
        super(message);
    }

    public OperationFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public OperationFailedException(Throwable cause)
    {
        super(cause);
    }

    public OperationFailedException(HttpRequest request)
    {
        super(request);
    }

    public OperationFailedException(HttpRequest request, String message)
    {
        super(request, message);
    }

    public OperationFailedException(HttpRequest request, String message, Throwable cause)
    {
        super(request, message, cause);
    }

    public OperationFailedException(HttpRequest request, Throwable cause)
    {
        super(request, cause);
    }

    public OperationFailedException(HttpResponse response)
    {
        super(response);
    }

    public OperationFailedException(HttpResponse response, String message)
    {
        super(response, message);
    }

    public OperationFailedException(HttpResponse response, String message, Throwable cause)
    {
        super(response, message, cause);
    }

    public OperationFailedException(HttpResponse response, Throwable cause)
    {
        super(response, cause);
    }

    public OperationFailedException(HttpRequest request, HttpResponse response)
    {
        super(request, response);
    }

    public OperationFailedException(HttpRequest request, HttpResponse response, String message)
    {
        super(request, response, message);
    }

    public OperationFailedException(HttpRequest request, HttpResponse response, String message, Throwable cause)
    {
        super(request, response, message, cause);
    }

    public OperationFailedException(HttpRequest request, HttpResponse response, Throwable cause)
    {
        super(request, response, cause);
    }

}
