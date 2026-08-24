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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
public class Step2ImplTest {
    @Mock
    private AlchemyHttpStateMachine stateMachine;

    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private JsonElement expectedBody;

    private Gson gson = Constants.DEFAULT_GSON;

    private Step2Impl instance;

    @BeforeEach
    public void setUp() {
        request = HttpRequest.Builder.newInstance().build();

        instance = new Step2Impl(request, stateMachine, gson);

        expectedBody = one(Generators.jsonObjects());
    }

    @Test
    public void testNoBody() {
        instance.noBody();

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        expectedBody = JsonNull.INSTANCE;
        var requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }

    @Test
    public void testNothing() {
        instance.nothing();

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        expectedBody = JsonNull.INSTANCE;
        var requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }

    @Test
    public void testStringBody() {
        var stringBody = gson.toJson(expectedBody);
        instance.body(stringBody);

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        var requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }

    @Test
    public void testStringBodyWhenEmpty() {
        assertThrows(() -> instance.body(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testObjectBody() {
        var pojo = TestPojo.generate();

        instance.body(pojo);

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        expectedBody = gson.toJsonTree(pojo);
        var requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }

    private void verifyRequestMade(HttpRequest requestMade) {
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.body(), equalTo(expectedBody));
    }

    @Test
    public void testToString() {
        var toString = instance.toString();
        assertThat(Strings.isNullOrEmpty(toString), equalTo(false));
    }
}
