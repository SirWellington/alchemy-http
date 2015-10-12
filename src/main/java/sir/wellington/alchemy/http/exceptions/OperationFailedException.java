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

/**
 *
 * @author SirWellington
 */
class OperationFailedException extends AlchemyHttpException
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

}
