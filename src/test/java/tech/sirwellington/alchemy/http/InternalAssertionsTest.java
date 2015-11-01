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

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.StringGenerators;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class InternalAssertionsTest
{

    @Before
    public void setUp()
    {
    }

    @Test
    public void testValidHttpStatusCode()
    {
        AlchemyAssertion<Integer> instance = InternalAssertions.validHttpStatusCode();
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
        AlchemyAssertion<Class<Object>> instanceOne = InternalAssertions.validResponseClass();
        assertThat(instanceOne, notNullValue());
        instanceOne.check(Object.class);

        //Check String
        AlchemyAssertion<Class<String>> instanceTwo = InternalAssertions.validResponseClass();
        instanceTwo.check(String.class);

        //Edge Cases
        assertThrows(() -> instanceOne.check(null))
                .isInstanceOf(FailedAssertionException.class);

        AlchemyAssertion<Class<Void>> instanceThree = InternalAssertions.validResponseClass();
        assertThrows(() -> instanceThree.check(Void.class))
                .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testRequestReady() throws MalformedURLException
    {
        AlchemyAssertion<HttpRequest> instance = InternalAssertions.requestReady();

        URL url = new URL("http://" + one(alphabeticString()));
        HttpVerb verb = mock(HttpVerb.class);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getUrl()).thenReturn(url);
        when(request.getVerb()).thenReturn(verb);

        instance.check(request);
    }

    @Test
    public void testRequestReadyEdgeCases() throws MalformedURLException
    {
        AlchemyAssertion<HttpRequest> instance = InternalAssertions.requestReady();

        //Edge cases
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        URL url = new URL("http://" + one(alphabeticString()));
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
        URL badUrl = new URL("file://" + one(alphabeticString()));
        when(request.getUrl()).thenReturn(badUrl);

        assertThrows(() -> instance.check(request))
                .isInstanceOf(FailedAssertionException.class);

    }

    @Test
    public void testValidContentType()
    {
        AlchemyAssertion<String> instance = InternalAssertions.validContentType();
        assertThat(instance, notNullValue());

        AlchemyGenerator<String> validTypes = StringGenerators.stringsFromFixedList("application/json", "text/plain");

        String contentType = one(validTypes);

        instance.check(contentType);
        instance.check(contentType + one(alphabeticString()));
    }

    @Test
    public void testValidContentTypeEdgeCases()
    {
        AlchemyAssertion<String> instance = InternalAssertions.validContentType();

        //Edge cases
        assertThrows(() -> instance.check(null))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(""))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(one(alphabeticString())))
                .isInstanceOf(FailedAssertionException.class);

        assertThrows(() -> instance.check(one(hexadecimalString(10))))
                .isInstanceOf(FailedAssertionException.class);
    }
}
