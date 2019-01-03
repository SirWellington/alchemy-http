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

import com.nhaarman.mockito_kotlin.*
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat(100)
class AlchemyHttpImplTest
{

    @Mock
    private lateinit var  stateMachine: AlchemyHttpStateMachine

    private lateinit var  requestCaptor: KArgumentCaptor<HttpRequest>

    private lateinit var  defaultHeaders: Map<String, String>

    private lateinit var  instance: AlchemyHttp

    @Before
    fun setUp()
    {

        defaultHeaders = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 20)

        instance = AlchemyHttpImpl(defaultHeaders, stateMachine)
        verifyZeroInteractions(stateMachine)

        requestCaptor = argumentCaptor()
    }

    @Test
    fun testDefaultHeadersArePassedToStateMachine()
    {
        val result = instance.go()

        verify(stateMachine).begin(requestCaptor.capture())

        val requestMade = requestCaptor.firstValue
        assertThat(requestMade, notNullValue())
        assertThat(requestMade.requestHeaders, equalTo(defaultHeaders))
    }

    @Test
    fun testUsingDefaultHeader()
    {
        val key = one(alphabeticStrings())
        val value = one(alphabeticStrings())

        val result = instance.usingDefaultHeader(key, value)
        assertThat(result, notNullValue())
        assertThat(result, not(sameInstance(instance)))

        result.go()
        verify(stateMachine).begin(requestCaptor.capture())

        val requestMade = requestCaptor.firstValue
        assertThat(requestMade, notNullValue())

        val expectedHeaders = Maps.mutableCopyOf(defaultHeaders)
        expectedHeaders.put(key, value)
        assertThat(requestMade.requestHeaders, equalTo(expectedHeaders))

    }

    @Test
    fun testUsingDefaultHeaderEdgeCase()
    {
        val key = one(alphabeticStrings())
        val value = one(alphabeticStrings())

        assertThrows { instance.usingDefaultHeader("", "") }
                .isInstanceOf(IllegalArgumentException::class.java)

        assertThrows { instance.usingDefaultHeader("", value) }
                .isInstanceOf(IllegalArgumentException::class.java)

        //Key alone is OK
        instance.usingDefaultHeader(key, "")
    }

    @Test
    fun testGo()
    {
        val result = instance.go()
        verify(stateMachine).begin(any())
    }

    @Test
    fun testGetDefaultHeaders()
    {
        val result = instance.defaultHeaders
        assertThat(result, equalTo(defaultHeaders))

        val javaHeaders = result as? java.util.Map<String, String> ?: return
        assertThrows { javaHeaders.clear() }
    }

    @Test
    fun testToString()
    {
        val toString = instance.toString()
        assertThat(toString, not(isEmptyOrNullString()))
    }
}
