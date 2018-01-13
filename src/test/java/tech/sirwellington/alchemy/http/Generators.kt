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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.access.NonInstantiable
import tech.sirwellington.alchemy.generator.AlchemyGenerator
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.BooleanGenerators.Companion.booleans
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.integers
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.positiveDoubles
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.positiveIntegers
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphanumericStrings
import java.net.MalformedURLException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
internal object Generators
{

    private val LOG = LoggerFactory.getLogger(Generators::class.java)

    @JvmStatic
    fun validUrls(): AlchemyGenerator<URL>
    {
        return AlchemyGenerator {

            val protocols = StringGenerators.stringsFromFixedList("https://", "http://")
            val protocol = one(protocols)
            val host = alphanumericStrings(10).get()
            val uri = "$protocol$host"

            try
            {
                URL(uri)
            }
            catch (ex: MalformedURLException)
            {
                throw RuntimeException(ex)
            }
        }
    }

    @JvmStatic
    fun jsonElements(): AlchemyGenerator<JsonElement>
    {
        return AlchemyGenerator {

            val random = one(integers(1, 5))

            when (random)
            {
                1    -> jsonObjects().get()
                2    -> jsonArrays().get()
                3    -> jsonNull().get()
                else -> jsonPrimitives().get()
            }
        }
    }

    @JvmStatic
    fun jsonObjects(): AlchemyGenerator<JsonObject>
    {
        return AlchemyGenerator {

            val result = JsonObject()

            val elements = one<Int>(integers(10, 50))

            for (i in 0 until elements)
            {
                val key = one(alphabeticStrings())

                val random = one(integers(1, 3))
                when (random)
                {
                    2    -> result.add(key, one(jsonPrimitives()))
                    else -> result.add(key, one(jsonArrays()))
                }

            }

            result
        }

    }

    @JvmStatic
    fun jsonArrays(): AlchemyGenerator<JsonArray>
    {
        return AlchemyGenerator {

            val arraySize = one(integers(50, 1000))
            val array = JsonArray()

            for (i in 0 until arraySize)
            {
                array.add(one(jsonPrimitives()))
            }

            array
        }
    }

    @JvmStatic
    fun jsonPrimitives(): AlchemyGenerator<JsonPrimitive>
    {
        return AlchemyGenerator {

            val random = one(integers(1, 4))

            when (random)
            {
                1    -> JsonPrimitive(one(booleans()))
                2    -> JsonPrimitive(one(positiveDoubles()))
                3    -> JsonPrimitive(one(positiveIntegers()))
                else -> JsonPrimitive(one(alphabeticStrings()))
            }
        }
    }

    @JvmStatic
    fun jsonNull(): AlchemyGenerator<JsonNull>
    {
        return AlchemyGenerator { JsonNull.INSTANCE }
    }
}
