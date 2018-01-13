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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequest.*;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class Step5ImplTest
{

    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Mock
    private HttpRequest request;

    private Class<?> expectedClass;

    @Mock
    private OnSuccess onSuccess;

    @Mock
    private OnFailure onFailure;

    private Step5 instance;

    @Before
    public void setUp()
    {
        expectedClass = String.class;

        instance = new Step5Impl(stateMachine, request, expectedClass, onSuccess);

        verifyZeroInteractions(stateMachine, request, onSuccess);

    }

    @Test
    public void testConstructor()
    {
        assertThrows(() -> new Step5Impl<>(null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Step5Impl<>(stateMachine, request, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Step5Impl<>(stateMachine, request, expectedClass, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Step5Impl<>(stateMachine, request, expectedClass, null))
                .isInstanceOf(IllegalArgumentException.class);

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
        assertFalse(toString.isEmpty());
    }

}
