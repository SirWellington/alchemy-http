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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.kotlin.extensions.random
import tech.sirwellington.alchemy.test.hamcrest.isNull
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertFails
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
@Repeat
class HttpStatusCodeTest
{
    @GenerateEnum
    private lateinit var status: HttpStatusCode

    @Test
    fun testMatchesCode()
    {
        val code = status.code
        assertTrue { status.matchesCode(code) }

        val other = HttpStatusCode.anyExcept(status)
        assertFails { other.matchesCode(code) }
    }

    @Test
    fun testForCode()
    {
        val code = status.code
        val result = HttpStatusCode.forCode(code)
        assertThat(result, equalTo(status))
    }

    @Test
    fun testForCodeWhenUnknown()
    {
        val code = Int.random(-100, 100)
        val result = HttpStatusCode.forCode(code)
        assertThat(result, isNull)
    }

    @Test
    fun testAnyExcept()
    {
        val result = HttpStatusCode.anyExcept(status)
        assertThat(result, notNull)
        assertThat(result, !equalTo(status))
    }

}