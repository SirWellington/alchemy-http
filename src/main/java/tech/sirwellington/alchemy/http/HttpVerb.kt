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
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
@Internal
interface HttpVerb
{

    @Throws(AlchemyHttpException::class)
    fun execute(request: HttpRequest, gson: Gson, timeoutMillis: Long = Constants.DEFAULT_TIMEOUT): HttpResponse

    object GET: HttpVerb by get()
    object POST: HttpVerb by post()
    object PUT: HttpVerb by put()
    object DELETE: HttpVerb by delete()

    companion object
    {
        fun get(): HttpVerb
        {
            return HttpVerbImpl.using(HttpMethod.GET)
        }

        fun post(): HttpVerb
        {
            return HttpVerbImpl.using(HttpMethod.POST)
        }

        fun put(): HttpVerb
        {
            return HttpVerbImpl.using(HttpMethod.PUT)
        }

        fun delete(): HttpVerb
        {
            return HttpVerbImpl.using(HttpMethod.DELETE)
        }
    }
}
