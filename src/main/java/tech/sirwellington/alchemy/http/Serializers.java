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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
class Serializers
{

    private final static Logger LOG = LoggerFactory.getLogger(Serializers.class);

    /**
     * Serializes {@linkplain Date Dates} as a String using the given formatter.
     */
    static class DateStringSerializer implements JsonSerializer<Date>, JsonDeserializer<Date>
    {

        private DateTimeFormatter formatter;

        DateStringSerializer()
        {
            this(DateTimeFormatter.ISO_INSTANT);
        }

        DateStringSerializer(DateTimeFormatter formatter)
        {
            checkThat(formatter).is(notNull());

            this.formatter = formatter;
        }

        @Override
        public JsonElement serialize(Date date, Type typeOfSrc, JsonSerializationContext context)
        {
            if (date == null)
            {
                return JsonNull.INSTANCE;
            }

            Instant instant = Instant.ofEpochMilli(date.getTime());
            String string = formatter.format(instant);
            return new JsonPrimitive(string);
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if (json == null || json instanceof JsonNull)
            {
                return null;
            }

            if (!json.isJsonPrimitive())
            {
                throw new JsonParseException("Expecting a String to deserialize. Instead: " + json);
            }

            String string = json.getAsString();

            try
            {
                Instant instant = Instant.from(formatter.parse(string));
                return new Date(instant.toEpochMilli());
            }
            catch (Exception ex)
            {
                throw new JsonParseException("Could not deserialize JSON: " + json + " using pattern: " + formatter.toString(), ex);
            }
        }

    }
}
