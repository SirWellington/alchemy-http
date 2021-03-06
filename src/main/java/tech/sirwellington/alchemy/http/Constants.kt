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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.access.NonInstantiable
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

/**
 * @author SirWellington
 */
@NonInstantiable
@Internal
internal object Constants
{

    @JvmStatic
    val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    @JvmField
    val DEFAULT_HEADERS = createDefaultHeaders()

    @JvmField
    val DEFAULT_GSON: Gson = GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .create()

    /**
     * [Http Requests][HttpRequest] default to [RequestMethod.GET]
     * if not set explicitly.
     */
    @JvmField
    val DEFAULT_REQUEST_METHOD = RequestMethod.GET

    @JvmField
    val DEFAULT_TIMEOUT = TimeUnit.MILLISECONDS.convert(60, SECONDS)

    @JvmStatic
    private fun createDefaultHeaders(): Map<String, String>
    {
        val headers = Maps.create<String, String>()

        headers["Accept"] = "${ContentTypes.APPLICATION_JSON}, ${ContentTypes.PLAIN_TEXT}"
        headers["User-Agent"] = "Alchemy HTTP"
        headers["Content-Type"] = "${ContentTypes.APPLICATION_JSON}; charset=UTF-8"

        return Maps.immutableCopyOf(headers)
    }
}

object ContentTypes
{
    @JvmField
    val APPLICATION_JSON = "application/json"

    @JvmField
    val PLAIN_TEXT = "text/plain"

}