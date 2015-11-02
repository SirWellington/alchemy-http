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
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.time.ZonedDateTime.ofInstant;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.DateGenerators.anyTime;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class SerializersTest
{

    @Mock
    private Type type;

    @Mock
    private JsonSerializationContext serializationContext;

    @Mock
    private JsonDeserializationContext deserializationContext;

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

    private Date date;

    private Instant instant;
    private ZonedDateTime dateTime;

    @Before
    public void setUp()
    {
        date = one(anyTime());
        instant = Instant.ofEpochMilli(date.getTime());
        dateTime = ofInstant(instant, ZoneOffset.UTC);
    }

    @Test
    public void testDateStringSerializerSerialize()
    {
        Serializers.DateStringSerializer instance;
        instance = new Serializers.DateStringSerializer(formatter);

        //Null case
        JsonElement result = instance.serialize(null, type, serializationContext);
        assertThat(result, is(JsonNull.INSTANCE));

        String expected = formatter.format(instant);
        JsonPrimitive expectedJson = new JsonPrimitive(expected);

        result = instance.serialize(date, type, serializationContext);
        assertThat(result, is(expectedJson));
    }

    @Test
    public void testDateStringSerializerDeserialize()
    {
        Serializers.DateStringSerializer instance;
        instance = new Serializers.DateStringSerializer(formatter);

        //Null case
        Date result = instance.deserialize(null, type, deserializationContext);
        assertThat(result, nullValue());
        result = instance.deserialize(JsonNull.INSTANCE, type, deserializationContext);
        assertThat(result, nullValue());

        String string = formatter.format(instant);
        JsonPrimitive jsonString = new JsonPrimitive(string);
        result = instance.deserialize(jsonString, type, deserializationContext);
        assertThat(result, is(date));
    }

}
