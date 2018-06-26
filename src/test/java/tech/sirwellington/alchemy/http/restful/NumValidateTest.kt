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

package tech.sirwellington.alchemy.http.restful

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.generator.PeopleGenerators
import tech.sirwellington.alchemy.http.AlchemyHttpBuilder
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
@IntegrationTest
@Ignore
class NumValidateTest
{

    private val ENDPOINT = "https://numvalidate.com/api/validate"
    private val LOG = LoggerFactory.getLogger(this::class.java)

    private val http = AlchemyHttpBuilder.newInstance().build()

    @Ignore
    @Repeat(5)
    @Test
    fun testPhone()
    {
        val url = ENDPOINT
        val phone = PeopleGenerators.phoneNumberStrings().get()

        val response = http.go()
                           .get()
                           .usingQueryParam("number", phone)
                           .at(url)

        assertThat(response, notNull)
        assertTrue { response.body().isJsonObject }

        val json = response.body().asJsonObject

        LOG.info("Received response for phone number [$phone] | [$json]")
    }
}