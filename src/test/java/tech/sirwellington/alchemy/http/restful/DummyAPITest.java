/*
 * Copyright © 2026. Sir Wellington.
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

package tech.sirwellington.alchemy.http.restful;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.AlchemyTest;
import tech.sirwellington.alchemy.test.generation.GeneratePojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@AlchemyTest
@IntegrationTest
public class DummyAPITest {

    private static final String ENDPOINT = "https://jsonplaceholder.typicode.com";
    private static final Logger LOG = LoggerFactory.getLogger(DummyAPITest.class);

    private record PostRequest(
        String title,
        String body,
        int userId
    ) { }

    private record Post(
        int id,
        String title,
        String body,
        int userId
    ) {}

    @GeneratePojo
    private PostRequest request;

    private final AlchemyHttp http = AlchemyHttp.newBuilder().build();

    @Test
    public void testCreatePost() throws Exception {
        var url = ENDPOINT + "/posts";

        var response = http.go()
                           .post()
                           .body(request)
                           .expecting(Post.class)
                           .at(url);

        assertThat(response, notNullValue());
        assertThat(response.userId, equalTo(request.userId));
        assertThat(response.title, equalTo(request.title));
        assertThat(response.body, equalTo(request.body));

        LOG.info("Received response from [{}] | [{}]", url, response);
    }

    @Test
    public void testDeletePost() throws Exception {
        var postId = 1;
        var url = ENDPOINT + "/posts/" + postId;

        var response = http.go()
                           .delete()
                           .noBody()
                           .at(url);

        assertThat(response, notNullValue());
        assertThat(response.isOk(), is(true));

        LOG.info("Received response when deleting [{}] | [{}]", url, response);
    }
}
