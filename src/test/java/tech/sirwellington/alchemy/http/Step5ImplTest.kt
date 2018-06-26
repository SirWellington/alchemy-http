/*
 * Copyright © 2018. Sir Wellington.
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
package tech.sirwellington.alchemy.http

import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnFailure
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step5
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class Step5ImplTest
{

    @Mock
    private lateinit var stateMachine: AlchemyHttpStateMachine

    @Mock
    private lateinit var  request: HttpRequest

    private lateinit var  expectedClass: Class<TestPojo>

    @Mock
    private lateinit var  onSuccess: OnSuccess<TestPojo>

    @Mock
    private lateinit var  onFailure: OnFailure

    private lateinit var  instance: Step5<*>

    @Before
    fun setUp()
    {
        expectedClass = TestPojo::class.java

        instance = Step5Impl(stateMachine, request, expectedClass, onSuccess)

        verifyZeroInteractions(stateMachine, request, onSuccess)

    }

    @Test
    fun testOnFailure()
    {
        instance.onFailure(onFailure)

        verify(stateMachine).jumpToStep6(request, expectedClass, onSuccess, onFailure)
    }

    @Test
    fun testToString()
    {
        val toString = instance.toString()
        assertThat(toString, notNullValue())
        assertFalse(toString.isEmpty())
    }

}
