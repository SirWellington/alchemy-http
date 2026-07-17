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
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.*;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
public class HttpAssertionsTest {

    private RequestMethod requestMethod;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testConstructor() {
        assertThrows(
            () -> HttpAssertions.class.getDeclaredConstructor().newInstance()
        );
    }

    @Test
    public void testValidHttpStatusCode() {
        var instance = HttpAssertions.validHttpStatusCode();
        assertThat(instance, notNullValue());

        var statusCode = one(integers(200, 500));
        instance.check(statusCode);

        var badStatusCode = one(integers(-100, 100));
        assertThrows(() -> instance.check(badStatusCode))
            .isInstanceOf(FailedAssertionException.class);

        var anotherBadCode = one(integers(600, Integer.MAX_VALUE));
        assertThrows(() -> instance.check(anotherBadCode))
            .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidResponseClass() {
        // Check Object
        var instanceOne = HttpAssertions.validResponseClass();
        assertThat(instanceOne, notNullValue());
        instanceOne.check(Object.class);

        // Check String
        var instanceTwo = HttpAssertions.validResponseClass();
        instanceTwo.check((Class<Object>) (Class<?>) String.class);

        // Edge Cases
        assertThrows(() -> instanceOne.check(null))
            .isInstanceOf(FailedAssertionException.class);

        var instanceThree = HttpAssertions.validResponseClass();
        assertThrows(() -> instanceThree.check((Class<Object>) (Class<?>) Void.class))
            .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testRequestReady() throws MalformedURLException {
        var instance = HttpAssertions.ready();

        var url = one(Generators.validUrls());

        var request = mock(HttpRequest.class);
        when(request.url()).thenReturn(url);
        when(request.method()).thenReturn(requestMethod);

        instance.check(request);
    }

    @Test
    public void testRequestReadyEdgeCases() throws Exception {
        var instance = HttpAssertions.ready();

        // Edge cases
        assertThrows(() -> instance.check(null))
            .isInstanceOf(FailedAssertionException.class);

        var url = one(Generators.validUrls());
        var request = mock(HttpRequest.class);
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
        var badUrl = new URI("file://" + one(alphabeticStrings()));
        when(request.url()).thenReturn(badUrl.toURL());

        assertThrows(() -> instance.check(request))
            .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testValidContentType() {
        var instance = HttpAssertions.validContentType();
        assertThat(instance, notNullValue());

        var contentType = one(stringsFromFixedList(
            ContentTypes.APPLICATION_JSON,
            ContentTypes.PLAIN_TEXT
        ));

        instance.check(contentType);
        instance.check(contentType + one(alphabeticStrings()));
    }

    @Test
    public void testValidContentTypeEdgeCases() {
        var instance = HttpAssertions.validContentType();

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

    @Test
    public void testJsonArray() {
        var instance = HttpAssertions.jsonArray();
        assertThat(instance, notNullValue());

        var valid = one(Generators.jsonArrays());
        instance.check(valid);

        var object = one(Generators.jsonObjects());
        assertThrows(() -> instance.check(object))
            .isInstanceOf(FailedAssertionException.class);

        var primitive = one(Generators.jsonPrimitives());
        assertThrows(() -> instance.check(primitive))
            .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testOkResponse() {
        var instance = HttpAssertions.okResponse();
        assertThat(instance, notNullValue());

        // Check with null argument
        assertThrows(() -> instance.check(null))
            .isInstanceOf(FailedAssertionException.class);

        // Response is OK
        var okResponse = mock(HttpResponse.class);
        when(okResponse.isOk()).thenReturn(true);
        instance.check(okResponse);

        // Response is NOT OK
        var notOkResponse = mock(HttpResponse.class);
        when(notOkResponse.isOk()).thenReturn(false);
        assertThrows(() -> instance.check(notOkResponse))
            .isInstanceOf(FailedAssertionException.class);
    }
}
