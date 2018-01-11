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
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step3;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.http.Generators.validUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
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
    private OnSuccess onSuccess;

    private URL url;

    private Step3 instance;

    @Before
    public void setUp() throws MalformedURLException
    {
        url = one(validUrls());

        request = HttpRequest.Builder.newInstance()
                .usingUrl(url)
                .build();

        instance = new Step3Impl(stateMachine, request);

        verifyZeroInteractions(stateMachine);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new Step3Impl(stateMachine, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Step3Impl(null, request))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testUsingHeader()
    {
        //Edge Cases
        assertThrows(() -> instance.usingHeader("", ""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingHeader(null, ""))
                .isInstanceOf(IllegalArgumentException.class);

        //Happy cases
        Map<String, String> expectedHeaders = mapOf(alphabeticStrings(), hexadecimalString(10), 20);
        for (Map.Entry<String, String> header : expectedHeaders.entrySet())
        {
            instance = instance.usingHeader(header.getKey(), header.getValue());
        }

        instance.at(url);

        verify(stateMachine).executeSync(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade, not(sameInstance(request)));
        assertThat(requestMade.getRequestHeaders(), is(expectedHeaders));

        //Adding an empty value should be ok too
        String key = one(alphabeticStrings());
        instance.usingHeader(key, "");
    }

    @Test
    public void testUsingQueryParam()
    {
        int amount = one(integers(5, 20));

        Map<String, String> strings = mapOf(alphabeticStrings(), hexadecimalString(10), amount);
        Map<String, Integer> integers = mapOf(alphabeticStrings(), smallPositiveIntegers(), amount);
        Map<String, Boolean> booleans = mapOf(alphabeticStrings(), booleans(), amount);

        for (Map.Entry<String, String> param : strings.entrySet())
        {
            instance = instance.usingQueryParam(param.getKey(), param.getValue());
        }

        for (Map.Entry<String, Integer> param : integers.entrySet())
        {
            instance = instance.usingQueryParam(param.getKey(), param.getValue());
        }

        for (Map.Entry<String, Boolean> param : booleans.entrySet())
        {
            instance = instance.usingQueryParam(param.getKey(), param.getValue());
        }

        Map<String, String> expected = Maps.mutableCopyOf(strings);
        //Put the integers
        integers.forEach((k, v) -> expected.put(k, String.valueOf(v)));
        //Put the booleans too
        booleans.forEach((k, v) -> expected.put(k, String.valueOf(v)));

        instance.at(url);

        verify(stateMachine).executeSync(requestCaptor.capture());
        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.getQueryParams(), is(expected));
        assertThat(requestMade, not(sameInstance(request)));
    }

    @DontRepeat
    @Test
    public void testUsingQueryParamEdgeCases()
    {
        //Edge cases
        assertThrows(() -> instance.usingQueryParam(null, ""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingQueryParam(null, 1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingQueryParam(null, true))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingQueryParam("", ""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingQueryParam("", (Number) null))
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
        //Edge Cases
        assertThrows(() -> instance.at((URL) null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);

        instance.at(url);
        verify(stateMachine).executeSync(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.getUrl(), is(url));
        assertThat(requestMade, not(sameInstance(request)));
    }

    @Test
    public void testOnSuccess()
    {
        assertThrows(() -> instance.onSuccess(null))
                .isInstanceOf(IllegalArgumentException.class);

        instance.onSuccess(onSuccess);

        verify(stateMachine).jumpToStep5(request, HttpResponse.class, onSuccess);
    }

    @Test
    public void testExpecting()
    {
        //Sad Cases
        assertThrows(() -> instance.expecting(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.expecting(Void.class))
                .isInstanceOf(IllegalArgumentException.class);

        //Happy cases
        Class<?> expectedClass = String.class;
        instance.expecting(expectedClass);
        verify(stateMachine).jumpToStep4(request, expectedClass);
    }

}
