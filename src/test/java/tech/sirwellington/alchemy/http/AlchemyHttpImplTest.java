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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
public class AlchemyHttpImplTest {
    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private Map<String, String> defaultHeaders;

    private AlchemyHttp instance;

    @BeforeEach
    public void setUp() {
        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 20);
        instance = new AlchemyHttpImpl(defaultHeaders, stateMachine);
        verifyNoInteractions(stateMachine);
    }

    @Test
    public void testDefaultHeadersArePassedToStateMachine() {
        instance.go();

        verify(stateMachine).begin(requestCaptor.capture());

        var requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.requestHeaders(), equalTo(defaultHeaders));
    }

    @Test
    public void testUsingDefaultHeader() {
        var key = one(alphabeticStrings());
        var value = one(alphabeticStrings());

        AlchemyHttp result = instance.usingDefaultHeader(key, value);
        assertThat(result, notNullValue());
        assertThat(result, not(sameInstance(instance)));

        result.go();
        verify(stateMachine).begin(requestCaptor.capture());

        var requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());

        var expectedHeaders = new HashMap<>(defaultHeaders);
        expectedHeaders.put(key, value);
        assertThat(requestMade.requestHeaders(), equalTo(expectedHeaders));
    }

    @Test
    public void testUsingDefaultHeaderEdgeCase() {
        var key = one(alphabeticStrings());
        var value = one(alphabeticStrings());

        assertThrows(() -> instance.usingDefaultHeader("", ""))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.usingDefaultHeader("", value))
            .isInstanceOf(IllegalArgumentException.class);

        // Key alone is OK
        instance.usingDefaultHeader(key, "");
    }

    @Test
    public void testGo() {
        instance.go();
        verify(stateMachine).begin(any());
    }

    @Test
    public void testGetDefaultHeaders() {
        var result = instance.getDefaultHeaders();
        assertThat(result, equalTo(defaultHeaders));

        assertThrows(() -> result.clear());
    }

    @Test
    public void testToString() {
        var toString = instance.toString();
        assertThat(toString, not(isEmptyOrNullString()));
    }
}
