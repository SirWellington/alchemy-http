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

package tech.sirwellington.alchemy.http.restful

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.http.AlchemyHttpBuilder
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertTrue


@RunWith(AlchemyTestRunner::class)
@IntegrationTest
@Repeat(35)
class DummyAPITest
{
    private val ENDPOINT = "https://jsonplaceholder.typicode.com"
    private val LOG = LoggerFactory.getLogger(this::class.java)

    data class PostRequest(val title: String,
                           val body: String,
                           val userId: Int)

    data class Post(val id: Int,
                    val title: String,
                    val body: String,
                    val userId: Int)

    @GeneratePojo
    private lateinit var request: PostRequest

    private val http = AlchemyHttpBuilder.newInstance().build()

    @Test
    fun testCreatePost()
    {
        val url = "$ENDPOINT/posts"

        val response = http.go()
                .post()
                .body(request)
                .expecting(Post::class.java)
                .at(url)

        assertThat(response, notNull)
        assertThat(response.userId, equalTo(request.userId))
        assertThat(response.title, equalTo(request.title))
        assertThat(response.body, equalTo(request.body))

        LOG.info("Received response from [$url] | [$response]")
    }

    @Test
    fun testDeletePost()
    {
        val postId = 1
        val url = "$ENDPOINT/posts/$postId"

        val response = http.go()
                           .delete()
                           .noBody()
                           .at(url)

        assertThat(response, notNull)
        assertTrue { response.isOk }

        LOG.info("Received response when deleting [$url] | [$response]")
    }
}
