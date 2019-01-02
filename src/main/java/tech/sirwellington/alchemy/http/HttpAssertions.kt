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

package tech.sirwellington.alchemy.http

import com.google.gson.JsonElement
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.access.NonInstantiable
import tech.sirwellington.alchemy.arguments.AlchemyAssertion
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.FailedAssertionException
import tech.sirwellington.alchemy.arguments.assertions.*

/**
 * @author SirWellington
 */
@Internal
@NonInstantiable
internal object HttpAssertions
{

    @JvmStatic
    fun validHttpStatusCode(): AlchemyAssertion<Int>
    {
        /*
        * See https://www.whoishostingthis.com/resources/http-status-codes
        */
        return greaterThanOrEqualTo(100).and(lessThanOrEqualTo(600))
    }

    /*
    * TODO: Add check to see if the class structure is that of a POJO.
    */
    @JvmStatic
    fun <Response> validResponseClass(): AlchemyAssertion<Class<Response>>
    {

        return AlchemyAssertion { klass ->

            checkThat(klass).isA(nonNullReference())

            checkThat(klass == Void::class.java)
                    .usingMessage("Response class cannot be null")
                    .isA(falseStatement())
        }
    }

    @JvmStatic
    fun ready(): AlchemyAssertion<HttpRequest>
    {
        return AlchemyAssertion { request ->

            checkThat(request)
                    .usingMessage("Request missing")
                    .isA(nonNullReference())

            checkThat(request.method)
                    .usingMessage("Request missing HTTP Method")
                    .isA(nonNullReference())

            checkThat(request.url)
                    .usingMessage("Request missing URL")
                    .isA(nonNullReference())

            checkThat(request.url!!.protocol)
                    .isA(stringBeginningWith("http"))
        }
    }

    @JvmStatic
    fun validContentType(): AlchemyAssertion<String>
    {
        return AlchemyAssertion assertion@ { contentType ->

            checkThat(contentType)
                    .usingMessage("missing Content-Type")
                    .isA(nonEmptyString())

            if (contentType.contains(ContentTypes.APPLICATION_JSON))
            {
                return@assertion
            }

            if (contentType.contains(ContentTypes.PLAIN_TEXT))
            {
                return@assertion
            }

            throw FailedAssertionException("Not a valid JSON content Type: " + contentType)
        }
    }

    @JvmStatic
    fun validRequest(): AlchemyAssertion<HttpRequest>
    {
        return AlchemyAssertion { request ->

            checkThat(request)
                    .usingMessage("missing HTTP Request")
                    .isA(nonNullReference())

            checkThat(request.url)
                    .usingMessage("missing request URL")
                    .isA(nonNullReference())
        }
    }

    @JvmStatic
    fun jsonArray(): AlchemyAssertion<JsonElement>
    {
        return AlchemyAssertion { json ->

            checkThat<JsonElement>(json).isA(nonNullReference())

            if (!json.isJsonArray)
            {
                throw FailedAssertionException("Expecting JSON Array, instead: " + json)
            }
        }
    }

    @JvmStatic
    fun okResponse(): AlchemyAssertion<HttpResponse>
    {
        return AlchemyAssertion { response ->

            checkThat(response).isA(nonNullReference())

            if (!response.isOk)
            {
                throw FailedAssertionException("Http Response not OK. Status Code: " + response.statusCode())
            }
        }
    }

}
