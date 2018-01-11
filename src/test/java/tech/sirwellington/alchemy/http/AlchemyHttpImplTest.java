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

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat(100)
public class AlchemyHttpImplTest
{

    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private Map<String, String> defaultHeaders;

    private AlchemyHttp instance;

    @Before
    public void setUp()
    {

        defaultHeaders = mapOf(alphabeticStrings(), alphabeticStrings(), 20);

        instance = new AlchemyHttpImpl(defaultHeaders, stateMachine);
        verifyZeroInteractions(stateMachine);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new AlchemyHttpImpl(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyHttpImpl(null, stateMachine))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AlchemyHttpImpl(defaultHeaders, null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testDefaultHeadersArePassedToStateMachine()
    {
        AlchemyRequest.Step1 result = instance.go();

        verify(stateMachine).begin(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.getRequestHeaders(), is(defaultHeaders));
    }

    @Test
    public void testUsingDefaultHeader()
    {
        String key = one(alphabeticStrings());
        String value = one(alphabeticStrings());

        AlchemyHttp result = instance.usingDefaultHeader(key, value);
        assertThat(result, notNullValue());
        assertThat(result, not(sameInstance(instance)));

        result.go();
        verify(stateMachine).begin(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());

        Map<String, String> expectedHeaders = Maps.mutableCopyOf(defaultHeaders);
        expectedHeaders.put(key, value);
        assertThat(requestMade.getRequestHeaders(), is(expectedHeaders));

    }

    @Test
    public void testUsingDefaultHeaderEdgeCase()
    {
        String key = one(alphabeticStrings());
        String value = one(alphabeticStrings());

        assertThrows(() -> instance.usingDefaultHeader(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingDefaultHeader("", ""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingDefaultHeader("", value))
                .isInstanceOf(IllegalArgumentException.class);

        //Key alone is OK
        instance.usingDefaultHeader(key, "");
    }

    @Test
    public void testGo()
    {
        AlchemyRequest.Step1 result = instance.go();
        verify(stateMachine).begin(any(HttpRequest.class));
    }

    @Test
    public void testGetDefaultHeaders()
    {
        Map<String, String> result = instance.getDefaultHeaders();
        assertThat(result, is(defaultHeaders));

        assertThrows(() -> result.clear());
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(toString, not(isEmptyOrNullString()));
    }
}
