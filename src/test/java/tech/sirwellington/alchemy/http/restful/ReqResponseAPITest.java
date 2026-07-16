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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;


/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@IntegrationTest
@Repeat(50)
public class ReqResponseAPITest
{

    private static final String ENDPOINT = "https://reqres.in";
    private static final AlchemyHttp http = AlchemyHttp.newBuilder().build();
    private static final Logger LOG = LoggerFactory.getLogger(ReqResponseAPITest.class);

    private static class CreateUserRequest
    {
        String name;
        String job;

        @Override
        public String toString()
        {
            return "CreateUserRequest{" +
                   "name='" + name + '\'' +
                   ", job='" + job + '\'' +
                   '}';
        }
    }

    private static class CreateUserResponse
    {
        String name;
        String job;
        String id;
        String createdAt;

        @Override
        public String toString()
        {
            return "CreateUserResponse{" +
                   "name='" + name + '\'' +
                   ", job='" + job + '\'' +
                   ", id='" + id + '\'' +
                   ", createdAt='" + createdAt + '\'' +
                   '}';
        }
    }

    private static class UpdateUserResponse
    {
        String name;
        String job;
        String updatedAt;

        @Override
        public String toString()
        {
            return "UpdateUserResponse{" +
                   "name='" + name + '\'' +
                   ", job='" + job + '\'' +
                   ", updatedAt='" + updatedAt + '\'' +
                   '}';
        }
    }

    @GeneratePojo
    private CreateUserRequest request;

    @Test
    public void testCreateUser()
    {
        String url = ENDPOINT + "/api/users";

        CreateUserResponse response = http.go()
                                          .post()
                                          .body(request)
                                          .expecting(CreateUserResponse.class)
                                          .at(url);

        LOG.info("POST @ [{}] produced | [{}]", url, response);

        assertThat(response, notNullValue());
        assertThat(response.name, equalTo(request.name));
        assertThat(response.job, equalTo(request.job));
        assertFalse(response.id == null || response.id.isEmpty());
        assertFalse(response.createdAt == null || response.createdAt.isEmpty());
    }

    @Test
    public void testUpdateUser()
    {
        int userId = 3;
        String url = ENDPOINT + "/api/users/" + userId;

        UpdateUserResponse response = http.go()
                                          .put()
                                          .body(request)
                                          .expecting(UpdateUserResponse.class)
                                          .at(url);

        LOG.info("PUT request @ [{}] produced response [{}]", url, response);

        assertThat(response, notNullValue());
        assertThat(response.name, equalTo(request.name));
        assertThat(response.job, equalTo(request.job));
        assertFalse(response.updatedAt == null || response.updatedAt.isEmpty());
    }

    @Test
    public void testDeleteUser()
    {
        int userId = one(smallPositiveIntegers());
        String url = ENDPOINT + "/api/users/" + userId;

        HttpResponse response = http.go()
                                    .delete()
                                    .noBody()
                                    .at(url);

        LOG.info("DELETE request @[{}] produced | [{}]", url, response);

        assertThat(response, notNullValue());
        assertTrue(response.isOk());
        assertThat(response.statusCode(), equalTo(204));
    }

    @Test
    public void testWithInvalidBody()
    {
        String url = ENDPOINT + "/api/users";
        String body = one(alphabeticStrings());

        try
        {
            HttpResponse response = http.go()
                                        .post()
                                        .body(body)
                                        .at(url);
        }
        catch (AlchemyHttpException ex)
        {
            assertThat(ex.getRequest(), notNullValue());
            assertThat(ex.getResponse(), notNullValue());
            LOG.info("Received response: [{}]", ex.getResponse());
            return;
        }
        catch (Exception ex)
        {
            throw ex;
        }

        fail("Expected exception here");
    }
}
