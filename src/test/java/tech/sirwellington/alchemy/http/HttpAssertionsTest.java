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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class HttpAssertionsTest
{

    @GenerateEnum
    private RequestMethod requestMethod;

    @Before
    public void setUp()
    {
    }

    @Test
    public void testConstructor()
    {
        assertThrows(() -> HttpAssertions.class.getDeclaredConstructor().newInstance())
                .isInstanceOf(Exception.class);
    }

    @Repeat
    @Test
    public void testValidHttpStatusCode()
    {
        AlchemyAssertion<Integer> instance = HttpAssertions.validHttpStatusCode();
        assertThat(instance, notNullValue());

        int statusCode = one(integers(200, 500));
        instance.check(statusCode);

        int badStatusCode = one(integers(-100, 100));
        assertThrows(() -> instance.check(badStatusCode))
                .isInstanceOf(FailedAssertionException.class);

        int anotherBadCode = one(integers(600, Integer.MAX_VALUE));
        assertThrows(() -> instance.check(anotherBadCode))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testValidResponseClass()
    {
        // Check Object
        AlchemyAssertion<Class<Object>> instanceOne = HttpAssertions.validResponseClass();
        assertThat(instanceOne, notNullValue());
        instanceOne.check(Object.class);

        // Check String
        AlchemyAssertion<Class<String>> instanceTwo = HttpAssertions.validResponseClass();
        instanceTwo.check(String.class);

        // Edge Cases
        assertThrows(() -> instanceOne.check(null))
                .isInstanceOf(FailedAssertionException.class);

        AlchemyAssertion<Class<Void>> instanceThree = HttpAssertions.validResponseClass();
        assertThrows(() -> instanceThree.check(Void.class))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testRequestReady() throws MalformedURLException
    {
        AlchemyAssertion<HttpRequest> instance = HttpAssertions.ready();

        URL url = one(Generators.validUrls());

        HttpRequest request = mock(HttpRequest.class);
        when(request.url()).thenReturn(url);
        when(request.method()).thenReturn(requestMethod);

        instance.check(request);
    }

    @Repeat
    @Test
    public void testRequestReadyEdgeCases() throws MalformedURLException
    {
        AlchemyAssertion<HttpRequest> instance = HttpAssertions.ready();

        // Edge cases
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        URL url = one(Generators.validUrls());
        HttpRequest request = mock(HttpRequest.class);
        when(request.url()).thenReturn(url);
        when(request.method()).thenReturn(requestMethod);

        // Missing Request Method
        when(request.method()).thenReturn(null);

        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);

        when(request.method()).thenReturn(requestMethod);

        // Missing URL
        when(request.url()).thenReturn(null);

        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);

        // Bad URL
        URL badUrl = new URL("file://" + one(alphabeticStrings()));
        when(request.url()).thenReturn(badUrl);

        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);
    }

    @RepeatedTest(10)
    @Test
    public void testValidContentType()
    {
        AlchemyAssertion<String> instance = HttpAssertions.validContentType();
        assertThat(instance, notNullValue());

        String contentType = one(stringsFromFixedList(ContentTypes.APPLICATION_JSON, ContentTypes.PLAIN_TEXT));

        instance.check(contentType);
        instance.check(contentType + one(alphabeticStrings()));
    }

    @Test
    public void testValidContentTypeEdgeCases()
    {
        AlchemyAssertion<String> instance = HttpAssertions.validContentType();

        // Edge cases
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(""))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(one(alphabeticStrings())))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(one(hexadecimalString(10))))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Repeat
    @Test
    public void testJsonArray()
    {
        AlchemyAssertion<com.google.gson.JsonElement> instance = HttpAssertions.jsonArray();
        assertThat(instance, notNullValue());

        com.google.gson.JsonArray valid = one(Generators.jsonArrays());
        instance.check(valid);

        com.google.gson.JsonObject object = one(Generators.jsonObjects());
        assertThrows(() -> instance.check(object))
                .isInstanceOf(FailedAssertionException.class);

        com.google.gson.JsonPrimitive primitive = one(Generators.jsonPrimitives());
        assertThrows(() -> instance.check(primitive))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testOkResponse()
    {
        AlchemyAssertion<HttpResponse> instance = HttpAssertions.okResponse();
        assertThat(instance, notNullValue());

        // Check with null argument
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        // Response is OK
        HttpResponse okResponse = mock(HttpResponse.class);
        when(okResponse.isOk()).thenReturn(true);
        instance.check(okResponse);

        // Response is NOT OK
        HttpResponse notOkResponse = mock(HttpResponse.class);
        when(notOkResponse.isOk()).thenReturn(false);
        assertThrows(() -> instance.check(notOkResponse))
                .isInstanceOf(FailedAssertionException.class);
    }
}
