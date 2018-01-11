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
package tech.sirwellington.alchemy.http;

import java.util.Collections;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.http.Generators.jsonNull;
import static tech.sirwellington.alchemy.http.HttpResponse.Builder.newInstance;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class HttpResponseBuilderTest
{

    private final Gson gson = Constants.getDefaultGson();

    private TestResponse response;
    private JsonElement responseBody;

    private HttpResponse.Builder instance;

    @Before
    public void setUp()
    {
        instance = Companion.newInstance();

        response = new TestResponse();
        responseBody = response.responseBody;
    }

    @Test
    public void testWithStatusCode()
    {
        int goodStatusCode = one(integers(200, 500));
        HttpResponse.Builder result = instance.withStatusCode(goodStatusCode);
        assertThat(result, notNullValue());

        int badStatusCode = one(integers(520, 10000));
        assertThrows(() -> instance.withStatusCode(badStatusCode))
                .isInstanceOf(IllegalArgumentException.class);

        int negativeStatusCode = one(negativeIntegers());
        assertThrows(() -> instance.withStatusCode(negativeStatusCode))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testUsingGson()
    {
        HttpResponse.Builder result = instance.usingGson(gson);
        assertThat(result, notNullValue());

        assertThrows(() -> instance.usingGson(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWithResponseBody()
    {
        HttpResponse.Builder result = instance.withResponseBody(responseBody);
        assertThat(result, notNullValue());

        instance.withResponseBody(one(jsonNull()));

        assertThrows(() -> instance.withResponseBody(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWithResponseHeaders()
    {
        HttpResponse.Builder result = instance.withResponseHeaders(response.responseHeaders);
        assertThat(result, notNullValue());

        //Empty Map is ok
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
        assertThat(result.equals(response), is(true));
        assertThat(response.equals(result), is(true));

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
        assertThat(result, is(response));
        assertThat(response, is(result));

    }

    @Test
    public void testMergeFromEdgeCases()
    {
        Map<String,String> headers = response.responseHeaders;
        response.responseHeaders = null;
        HttpResponse result = instance.copyFrom(response).build();
        assertThat(result, notNullValue());

        response.responseHeaders = headers;
        response.statusCode = one(negativeIntegers());
        assertThrows(() -> instance.copyFrom(response))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
