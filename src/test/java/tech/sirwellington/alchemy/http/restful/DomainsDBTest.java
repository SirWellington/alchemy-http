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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * @author SirWellington
 */
@AlchemyTest
@IntegrationTest
final class DomainsDBTest {

    private static final Logger LOG = LoggerFactory.getLogger(DomainsDBTest.class);

    private static final String ENDPOINT = "https://dns.google/resolve";
    private final AlchemyHttp http = AlchemyHttp.newBuilder().build();

    private record ResponseBody(
        int Status,
        Answer[] Answer
    ) {
        record Answer(
            String name,
            int type,
            int ttl,
            String data
        ) { }
    }

    @Test
    public void testMicrosoft() throws Exception {
        var response = http.go()
                           .get()
                           .usingQueryParam("name", "microsoft.com")
                           .usingQueryParam("type", "A")
                           .expecting(ResponseBody.class)
                           .at(ENDPOINT);

        checkResponse(response);
    }

    @Test
    public void testFacebook() throws Exception {
        var response = http.go()
                           .get()
                           .usingQueryParam("name", "facebook")
                           .usingQueryParam("tld", "com")
                           .expecting(ResponseBody.class)
                           .at(ENDPOINT);

        checkResponse(response);
    }

    @Test
    public void testAmazon() throws Exception {
        var response = http.go()
                           .get()
                           .usingQueryParam("name", "Google")
                           .usingQueryParam("told", "com")
                           .expecting(ResponseBody.class)
                           .at(ENDPOINT);

        checkResponse(response);
    }

    private void checkResponse(ResponseBody response) {
        LOG.info("Received response: [{}]", response);

        assertThat(response, notNullValue());
        assertThat(response.Status, notNullValue());
        assertThat(response.Answer, notNullValue());
        var answers = response.Answer;
        assertThat(answers, Matchers.not(emptyArray()));
        var first = answers[0];
        assertThat(first.name, not(emptyOrNullString()));
        assertThat(first.ttl, not(nullValue()));
        assertThat(first.data, not(emptyOrNullString()));
    }
}
