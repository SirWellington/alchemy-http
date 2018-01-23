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

package tech.sirwellington.alchemy.http.restful;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@IntegrationTest
public class WordnikAPITest
{

    private static final Logger LOG = LoggerFactory.getLogger(WordnikAPITest.class);

    private static final String ENDPOINT = "http://api.wordnik.com:80/v4";

    private AlchemyHttp http = AlchemyHttp.Factory.newBuilder()
                                                  .usingDefaultHeader("api_key", "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5")
                                                  .build();

    @Test
    public void testWordOfDay() throws Exception
    {
        String url = ENDPOINT + "/words.json/wordOfTheDay";

        HttpResponse response = http.go()
                                    .get()
                                    .at(url);

        assertThat(response, notNullValue());
        assertTrue(response.body() != null);
        assertTrue(response.body().isJsonObject());

        JsonObject json = response.body().getAsJsonObject();
        assertTrue(json.has("id"));
        assertTrue(json.has("word"));
        assertTrue(json.has("definitions"));
        assertTrue(json.has("examples"));
    }

    @Repeat(5)
    @Test
    public void testRandomWord() throws Exception
    {
        String url = ENDPOINT + "/words.json/randomWord";

        HttpResponse response = http.go()
                                    .get()
                                    .usingQueryParam("hasDictionaryDef", true)
                                    .at(url);

        assertThat(response, notNullValue());
        assertThat(response.body(), notNullValue());

        JsonObject json = response.body().getAsJsonObject();

        assertTrue(json.has("id"));
        assertTrue(json.has("word"));

        LOG.info("Random word is [{}]", json.get("word").getAsString());
    }

    @Test
    public void testGetTokenStatus() throws Exception
    {
        String url = ENDPOINT + "/account.json/apiTokenStatus";

        HttpResponse response = http.go()
                                    .get()
                                    .at(url);

        JsonObject json = response.body().getAsJsonObject();

        LOG.info("Token status: [{}]", json);
    }

    @Test(expected = AlchemyHttpException.class)
    public void testWhenNotFound() throws Exception
    {
        String url = ENDPOINT + "/unknown";

        http.go().get().at(url);
    }

}
