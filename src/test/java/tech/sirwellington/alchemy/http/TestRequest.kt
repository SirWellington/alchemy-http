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

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators
import java.net.URL
import java.util.Objects

/**
 *
 * @author SirWellington
 */
@Internal
internal data class TestRequest(override var queryParams: Map<String, String> = CollectionGenerators.mapOf(StringGenerators.alphabeticStrings(), StringGenerators.alphabeticStrings(), 6),
                                override var url: URL? = one(Generators.validUrls()),
                                override var body: JsonElement? = one(Generators.jsonElements()),
                                override var method: RequestMethod = Constants.DEFAULT_REQUEST_METHOD) : HttpRequest
{

    override var requestHeaders: Map<String, String>? = CollectionGenerators.mapOf(StringGenerators.alphabeticStrings(),
                                                                                   StringGenerators.alphabeticStrings(),
                                                                                   20).toMap()


    override fun hasBody(): Boolean
    {
        return body?.let { it != JsonNull.INSTANCE } ?: false
    }

    override fun hasQueryParams(): Boolean
    {
        return Maps.isEmpty(queryParams)
    }

    override fun equals(other: HttpRequest?): Boolean
    {
        return this == other as Any?
    }

    override fun hasMethod(): Boolean
    {
        return Objects.nonNull(method)
    }

    override fun equals(other: Any?): Boolean
    {
        val other = other as? HttpRequest ?: return false
        return super.equals(other)
    }

}
