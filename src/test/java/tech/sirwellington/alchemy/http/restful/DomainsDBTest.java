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

import java.util.List;

import com.google.gson.JsonObject;
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
public class DomainsDBTest
{

    private static final Logger LOG = LoggerFactory.getLogger(DomainsDBTest.class);

    private static final String ENDPOINT = "https://api.domainsdb.info/search";
    private final AlchemyHttp http = AlchemyHttp.newBuilder().build();

    private static class ResponseBody
    {
        Integer total;
        Integer time;
        List<JsonObject> domains;

        @Override
        public String toString()
        {
            return "ResponseBody{" +
                   "total=" + total +
                   ", time=" + time +
                   ", domains=" + domains +
                   '}';
        }
    }

    @Test
    public void testCensio() throws Exception
    {
        String url = ENDPOINT;

        ResponseBody response = http.go()
                                    .get()
                                    .usingQueryParam("query", "censio")
                                    .usingQueryParam("tld", "love")
                                    .expecting(ResponseBody.class)
                                    .at(url);

        checkResponse(response);
    }

    @Test
    public void testFacebook() throws Exception
    {
        String url = ENDPOINT;

        ResponseBody response = http.go()
                                    .get()
                                    .usingQueryParam("query", "facebook")
                                    .usingQueryParam("tld", "com")
                                    .expecting(ResponseBody.class)
                                    .at(url);

        checkResponse(response);
    }

    @Test
    public void testAmazon() throws Exception
    {
        String url = ENDPOINT;

        ResponseBody response = http.go()
                                    .get()
                                    .usingQueryParam("query", "Google")
                                    .usingQueryParam("told", "com")
                                    .expecting(ResponseBody.class)
                                    .at(url);

        checkResponse(response);
    }

    private void checkResponse(ResponseBody response)
    {
        LOG.info("Received response: [{}]", response);

        assertThat(response, notNullValue());
        assertThat(response.total, notNullValue());
        assertThat(response.time, notNullValue());
        assertThat(response.domains, notNullValue());
        assertThat(response.domains, not(empty()));
    }
}
