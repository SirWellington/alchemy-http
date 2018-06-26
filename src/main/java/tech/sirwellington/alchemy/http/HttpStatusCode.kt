/*
 * Copyright © 2018. Sir Wellington.
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


package tech.sirwellington.alchemy.http


import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.positiveInteger
import tech.sirwellington.alchemy.kotlin.extensions.anyElement

/**
 * Contains commonly used HTTP Status Codes.
 *
 * @see [http://www.restapitutorial.com/httpstatuscodes.html](http://www.restapitutorial.com/httpstatuscodes.html)
 *
 * @author SirWellington
 */
enum class HttpStatusCode(val code: Int)
{

    //Informational
    INFORMATIONAL(100),

    //Success
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NON_AUTHORIZED_INFORMATION(203),
    NO_CONTENT(204),
    RESET_CONTENT(205),
    PARTIAL_CONTENT(206),

    //Redirection
    REDIRECTED(300),
    MOVED_PERMANENTLY(301),
    FOUND(302),
    NOT_MODIFIED(304),
    USE_PROXY(305),
    TEMPORARY_REDIRECT(307),


    //Client Errors
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    PAYMENT_REQUIRED(402),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),
    GONE(410),
    LENGTH_REQUIRED(411),
    PRECONDITION_FAILED(412),
    REQUEST_ENTITY_TOO_LARGE(413),
    REQUEST_URI_TOO_LONG(414),
    UNSUPPORTED_MEDIA_TYPE(415),

    //Server Errrors
    INTERNAL_SERVICE_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504),
    HTTP_VERSION_NOT_SUPPORTED(505),
    NETWORK_READ_TIMEOUT_ERROR(598),
    NETWORK_AUTHENTICATION_REQUIRED(511);

    val statusName = this.toString()

    init
    {
        checkThat(code).isA(positiveInteger())
    }

    fun matchesCode(code: Int): Boolean
    {
        return this.code == code
    }

    companion object
    {

        private val REVERSE_MAP = createReverseMapping()

        @JvmStatic
        val all = values().toList()

        @JvmStatic
        val any get() = all.anyElement ?: OK

        @JvmStatic
        fun forCode(code: Int): HttpStatusCode?
        {
            return REVERSE_MAP[code]
        }

        private fun createReverseMapping(): Map<Int, HttpStatusCode>
        {
            return all.map { it.code to it }.toMap()
        }
    }


}
