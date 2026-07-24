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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.strings;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
public class HttpResponseTest {

    private final Gson gson = Constants.DEFAULT_GSON;

    private TestResponse first;
    private TestResponse second;

    private TestPojo pojo;
    private JsonObject pojoAsJson;

    private HttpResponse.Builder builder;

    @BeforeEach
    public void setUp() {
        first = new TestResponse();
        second = first.copy();

        pojo = TestPojo.generate();
        pojoAsJson = gson.toJsonTree(pojo).getAsJsonObject();

        builder = HttpResponse.builder().copyFrom(first);
    }

    @Test
    public void testStatusCode() {
        var instance = builder.build();
        assertThat(instance.statusCode(), equalTo(first.statusCode));
    }

    @Test
    public void testStatus() {
        var instance = builder.build();
        var status = instance.statusCode();
        var expected = HttpStatusCode.forCode(status);
        assertThat(instance.status(), equalTo(expected));
    }

    @Test
    public void testNotFound() {
        var status = HttpStatusCode.any();
        var code = status.getCode();
        var instance = builder.withStatusCode(code).build();

        var expected = (status == HttpStatusCode.NOT_FOUND);
        assertThat(instance.notFound(), equalTo(expected));
    }

    @Test
    public void testIsOk() {
        first.statusCode = one(integers(200, 209));
        builder = builder.copyFrom(first);

        var instance = builder.build();
        assertThat(instance.isOk(), equalTo(true));
    }

    @Test
    public void testIsOkWhenNotOk() {
        first.statusCode = one(integers(400, 506));
        builder = builder.copyFrom(first);

        var instance = builder.build();
        assertThat(instance.isOk(), equalTo(false));
    }

    @Test
    public void testResponseHeaders() {
        var instance = builder.build();
        assertThat(instance.responseHeaders(), equalTo(first.responseHeaders));

        var value = one(alphabeticStrings());
        var headers = instance.responseHeaders();
        assertThrows(() -> headers.put(value, value));
    }

    @Test
    public void testBody() {
        var instance = builder.build();
        assertThat(instance.body(), equalTo(first.responseBody));
    }

    @Test
    public void testBodyAsString() {
        var instance = builder.build();
        var asString = instance.bodyAsString();
        var expected = first.responseBody.toString();
        assertThat(asString, equalTo(expected));
    }

    @Test
    public void testBodyAs() {
        first.responseBody = pojoAsJson;
        var instance = builder.copyFrom(first).build();

        var result = instance.bodyAs(TestPojo.class);
        assertThat(result.equals(pojo), equalTo(true));
    }

    @Test
    public void testBodyAsArrayOf() {
        var pojos = CollectionGenerators.listOf(TestPojo::generate);
        var jsonArray = gson.toJsonTree(pojos);
        assertThat(jsonArray.isJsonArray(), equalTo(true));
        first.responseBody = jsonArray;

        var instance = builder.copyFrom(first).build();
        var result = instance.bodyAsArrayOf(TestPojo.class);
        assertThat(result, equalTo(pojos));
    }

    @Test
    public void testEqualsWhenTrue() {
        assertBothEquals();
    }

    @Test
    public void testEqualsWhenStatusCodeDifferent() {
        do {
            second.statusCode = one(integers(200, 500));
        }
        while (second.statusCode == first.statusCode);

        assertBothDifferent();
    }

    @Test
    public void testEqualsWhenResponseHeadersDifferent() {
        do {
            second.responseHeaders = CollectionGenerators.mapOf(
                strings(10),
                strings(10),
                20
            );
        }
        while (second.responseHeaders.equals(first.responseHeaders));
    }

    @Test
    public void testEqualsWhenResponseBodyDifferent() {
        do {
            second.responseBody = one(Generators.jsonElements());
        }
        while (second.responseBody.equals(first.responseBody));
    }

    private void assertBothEquals() {
        assertThat(second, equalTo(first));
        assertThat(first.equals(second), equalTo(true));
        assertThat(second.equals(first), equalTo(true));
    }

    private void assertBothDifferent() {
        assertThat(second, not(equalTo(first)));
        assertThat(first.equals(second), equalTo(false));
        assertThat(second.equals(first), equalTo(false));
    }

    @Test
    public void testBuilder() {
        assertThat(HttpResponse.builder(), notNullValue());
    }
}
