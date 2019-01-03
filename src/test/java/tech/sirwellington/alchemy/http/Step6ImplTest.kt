/*
 * Copyright © 2019. Sir Wellington.
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

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.Step6
import tech.sirwellington.alchemy.http.Generators.validUrls
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.net.URL

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class Step6ImplTest
{

    @Mock
    private lateinit var stateMachine: AlchemyHttpStateMachine

    @Mock
    private lateinit var request: HttpRequest

    @Captor
    private lateinit var requestCaptor: ArgumentCaptor<HttpRequest>

    @Mock
    private lateinit var onSuccess: AlchemyRequestSteps.OnSuccess<Any>

    @Mock
    private lateinit var onFailure: AlchemyRequestSteps.OnFailure

    private lateinit var responseClass: Class<Any>

    private lateinit var url: URL

    private lateinit var instance: Step6<Any>

    @Before
    fun setUp()
    {
        responseClass = Any::class.java
        url = one(validUrls())

        instance = Step6Impl(stateMachine, request, responseClass, onSuccess, onFailure)
        verifyZeroInteractions(stateMachine, request, onSuccess, onFailure)
    }

    @Test
    fun testAtWithBadArgs()
    {
        //Edge cases
        assertThrows { instance.at("") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Repeat(50)
    @Test
    @Throws(Exception::class)
    fun testAt()
    {

        instance.at(url)

        val expectedRequest = HttpRequest.Builder.from(request)
                .usingUrl(url)
                .build()

        verify(stateMachine).executeAsync(expectedRequest, responseClass, onSuccess, onFailure)
    }

    @Test
    fun testToString()
    {
        val toString = instance.toString()
        assertThat(Strings.isNullOrEmpty(toString), equalTo(false))
    }

}
