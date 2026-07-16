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
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step3;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class Step3ImplTest
{
    @Mock
    private AlchemyHttpStateMachine stateMachine;

    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    @Mock
    private OnSuccess<?> onSuccess;

    private URL url;

    private Step3 instance;

    @Before
    public void setUp() throws MalformedURLException
    {
        url = one(Generators.validUrls());

        request = HttpRequest.Builder.newInstance()
                .usingUrl(url)
                .build();

        instance = new Step3Impl(stateMachine, request);

        verifyNoInteractions(stateMachine);
    }

    @Test
    public void testUsingHeader()
    {
        // Edge Cases
        assertThrows(() -> instance.usingHeader("", ""))
                .isInstanceOf(IllegalArgumentException.class);

        // Happy cases
        Map<String, String> expectedHeaders = CollectionGenerators.mapOf(alphabeticStrings(),
                hexadecimalString(10),
                20);

        for (Map.Entry<String, String> entry : expectedHeaders.entrySet())
        {
            instance = instance.usingHeader(entry.getKey(), entry.getValue());
        }

        instance.at(url);

        verify(stateMachine).executeSync(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade, not(sameInstance(request)));
        assertThat(requestMade.requestHeaders(), equalTo(expectedHeaders));

        // Adding an empty value should be ok too
        String key = one(alphabeticStrings());
        instance.usingHeader(key, "");
    }

    @Test
    public void testUsingQueryParam()
    {
        int amount = one(integers(5, 20));

        Map<String, String> strings = CollectionGenerators.mapOf(alphabeticStrings(),
                hexadecimalString(10),
                amount);

        Map<String, Integer> integers = CollectionGenerators.mapOf(alphabeticStrings(),
                smallPositiveIntegers(),
                amount);

        Map<String, Boolean> booleans = CollectionGenerators.mapOf(alphabeticStrings(),
                booleans(),
                amount);

        for (Map.Entry<String, String> entry : strings.entrySet())
        {
            instance = instance.usingQueryParam(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : integers.entrySet())
        {
            instance = instance.usingQueryParam(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Boolean> entry : booleans.entrySet())
        {
            instance = instance.usingQueryParam(entry.getKey(), entry.getValue());
        }

        Map<String, String> expected = Maps.mutableCopyOf(strings);
        // Put the integers
        for (Map.Entry<String, Integer> entry : integers.entrySet())
        {
            expected.put(entry.getKey(), entry.getValue().toString());
        }
        // Put the booleans too
        for (Map.Entry<String, Boolean> entry : booleans.entrySet())
        {
            expected.put(entry.getKey(), entry.getValue().toString());
        }

        instance.at(url);

        verify(stateMachine).executeSync(requestCaptor.capture());
        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.queryParams(), equalTo(expected));
        assertThat(requestMade, not(sameInstance(request)));
    }

    @DontRepeat
    @Test
    public void testUsingQueryParamEdgeCases()
    {
        // Edge cases
        assertThrows(() -> instance.usingQueryParam("", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFollowRedirects()
    {
        assertThrows(() -> instance.followRedirects(-10))
                .isInstanceOf(IllegalArgumentException.class);

        instance = instance.followRedirects();
        assertThat(instance, notNullValue());

        instance = instance.followRedirects(30);
        assertThat(instance, notNullValue());
    }

    @Test
    public void testAt()
    {
        // Edge Cases
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);

        instance.at(url);
        verify(stateMachine).executeSync(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.url(), equalTo(url));
        assertThat(requestMade, not(sameInstance(request)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOnSuccess()
    {
        instance.onSuccess((OnSuccess<HttpResponse>) onSuccess);

        verify(stateMachine).jumpToStep5(request, HttpResponse.class, (OnSuccess<HttpResponse>) onSuccess);
    }

    @Test
    public void testExpecting()
    {
        // Sad Cases
        assertThrows(() -> instance.expecting(Void.class))
                .isInstanceOf(IllegalArgumentException.class);

        // Happy cases
        Class<String> expectedClass = String.class;
        instance.expecting(expectedClass);
        verify(stateMachine).jumpToStep4(request, expectedClass);
    }
}
