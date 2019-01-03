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
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.NumberGenerators
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.http.HttpAssertions.jsonArray
import tech.sirwellington.alchemy.http.exceptions.JsonException
import java.util.Objects

internal class TestResponse : HttpResponse
{

    var statusCode = one(NumberGenerators.integers(200, 500))
    var responseHeaders = CollectionGenerators.mapOf(StringGenerators.alphabeticStrings(), StringGenerators.alphabeticStrings(), 10)
    var responseBody = one(Generators.jsonElements())
    private val gson = Constants.DEFAULT_GSON

    override val isOk: Boolean
        get() = statusCode >= 200 && statusCode <= 208

    fun copy(): TestResponse
    {
        val clone = TestResponse()
        clone.statusCode = this.statusCode
        clone.responseHeaders = Maps.mutableCopyOf(this.responseHeaders)
        clone.responseBody = gson.toJsonTree(responseBody)
        return clone
    }

    override fun statusCode(): Int
    {
        return statusCode
    }

    override fun responseHeaders(): Map<String, String>
    {
        return responseHeaders
    }

    override fun bodyAsString(): String
    {
        return responseBody.toString()
    }

    @Throws(JsonException::class)
    override fun body(): JsonElement
    {
        return responseBody
    }

    @Throws(JsonException::class)
    override fun <Pojo> bodyAs(classOfPojo: Class<Pojo>): Pojo
    {
        return gson.fromJson(responseBody, classOfPojo)
    }

    override fun hashCode(): Int
    {
        var hash = 7
        hash = 89 * hash + this.statusCode
        hash = 89 * hash + Objects.hashCode(this.responseHeaders)
        hash = 89 * hash + Objects.hashCode(this.responseBody)
        return hash
    }

    override fun equals(other: Any?): Boolean
    {
       val other = other as? HttpResponse ?: return false

        return this.equals(other)
    }

    override fun toString(): String
    {
        return "TestResponse{statusCode=$statusCode, responseHeaders=$responseHeaders, responseBody=$responseBody}"
    }

    @Throws(JsonException::class)
    override fun <T> bodyAsArrayOf(classOfT: Class<T>): List<T>
    {
        checkThat(this.responseBody).isA(jsonArray())

        val array = java.lang.reflect.Array.newInstance(classOfT, 0) as Array<T>
        val type = array::class.java

        return gson.fromJson(responseBody, type).toList()
    }
}
