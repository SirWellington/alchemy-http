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

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.AlchemyTest;
import tech.sirwellington.alchemy.test.generation.GeneratePojo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;


/**
 * @author SirWellington
 */
@AlchemyTest
@IntegrationTest
final class ReqResponseAPITest {

    private static final String ENDPOINT = "https://reqres.in";
    private static final String API_KEY = System.getenv("REQ_RES_API_KEY");
    private static final AlchemyHttp http = AlchemyHttp
        .newBuilder()
        .usingDefaultHeader("X-Api-Key", API_KEY)
        .build();
    private static final Logger LOG = LoggerFactory.getLogger(ReqResponseAPITest.class);

    private record User(
        String id,
        String email,
        String firstName,
        String lastName,
        URI avatar
    ) {}

    private record GetUsersResponse(
        int page,
        int perPage,
        int totalPages,
        List<User> data
    ) {}

    private record CreateUserRequest(
        String name,
        String job
    ) {}

    private record CreateUserResponse(
        String name,
        String job,
        String id,
        String createdAt
    ) {}

    private record UpdateUserResponse(
        String name,
        String job,
        String updatedAt
    ) {}

    @GeneratePojo
    private CreateUserRequest request;

    @Test
    public void testGetUsers() throws Exception {
        var url = ENDPOINT + "/api/users";

        var response = http.go()
            .get()
            .expecting(GetUsersResponse.class)
            .at(url);

        assertThat(response.data, not(empty()));
        var firstUser = response.data.getFirst();
        assertThat(firstUser.id, not(emptyOrNullString()));
        assertThat(firstUser.email, not(emptyOrNullString()));
        assertThat(firstUser.avatar, not(nullValue()));
    }

    @Test
    public void testCreateUser() throws Exception {
        var url = ENDPOINT + "/api/users";

        var response = http.go()
                           .post()
                           .body(request)
                           .expecting(CreateUserResponse.class)
                           .at(url);

        LOG.info("POST @ [{}] produced | [{}]", url, response);

        assertThat(response, notNullValue());
        assertThat(response.name, equalTo(request.name));
        assertThat(response.job, equalTo(request.job));
        assertThat(response.id == null || response.id.isEmpty(), is(false));
        assertThat(response.createdAt == null || response.createdAt.isEmpty(), is(false));
    }

    @Test
    public void testUpdateUser() throws Exception {
        var userId = 3;
        var url = ENDPOINT + "/api/users/" + userId;

        var response = http.go()
                           .put()
                           .body(request)
                           .expecting(UpdateUserResponse.class)
                           .at(url);

        LOG.info("PUT request @ [{}] produced response [{}]", url, response);

        assertThat(response, notNullValue());
        assertThat(response.name, equalTo(request.name));
        assertThat(response.job, equalTo(request.job));
        assertThat(response.updatedAt == null || response.updatedAt.isEmpty(), is(false));
    }

    @Test
    public void testDeleteUser() throws Exception {
        var userId = one(smallPositiveIntegers());
        var url = ENDPOINT + "/api/users/" + userId;

        var response = http.go()
                           .delete()
                           .noBody()
                           .at(url);

        LOG.info("DELETE request @[{}] produced | [{}]", url, response);

        assertThat(response, notNullValue());
        assertThat(response.isOk(), is(true));
        assertThat(response.statusCode(), equalTo(204));
    }

    @Test
    public void testWithInvalidBody() throws Exception {
        var url = ENDPOINT + "/api/users";
        var body = one(alphabeticStrings());

        assertThrows(
            () -> http.go()
                      .post()
                      .body(body)
                      .at(url)
        ).isInstanceOf(AlchemyHttpException.class)
         .assertThatException(ex -> {
             var alchemyEx = (AlchemyHttpException) ex;
             assertThat(alchemyEx.getRequest(), notNullValue());
             assertThat(alchemyEx.getResponse(), notNullValue());
             LOG.info("Received response: [{}]", alchemyEx.getResponse());
         });
    }
}
