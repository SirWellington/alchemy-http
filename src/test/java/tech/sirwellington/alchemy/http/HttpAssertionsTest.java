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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.StringGenerators;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.http.Generators.jsonArrays;
import static tech.sirwellington.alchemy.http.Generators.jsonObjects;
import static tech.sirwellington.alchemy.http.Generators.jsonPrimitives;
import static tech.sirwellington.alchemy.http.Generators.validUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class HttpAssertionsTest
{

    @Before
    public void setUp()
    {
    }

    public static void runTheThing()
    {

    }

    @Test
    public void testConstructor()
    {
        assertThrows(() -> HttpAssertions.class.newInstance())
                .isInstanceOf(IllegalAccessException.class);
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

        int anotherBadCode = one(integers(506, 1000));
        assertThrows(() -> instance.check(anotherBadCode))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testValidResponseClass()
    {
        //Check Object
        AlchemyAssertion<Class<Object>> instanceOne = HttpAssertions.validResponseClass();
        assertThat(instanceOne, notNullValue());
        instanceOne.check(Object.class);

        //Check String
        AlchemyAssertion<Class<String>> instanceTwo = HttpAssertions.validResponseClass();
        instanceTwo.check(String.class);

        //Edge Cases
        assertThrows(() -> instanceOne.check(null))
                .isInstanceOf(FailedAssertionException.class);

        AlchemyAssertion<Class<Void>> instanceThree = HttpAssertions.validResponseClass();
        assertThrows(() -> instanceThree.check(Void.class))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testRequestReady() throws MalformedURLException
    {
        AlchemyAssertion<HttpRequest> instance = HttpAssertions.requestReady();

        URL url = one(validUrls());
        HttpVerb verb = mock(HttpVerb.class);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getUrl()).thenReturn(url);
        when(request.getVerb()).thenReturn(verb);

        instance.check(request);
    }

    @Repeat
    @Test
    public void testRequestReadyEdgeCases() throws MalformedURLException
    {
        AlchemyAssertion<HttpRequest> instance = HttpAssertions.requestReady();

        //Edge cases
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        URL url = one(validUrls());
        HttpVerb verb = mock(HttpVerb.class);
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUrl()).thenReturn(url);
        when(request.getVerb()).thenReturn(verb);

        //Missing verb
        when(request.getVerb()).thenReturn(null);

        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);

        when(request.getVerb()).thenReturn(verb);

        //Missing URL
        when(request.getUrl()).thenReturn(null);

        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);

        //Bad URL
        URL badUrl = new URL("file://" + one(alphabeticStrings()));
        when(request.getUrl()).thenReturn(badUrl);

        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);

    }

    @Repeat(10)
    @Test
    public void testValidContentType()
    {
        AlchemyAssertion<String> instance = HttpAssertions.validContentType();
        assertThat(instance, notNullValue());

        AlchemyGenerator<String> validTypes = StringGenerators.stringsFromFixedList("application/json", "text/plain");

        String contentType = one(validTypes);

        instance.check(contentType);
        instance.check(contentType + one(alphabeticStrings()));
    }

    @Test
    public void testValidContentTypeEdgeCases()
    {
        AlchemyAssertion<String> instance = HttpAssertions.validContentType();

        //Edge cases
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(""))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(one(alphabeticStrings())))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(one(hexadecimalString(10))))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testNotNullAndHasURL()
    {
        AlchemyAssertion<HttpRequest> instance = HttpAssertions.notNullAndHasURL();
        assertThat(instance, notNullValue());

        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        //No URL
        HttpRequest request = mock(HttpRequest.class);
        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);

        URL url = one(validUrls());
        when(request.getUrl()).thenReturn(url);
        instance.check(request);

    }

    @Repeat
    @Test
    public void testJsonArray()
    {
        AlchemyAssertion<JsonElement> instance = HttpAssertions.jsonArray();
        assertThat(instance, notNullValue());

        JsonArray valid = one(jsonArrays());
        instance.check(valid);

        JsonObject object = one(jsonObjects());
        assertThrows(() -> instance.check(object))
                .isInstanceOf(FailedAssertionException.class);

        JsonPrimitive primitive = one(jsonPrimitives());
        assertThrows(() -> instance.check(primitive))
                .isInstanceOf(FailedAssertionException.class);

    }

    @Test
    public void testOkResponse()
    {
        AlchemyAssertion<HttpResponse> instance = HttpAssertions.okResponse();
        assertThat(instance, notNullValue());

        //Check with null argument
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        //Response is OK
        HttpResponse okResponse = mock(HttpResponse.class);
        when(okResponse.isOk()).thenReturn(true);
        instance.check(okResponse);

        //Response is NOT OK
        HttpResponse notOkResponse = mock(HttpResponse.class);
        when(notOkResponse.isOk()).thenReturn(false);
        assertThrows(() -> instance.check(notOkResponse))
                .isInstanceOf(FailedAssertionException.class);

    }

}
