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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnFailure;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step5;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author SirWellington
 */
@AlchemyTest
public class Step5ImplTest
{
    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Mock
    private HttpRequest request;

    private Class<TestPojo> expectedClass;

    @Mock
    private OnSuccess<TestPojo> onSuccess;

    @Mock
    private OnFailure onFailure;

    private Step5<?> instance;

    @BeforeEach
    public void setUp()
    {
        expectedClass = TestPojo.class;

        instance = new Step5Impl<>(stateMachine, request, expectedClass, onSuccess);

        verifyNoInteractions(stateMachine, request, onSuccess);
    }

    @Test
    public void testOnFailure()
    {
        instance.onFailure(onFailure);

        verify(stateMachine).jumpToStep6(request, expectedClass, onSuccess, onFailure);
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(toString, notNullValue());
        assertThat(toString.isEmpty(), is(false));
    }
}
