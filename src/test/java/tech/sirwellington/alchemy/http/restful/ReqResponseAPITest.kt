/*
 * Copyright © 2018. Sir Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.sirwellington.alchemy.http.restful

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.smallPositiveIntegers
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.one
import tech.sirwellington.alchemy.http.AlchemyHttpBuilder
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.kotlin.extensions.isEmptyOrNull
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail


/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@IntegrationTest
@Repeat(50)
class ReqResponseAPITest
{

    companion object
    {

        private const val ENDPOINT = "https://reqres.in"

        private val http = AlchemyHttpBuilder.newInstance().build()

        private val LOG = LoggerFactory.getLogger(this::class.java)
    }

    data class CreateUserRequest(val name: String, val job: String)
    data class CreateUserResponse(val name: String?,
                                  val job: String?,
                                  val id: String?,
                                  val createdAt: String?)

    data class UpdateUserResponse(val name: String?,
                                  val job: String?,
                                  val updatedAt: String?)

    @GeneratePojo
    private lateinit var request: CreateUserRequest

    @Test
    fun testCreateUser()
    {
        val url = "$ENDPOINT/api/users"

        val response = http.go()
                .post()
                .body(request)
                .expecting(CreateUserResponse::class.java)
                .at(url)

        LOG.info("POST @ [$url] produced | [$response]")

        assertThat(response, notNull)
        assertThat(response.name, equalTo(request.name))
        assertThat(response.job, equalTo(request.job))
        assertFalse { response.id.isEmptyOrNull }
        assertFalse { response.createdAt.isEmptyOrNull }
    }

    @Test
    fun testUpdateUser()
    {
        val userId = 3
        val url = "$ENDPOINT/api/users/$userId"

        val response = http.go()
                .put()
                .body(request)
                .expecting(UpdateUserResponse::class.java)
                .at(url)

        LOG.info("PUT request @ [$url] produced response [$response]")

        assertThat(response, notNull)
        assertThat(response.name, equalTo(request.name))
        assertThat(response.job, equalTo(request.job))
        assertFalse { response.updatedAt.isEmptyOrNull }
    }

    @Test
    fun testDeleteUser()
    {
        val userId = one(smallPositiveIntegers())
        val url = "$ENDPOINT/api/users/$userId"

        val response = http.go()
                .delete()
                .nothing()
                .at(url)

        LOG.info("DELETE request @[$url] produced | [$response]")

        assertThat(response, notNull)
        assertTrue { response.isOk }
        assertThat(response.statusCode(), equalTo(204))
    }

    @Test
    fun testWithInvalidBody()
    {

        val url = "$ENDPOINT/api/users"
        val body = one(alphabeticStrings())

        try
        {
            val response = http.go()
                    .post()
                    .body(body)
                    .at(url)
        }
        catch (ex: AlchemyHttpException)
        {
            LOG.info("Received response: [${ex.response}]")
            return
        }
        catch (ex: Exception)
        {
            throw ex
        }

        fail("Expected exception here")

    }
}