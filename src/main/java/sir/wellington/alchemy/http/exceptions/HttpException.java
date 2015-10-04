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
package sir.wellington.alchemy.http.exceptions;

import sir.wellington.alchemy.http.HttpResponse;

/**
 *
 * @author SirWellington
 */
public class HttpException extends RuntimeException
{

    private HttpResponse response;

    public HttpException()
    {
    }

    public HttpException(HttpResponse response)
    {
        this.response = response;
    }

    public HttpException(HttpResponse response, String message)
    {
        super(message);
        this.response = response;
    }

    public HttpException(HttpResponse response, Throwable cause)
    {
        super(cause);
        this.response = response;
    }

    public HttpException(HttpResponse response, String message, Throwable cause)
    {
        super(message, cause);
        this.response = response;
    }

    public HttpException(String message)
    {
        super(message);
    }

    public HttpException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HttpException(Throwable cause)
    {
        super(cause);
    }

    public HttpResponse getResponse()
    {
        return response;
    }

}
