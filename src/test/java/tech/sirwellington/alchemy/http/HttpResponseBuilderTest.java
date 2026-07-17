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

import java.util.Collections;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static org.hamcrest.Matchers.notNullValue;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
public class HttpResponseBuilderTest
{

    private final com.google.gson.Gson gson = Constants.DEFAULT_GSON;

    private TestResponse response;
    private JsonElement responseBody;

    private HttpResponse.Builder instance;

    @BeforeEach
    public void setUp()
    {
        instance = HttpResponse.Builder.newInstance();

        response = new TestResponse();
        responseBody = response.responseBody;
    }

    @Test
    public void testWithStatusCode()
    {
        var goodStatusCode = one(integers(200, 500));
        var result = instance.withStatusCode(goodStatusCode);
        assertThat(result, notNullValue());

        var badStatusCode = one(integers(600, 10000));
        assertThrows(() -> instance.withStatusCode(badStatusCode))
                .isInstanceOf(IllegalArgumentException.class);

        var negativeStatusCode = one(negativeIntegers());
        assertThrows(() -> instance.withStatusCode(negativeStatusCode))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWithStatusCodeEnum()
    {
        var status = HttpStatusCode.any();

        var result = instance.withStatusCode(status).build();
        assertThat(result.status(), equalTo(status));
    }

    @Test
    public void testUsingGson()
    {
        var result = instance.usingGson(gson);
        assertThat(result, notNullValue());
    }

    @Test
    public void testWithResponseBody()
    {
        var result = instance.withResponseBody(responseBody);
        assertThat(result, notNullValue());

        instance.withResponseBody(one(Generators.jsonNull()));
    }

    @Test
    public void testWithResponseHeaders()
    {
        var result = instance.withResponseHeaders(response.responseHeaders);
        assertThat(result, notNullValue());

        // Empty Map is ok
        instance.withResponseHeaders(Collections.emptyMap());
        instance.withResponseHeaders(null);
    }

    @Test
    public void testBuild()
    {
        var result = instance
                .withResponseBody(responseBody)
                .withResponseHeaders(response.responseHeaders)
                .withStatusCode(response.statusCode)
                .build();

        assertThat(result, notNullValue());
        assertThat(result.equals(response), equalTo(true));
        assertThat(response.equals(result), equalTo(true));
    }

    @Test
    public void testBuildMissingStatusCode()
    {
        instance.withResponseBody(responseBody)
                .withResponseHeaders(response.responseHeaders);

        assertThrows(() -> instance.build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testMergeFrom()
    {
        instance.copyFrom(response);

        var result = instance.build();
        assertThat(result, equalTo(response));
        assertThat(response.equals(result), equalTo(true));
    }

    @Test
    public void testSetResponseHeadersWithBadArgs() throws Exception
    {
        // Kotlin's lateinit var throws on null assignment.
        // In Java, we test that building with null responseHeaders in a response works gracefully.
        response.responseHeaders = null;
        // copyFrom should handle null headers without error
        instance.copyFrom(response);
    }

    @Test
    public void testMergeFromEdgeCases()
    {
        var headers = response.responseHeaders;
        response.responseHeaders = Maps.emptyMap();
        var result = instance.copyFrom(response).build();
        assertThat(result, notNullValue());

        response.responseHeaders = headers;
        response.statusCode = one(negativeIntegers());
        assertThrows(() -> instance.copyFrom(response))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
