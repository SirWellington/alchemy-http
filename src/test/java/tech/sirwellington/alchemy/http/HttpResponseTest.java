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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.strings;
import static tech.sirwellington.alchemy.http.Generators.jsonElements;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class HttpResponseTest
{

    private final Gson gson = Constants.getDefaultGson();

    private TestResponse first;
    private TestResponse second;

    private TestPojo pojo;
    private JsonObject pojoAsJson;

    private HttpResponse.Builder builder;

    @Before
    public void setUp()
    {
        first = new TestResponse();
        second = first.copy();

        pojo = TestPojo.generate();
        pojoAsJson = gson.toJsonTree(pojo).getAsJsonObject();

        builder = HttpResponse.builder().mergeFrom(first);
    }

    @Test
    public void testStatusCode()
    {
        HttpResponse instance = builder.build();
        assertThat(instance.statusCode(), is(first.statusCode));
    }

    @Test
    public void testIsOk()
    {
        first.statusCode = one(integers(200, 209));
        builder = builder.mergeFrom(first);

        HttpResponse instance = builder.build();
        assertThat(instance.isOk(), is(true));
    }

    @Test
    public void testIsOkWhenNotOk()
    {
        first.statusCode = one(integers(400, 506));
        builder = builder.mergeFrom(first);

        HttpResponse instance = builder.build();
        assertThat(instance.isOk(), is(false));
    }

    @Test
    public void testResponseHeaders()
    {
        HttpResponse instance = builder.build();
        assertThat(instance.responseHeaders(), is(first.responseHeaders));

        String value = one(alphabeticStrings());
        assertThrows(() -> instance.responseHeaders().put(value, value));
    }

    @Test
    public void testBody()
    {
        HttpResponse instance = builder.build();
        assertThat(instance.body(), is(first.responseBody));
    }

    @Test
    public void testBodyAsString()
    {
        HttpResponse instance = builder.build();
        String asString = instance.bodyAsString();
        String expected = first.responseBody.toString();
        assertThat(asString, is(expected));
    }

    @Test
    public void testBodyAs()
    {
        first.responseBody = pojoAsJson;
        HttpResponse instance = builder.mergeFrom(first).build();

        TestPojo result = instance.bodyAs(TestPojo.class);
        assertThat(result.equals(pojo), is(true));
    }

    @Test
    public void testBodyAsArrayOf()
    {
        List<TestPojo> pojos = listOf(TestPojo::generate);
        JsonElement jsonArray = gson.toJsonTree(pojos);
        assertThat(jsonArray.isJsonArray(), is(true));
        first.responseBody = jsonArray;
        
        HttpResponse instance = builder.mergeFrom(first).build();
        List<TestPojo> result = instance.bodyAsArrayOf(TestPojo.class);
        assertThat(result, is(pojos));
    }

    @Test
    public void testEqualsWhenTrue()
    {
        assertBothEquals();
    }

    @Test
    public void testEqualsWhenStatusCodeDifferent()
    {
        do
        {
            second.statusCode = one(integers(200, 500));
        }
        while (second.statusCode == first.statusCode);
        assertBothDifferent();
    }

    @Test
    public void testEqualsWhenResponseHeadersDifferent()
    {
        do
        {
            second.responseHeaders = mapOf(strings(10), strings(10), 20);
        }
        while (second.responseHeaders.equals(first.responseHeaders));
    }

    @Test
    public void testEqualsWhenResponseBodyDifferent()
    {
        do
        {
            second.responseBody = one(jsonElements());
        }
        while (second.responseBody.equals(first.responseBody));
    }

    private void assertBothEquals()
    {
        assertThat(second, is(first));
        assertThat(first.equals(second), is(true));
        assertThat(second.equals(first), is(true));
    }

    private void assertBothDifferent()
    {
        assertThat(second, not(first));
        assertThat(first.equals(second), is(false));
        assertThat(second.equals(first), is(false));
    }

    @Test
    public void testBuilder()
    {
        assertThat(HttpResponse.builder(), notNullValue());
    }

}
