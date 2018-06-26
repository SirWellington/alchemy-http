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

import com.google.gson.JsonObject
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.http.AlchemyHttp
import tech.sirwellington.alchemy.test.hamcrest.notEmpty
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@IntegrationTest
@Repeat(5)
class DomainsDBTest
{

    private val LOG = LoggerFactory.getLogger(this::class.java)

    private val ENDPOINT = "https://api.domainsdb.info/search"
    private val http = AlchemyHttp.newBuilder().build()

    data class ResponseBody(val total: Int?,
                            val time: Int?,
                            val domains: List<JsonObject>?)

    @Test
    fun testCensio()
    {
        val url = ENDPOINT

        val response = http.go()
                           .get()
                           .usingQueryParam("query", "censio")
                           .usingQueryParam("tld", "love")
                           .expecting(ResponseBody::class.java)
                           .at(url)

        checkResponse(response)
    }

    @Test
    fun testFacebook()
    {
        val url = ENDPOINT

        val response = http.go()
                           .get()
                           .usingQueryParam("query", "facebook")
                           .usingQueryParam("tld", "com")
                           .expecting(ResponseBody::class.java)
                           .at(url)

        checkResponse(response)
    }

    @Test
    fun testAmazon()
    {
        val url = ENDPOINT

        val response = http.go()
                           .get()
                           .usingQueryParam("query", "Google")
                           .usingQueryParam("told", "com")
                           .expecting(ResponseBody::class.java)
                           .at(url)

        checkResponse(response)
    }

    private fun checkResponse(response: ResponseBody)
    {
        LOG.info("Received response: [$response]")

        assertThat(response, notNull)
        assertThat(response.total, notNull)
        assertThat(response.time, notNull)
        assertThat(response.domains, notNull)
        assertThat(response.domains!!, notEmpty)
    }
}
