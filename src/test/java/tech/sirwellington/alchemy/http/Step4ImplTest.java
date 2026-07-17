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
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step4;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class Step4ImplTest
{
    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Mock
    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    @Mock
    private OnSuccess<TestPojo> onSuccess;

    private Class<TestPojo> responseClass;

    private Step4<TestPojo> instance;

    @Before
    public void setUp()
    {
        responseClass = TestPojo.class;

        instance = new Step4Impl<>(stateMachine, request, responseClass);
        verifyNoInteractions(stateMachine);
    }

    @Repeat
    @Test
    public void testAt()
    {
        URL url = one(Generators.validUrls());

        instance.at(url);

        verify(stateMachine).executeSync(requestCaptor.capture(), eq(responseClass));

        var requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade, not(sameInstance(request)));
        assertThat(requestMade.url(), equalTo(url));
    }

    @Test
    public void testAtWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testOnSuccess()
    {
        // Edge cases
        instance.onSuccess(onSuccess);

        verify(stateMachine).jumpToStep5(request, responseClass, onSuccess);
    }

    @Test
    public void testToString()
    {
        assertThat(Strings.isNullOrEmpty(instance.toString()), equalTo(false));
    }
}
