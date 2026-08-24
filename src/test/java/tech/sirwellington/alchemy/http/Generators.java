/*
 * Copyright © 2026. Sir Wellington.
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
package tech.sirwellington.alchemy.http;

import java.net.URI;
import java.net.URL;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.NumberGenerators.*;
import static tech.sirwellington.alchemy.generator.StringGenerators.*;

/**
 * @author SirWellington
 */
final class Generators {

    private static final Logger LOG = LoggerFactory.getLogger(Generators.class);

    private Generators() {
        throw new AssertionError("non-instantiable");
    }

    static AlchemyGenerator<URL> validUrls() {
        return () -> {
            var protocols = stringsFromFixedList("https://", "http://");
            var protocol = one(protocols);
            var host = one(alphanumericStrings(10));
            var uri = protocol + host;

            try {
                return new URI(uri).toURL();
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    static AlchemyGenerator<JsonElement> jsonElements() {
        return () ->
        {
            int random = one(integers(1, 5));

            return switch (random) {
                case 1 -> one(jsonObjects());
                case 2 -> one(jsonArrays());
                case 3 -> one(jsonNull());
                default -> one(jsonPrimitives());
            };
        };
    }

    static AlchemyGenerator<JsonObject> jsonObjects() {
        return () -> {
            var result = new JsonObject();

            var elements = one(integers(10, 50));

            for (int i = 0; i < elements; i++) {
                var key = one(alphabeticStrings());

                var random = one(integers(1, 3));
                switch (random) {
                    case 2  -> result.add(key, one(jsonPrimitives()));
                    default -> result.add(key, one(jsonArrays()));
                }
            }

            return result;
        };
    }

    static AlchemyGenerator<JsonArray> jsonArrays() {
        return () -> {
            var arraySize = one(integers(50, 1000));
            var array = new JsonArray();

            for (int i = 0; i < arraySize; i++) {
                array.add(one(jsonPrimitives()));
            }

            return array;
        };
    }

    static AlchemyGenerator<JsonPrimitive> jsonPrimitives() {
        return () -> {
            var random = one(integers(1, 4));

            return switch (random) {
                case 1  -> new JsonPrimitive(one(booleans()));
                case 2  -> new JsonPrimitive(one(positiveDoubles()));
                case 3  -> new JsonPrimitive(one(positiveIntegers()));
                default -> new JsonPrimitive(one(alphabeticStrings()));
            };
        };
    }

    static AlchemyGenerator<JsonNull> jsonNull() {
        return () -> JsonNull.INSTANCE;
    }
}
