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

import tech.sirwellington.alchemy.http.AlchemyHttp
import tech.sirwellington.alchemy.http.HttpRequest
import tech.sirwellington.alchemy.http.HttpResponse

/**
 * Parent to all Exceptions thrown by [AlchemyHttp].
 *
 * @author SirWellington
 */
open class AlchemyHttpException : RuntimeException
{

    var request: HttpRequest? = null
        private set
    var response: HttpResponse? = null
        private set

    constructor()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)

    constructor(request: HttpRequest)
    {
        this.request = request
    }

    constructor(request: HttpRequest, message: String) : super(message)
    {
        this.request = request
    }

    constructor(request: HttpRequest, message: String, cause: Throwable) : super(message, cause)
    {
        this.request = request
    }

    constructor(request: HttpRequest, cause: Throwable) : super(cause)
    {
        this.request = request
    }

    constructor(response: HttpResponse)
    {
        this.response = response
    }

    constructor(response: HttpResponse, message: String) : super(message)
    {
        this.response = response
    }

    constructor(response: HttpResponse, message: String, cause: Throwable) : super(message, cause)
    {
        this.response = response
    }

    constructor(response: HttpResponse, cause: Throwable) : super(cause)
    {
        this.response = response
    }

    constructor(request: HttpRequest, response: HttpResponse)
    {
        this.request = request
        this.response = response
    }

    constructor(request: HttpRequest, response: HttpResponse, message: String) : super(message)
    {
        this.request = request
        this.response = response
    }

    constructor(request: HttpRequest, response: HttpResponse, message: String, cause: Throwable) : super(message, cause)
    {
        this.request = request
        this.response = response
    }

    constructor(request: HttpRequest, response: HttpResponse, cause: Throwable) : super(cause)
    {
        this.request = request
        this.response = response
    }

    fun hasRequest(): Boolean
    {
        return request != null
    }

    fun hasResponse(): Boolean
    {
        return response != null
    }

    override fun toString(): String
    {
        var string = super.toString()

        val response = this.response ?: return string

        string += " | Status Code: ${response.statusCode()} | Response: ${response.bodyAsString()}"
        return string

    }

}
