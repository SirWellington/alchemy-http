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

import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step6;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class Step6ImplTest
{
    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Mock
    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    @Mock
    private AlchemyRequestSteps.OnSuccess<Object> onSuccess;

    @Mock
    private AlchemyRequestSteps.OnFailure onFailure;

    private Class<Object> responseClass;

    private URL url;

    private Step6<Object> instance;

    @Before
    public void setUp()
    {
        responseClass = Object.class;
        url = one(Generators.validUrls());

        instance = new Step6Impl<>(stateMachine, request, responseClass, onSuccess, onFailure);
        verifyNoInteractions(stateMachine, request, onSuccess, onFailure);
    }

    @Test
    public void testAtWithBadArgs()
    {
        // Edge cases
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @RepeatedTest(50)
    @Test
    public void testAt() throws Exception
    {
        instance.at(url);

        HttpRequest expectedRequest = HttpRequest.Builder.from(request)
                .usingUrl(url)
                .build();

        verify(stateMachine).executeAsync(expectedRequest, responseClass, onSuccess, onFailure);
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(Strings.isNullOrEmpty(toString), equalTo(false));
    }
}
