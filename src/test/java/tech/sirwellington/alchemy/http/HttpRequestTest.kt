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
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.generator.CollectionGenerators
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat(50)
class HttpRequestTest
{

    private lateinit var testRequest: TestRequest

    private lateinit var instance: HttpRequest

    @Before
    @Throws(Exception::class)
    fun setUp()
    {
        testRequest = TestRequest()
        instance = HttpRequest.copyOf(testRequest)
    }

    @Test
    fun testGetRequestHeaders()
    {
        assertThat(instance.requestHeaders, equalTo(testRequest.requestHeaders))
    }

    @Test
    fun testGetQueryParams()
    {
        assertThat(instance.queryParams, equalTo(testRequest.queryParams))
    }

    @Test
    fun testHasQueryParams()
    {
        testRequest.queryParams = emptyMap()
        instance = HttpRequest.copyOf(testRequest)
        assertThat(instance.hasQueryParams(), equalTo(false))

        testRequest.queryParams = CollectionGenerators.mapOf(alphabeticStrings(),
                                                               alphabeticStrings(),
                                                               10)
        instance = HttpRequest.copyOf(testRequest)
        assertThat(instance.hasQueryParams(), equalTo(true))

    }

    @Test
    fun testGetUrl()
    {
        assertThat(instance.url, equalTo(testRequest.url))
    }

    @Test
    fun testGetBody()
    {
        assertThat(instance.body, equalTo(testRequest.body))
    }

    @Test
    @Throws(Exception::class)
    fun testGetRequestMethod()
    {
        assertThat(instance.method, equalTo(testRequest.method))
    }

    @Test
    fun testHasBody()
    {
        assertThat(instance.body, equalTo(testRequest.body))
    }

    @Test
    fun testEquals()
    {
        assertThat(instance.equals(testRequest), equalTo(true))
        assertThat(testRequest.equals(instance), equalTo(true))
    }

    @Test
    fun testCopyOf()
    {
        val result = HttpRequest.copyOf(instance)
        assertThat(result, notNullValue())
        assertThat(result, equalTo(instance))
        assertThat(instance, equalTo(result))
    }

    @Test
    fun testFrom()
    {
        val result = HttpRequest.Builder.from(null)
        assertThat(result, notNullValue())
    }
}
