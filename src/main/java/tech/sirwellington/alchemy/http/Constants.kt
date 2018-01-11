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
package tech.sirwellington.alchemy.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.access.NonInstantiable

/**
 * @author SirWellington
 */
@NonInstantiable
@Internal
internal object Constants
{

    val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    @Internal
    val DEFAULT_HEADERS = createDefaultHeaders()

    val defaultGson: Gson = GsonBuilder()
                                .setDateFormat(DATE_FORMAT)
                                .create()

    private fun createDefaultHeaders(): Map<String, String>
    {
        val headers = Maps.create<String, String>()

        headers["Accept"] = "application/json, text/plain"
        headers["User-Agent"] = "Alchemy HTTP"
        headers["Content-Type"] = "application/json"

        return Maps.immutableCopyOf(headers)
    }
}
