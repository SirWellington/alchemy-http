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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import tech.sirwellington.alchemy.test.junit.runners.Repeat

@RunWith(AlchemyTestRunner::class)
@Repeat
class StringsTest
{

    @GenerateString
    private lateinit var string: String

    @Before
    @Throws(Exception::class)
    fun setUp()
    {

        setupData()
        setupMocks()

    }

    @Test
    @Throws(Exception::class)
    fun testNullToEmpty()
    {
        var result = Strings.nullToEmpty(string)
        assertThat(result, equalTo(string))

        result = Strings.nullToEmpty(null)
        assertThat(result, notNullValue())
        assertThat(result, equalTo(""))
    }

    @Test
    @Throws(Exception::class)
    fun testIsNullOrEmpty()
    {
        assertFalse(Strings.isNullOrEmpty(string))
        assertTrue(Strings.isNullOrEmpty(null))
        assertTrue(Strings.isNullOrEmpty(""))
    }

    @Throws(Exception::class)
    private fun setupData()
    {

    }

    @Throws(Exception::class)
    private fun setupMocks()
    {

    }
}