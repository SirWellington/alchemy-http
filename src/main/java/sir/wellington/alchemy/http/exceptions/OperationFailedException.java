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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SirWellington
 */
class OperationFailedException extends RuntimeException
{

    private final static Logger LOG = LoggerFactory.getLogger(OperationFailedException.class);

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

}
