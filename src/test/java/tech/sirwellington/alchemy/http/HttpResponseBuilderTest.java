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

import com.google.gson.JsonElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class HttpResponseBuilderTest
{

    private final com.google.gson.Gson gson = Constants.DEFAULT_GSON;

    private TestResponse response;
    private JsonElement responseBody;

    private HttpResponse.Builder instance;

    @Before
    public void setUp()
    {
        instance = HttpResponse.Builder.newInstance();

        response = new TestResponse();
        responseBody = response.responseBody;
    }

    @Test
    public void testWithStatusCode()
    {
        int goodStatusCode = one(integers(200, 500));
        HttpResponse.Builder result = instance.withStatusCode(goodStatusCode);
        assertThat(result, notNullValue());

        int badStatusCode = one(integers(600, 10000));
        assertThrows(() -> instance.withStatusCode(badStatusCode))
                .isInstanceOf(IllegalArgumentException.class);

        int negativeStatusCode = one(negativeIntegers());
        assertThrows(() -> instance.withStatusCode(negativeStatusCode))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWithStatusCodeEnum()
    {
        HttpStatusCode status = HttpStatusCode.any();

        HttpResponse result = instance.withStatusCode(status).build();
        assertThat(result.status(), equalTo(status));
    }

    @Test
    public void testUsingGson()
    {
        HttpResponse.Builder result = instance.usingGson(gson);
        assertThat(result, notNullValue());
    }

    @Test
    public void testWithResponseBody()
    {
        HttpResponse.Builder result = instance.withResponseBody(responseBody);
        assertThat(result, notNullValue());

        instance.withResponseBody(one(Generators.jsonNull()));
    }

    @Test
    public void testWithResponseHeaders()
    {
        HttpResponse.Builder result = instance.withResponseHeaders(response.responseHeaders);
        assertThat(result, notNullValue());

        // Empty Map is ok
        instance.withResponseHeaders(Collections.emptyMap());
        instance.withResponseHeaders(null);
    }

    @Test
    public void testBuild()
    {
        HttpResponse result = instance
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

        HttpResponse result = instance.build();
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
        java.util.Map<String, String> headers = response.responseHeaders;
        response.responseHeaders = Maps.emptyMap();
        HttpResponse result = instance.copyFrom(response).build();
        assertThat(result, notNullValue());

        response.responseHeaders = headers;
        response.statusCode = one(negativeIntegers());
        assertThrows(() -> instance.copyFrom(response))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
