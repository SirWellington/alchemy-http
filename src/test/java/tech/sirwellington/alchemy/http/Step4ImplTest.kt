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

import com.nhaarman.mockito_kotlin.KArgumentCaptor
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.sameInstance
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step4
import tech.sirwellington.alchemy.http.Generators.validUrls
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class Step4ImplTest
{

    @Mock
    private lateinit var stateMachine: AlchemyHttpStateMachine

    @Mock
    private lateinit var request: HttpRequest

    private lateinit var requestCaptor: KArgumentCaptor<HttpRequest>

    @Mock
    private lateinit var onSuccess: OnSuccess<TestPojo>

    private lateinit var responseClass: Class<TestPojo>

    private lateinit var instance: Step4<TestPojo>

    @Before
    fun setUp()
    {
        responseClass = TestPojo::class.java

        instance = Step4Impl(stateMachine, request, responseClass)
        verifyZeroInteractions(stateMachine)

        requestCaptor = argumentCaptor()
    }

    @Repeat
    @Test
    fun testAt()
    {
        val url = one(validUrls())

        instance.at(url)

        verify(stateMachine).executeSync(requestCaptor.capture(), eq(responseClass))

        val requestMade = requestCaptor.firstValue
        assertThat(requestMade, notNullValue())
        assertThat(requestMade, not(sameInstance(request)))
        assertThat(requestMade.url, equalTo(url))
    }

    @Test
    @Throws(Exception::class)
    fun testAtWithBadArgs()
    {
        assertThrows { instance.at("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testOnSuccess()
    {
        //Edge cases
        instance.onSuccess(onSuccess)

        verify(stateMachine).jumpToStep5(request, responseClass, onSuccess)
    }

    @Test
    fun testToString()
    {
        assertThat(Strings.isNullOrEmpty(instance.toString()), equalTo(false))
    }

}
