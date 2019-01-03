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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
class AlchemyHttpTest
{

    @Mock
    private lateinit var stateMachine: AlchemyHttpStateMachine

    @Mock
    private lateinit var step1: AlchemyRequestSteps.Step1

    @Mock
    private lateinit var executor: Executor

    private lateinit var defaultHeaders: Map<String, String>

    @GenerateString
    private lateinit var headerKey: String

    @GenerateString
    private lateinit var headerValue: String

    private lateinit var instance: AlchemyHttpImpl

    @Before
    fun setUp()
    {
        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(),
                                                    alphabeticStrings(),
                                                    20)

        instance = AlchemyHttpImpl(defaultHeaders, stateMachine)
    }

    @Repeat(100)
    @Test
    fun testUsingDefaultHeader()
    {
        val result = instance.usingDefaultHeader(headerKey, headerValue)
        assertThat(result, notNullValue())
        assertTrue(result.defaultHeaders.containsKey(headerKey))
        assertEquals(result.defaultHeaders[headerKey], headerValue)
    }

    @Test
    @Throws(Exception::class)
    fun testUsingDefaultHeaderWithEmptyKey()
    {
        assertThrows { instance.usingDefaultHeader("", headerValue) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testGetDefaultHeaders()
    {
        val result = instance.defaultHeaders
        assertThat(result, equalTo(defaultHeaders))
    }

    @Test
    fun testGo()
    {
        whenever(stateMachine.begin(any())).thenReturn(step1)

        val step = instance.go()

        assertThat(step, equalTo(step1))

        val captor = argumentCaptor<HttpRequest>()

        verify(stateMachine).begin(captor.capture())

        val request = captor.firstValue
        assertThat(request, notNullValue())
        assertThat(request.method, notNullValue())
        assertThat(request.requestHeaders, notNullValue())
    }

    @Test
    fun testNewDefaultInstance()
    {
        val result = AlchemyHttp.newDefaultInstance()
        assertThat(result, notNullValue())
    }

    @Test
    fun testNewInstance()
    {

        val result = AlchemyHttp.newInstance(executor, defaultHeaders)
        assertThat(result, notNullValue())

        //Edge cases
        assertThrows { AlchemyHttp.newInstance(executor, defaultHeaders, -1, TimeUnit.SECONDS) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testNewBuilder()
    {
        val result = AlchemyHttp.newBuilder()
        assertThat(result, notNullValue())

        val client = result
                .usingExecutor(executor)
                .usingDefaultHeaders(defaultHeaders)
                .build()

        assertThat(client, notNullValue())

        defaultHeaders.forEach { key, value ->
            assertTrue(client.defaultHeaders[key] == value)
        }
    }

}
