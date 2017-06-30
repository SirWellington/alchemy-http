/*
 * Copyright 2015 SirWellington Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.StringGenerators;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveDoubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
class Generators
{

    private final static Logger LOG = LoggerFactory.getLogger(Generators.class);

    static AlchemyGenerator<URL> validUrls()
    {
        return () ->
        {
            AlchemyGenerator<String> protocols = StringGenerators.stringsFromFixedList("https://", "http://");
            String protocol = one(protocols);
            String uri = protocol + one(alphabeticStrings());

            try
            {
                return new URL(uri);
            }
            catch (MalformedURLException ex)
            {
                throw new RuntimeException(ex);
            }
        };
    }

    static AlchemyGenerator<JsonElement> jsonElements()
    {
        return () ->
        {
            int random = one(integers(1, 5));
            switch (random)
            {
                case 1:
                    return jsonObjects().get();
                case 2:
                    return jsonArrays().get();
                case 3:
                    return jsonNull().get();
                default:
                    return jsonPrimitives().get();
            }
        };
    }

    static AlchemyGenerator<JsonObject> jsonObjects()
    {
        return () ->
        {
            JsonObject object = new JsonObject();

            int elements = one(integers(10, 50));

            for (int i = 0; i < elements; ++i)
            {
                String key = one(alphabeticStrings());

                int random = one(integers(1, 3));
                switch (random)
                {
                    case 2:
                        object.add(key, one(jsonPrimitives()));
                        break;
                    default:
                        object.add(key, one(jsonArrays()));
                        break;
                }

            }

            return object;
        };

    }

    static AlchemyGenerator<JsonArray> jsonArrays()
    {
        return () ->
        {

            int arraySize = one(integers(50, 1000));
            JsonArray array = new JsonArray();

            for (int i = 0; i < arraySize; ++i)
            {
                array.add(one(jsonPrimitives()));
            }

            return array;
        };
    }

    static AlchemyGenerator<JsonPrimitive> jsonPrimitives()
    {
        return () ->
        {
            int random = one(integers(1, 4));
            switch (random)
            {
                case 1:
                    return new JsonPrimitive(one(booleans()));
                case 2:
                    return new JsonPrimitive(one(positiveDoubles()));
                case 3:
                    return new JsonPrimitive(one(positiveIntegers()));
                default:
                    return new JsonPrimitive(one(alphabeticStrings()));
            }
        };
    }

    static AlchemyGenerator<JsonNull> jsonNull()

    {
        return () -> JsonNull.INSTANCE;
    }
}
