/*
 * Copyright © 2018. Sir Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.sirwellington.alchemy.http.exceptions

import tech.sirwellington.alchemy.http.HttpRequest
import tech.sirwellington.alchemy.http.HttpResponse


/**
 * Thrown when a connection could not be established
 * to the server.
 *
 * @author SirWellington
 */
class AlchemyConnectionException : AlchemyHttpException
{

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
    constructor(request: HttpRequest) : super(request)
    constructor(request: HttpRequest, message: String) : super(request, message)
    constructor(request: HttpRequest, message: String, cause: Throwable) : super(request, message, cause)
    constructor(request: HttpRequest, cause: Throwable) : super(request, cause)
    constructor(response: HttpResponse) : super(response)
    constructor(response: HttpResponse, message: String) : super(response, message)
    constructor(response: HttpResponse, message: String, cause: Throwable) : super(response, message, cause)
    constructor(response: HttpResponse, cause: Throwable) : super(response, cause)
    constructor(request: HttpRequest, response: HttpResponse) : super(request, response)
    constructor(request: HttpRequest, response: HttpResponse, message: String) : super(request, response, message)
    constructor(request: HttpRequest, response: HttpResponse, message: String, cause: Throwable) : super(request, response, message, cause)
    constructor(request: HttpRequest, response: HttpResponse, cause: Throwable) : super(request, response, cause)
}