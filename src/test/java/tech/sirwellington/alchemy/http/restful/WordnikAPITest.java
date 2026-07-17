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

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
@IntegrationTest
@Disabled
public class WordnikAPITest
{

    private static final Logger LOG = LoggerFactory.getLogger(WordnikAPITest.class);

    private static final String ENDPOINT = "https://api.wordnik.com/v4";
    private static final String API_KEY = "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
    private static final String api_key = "api_key";

    private AlchemyHttp http = AlchemyHttp.newBuilder()
                                          .usingDefaultHeader(api_key, API_KEY)
                                          .build();

    @Disabled
    @Test
    public void testWordOfDay() throws Exception
    {
        String url = ENDPOINT + "/words.json/wordOfTheDay";

        HttpResponse response = http.go()
                                    .get()
                                    .at(url);

        assertThat(response, notNullValue());
        assertThat(response.body() != null, is(true));
        assertThat(response.body().isJsonObject(), is(true));

        JsonObject json = response.body().getAsJsonObject();
        assertThat(json.has("id"), is(true));
        assertThat(json.has("word"), is(true));
        assertThat(json.has("definitions"), is(true));
        assertThat(json.has("examples"), is(true));
    }

    @Disabled
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

        assertThat(json.has("id"), is(true));
        assertThat(json.has("word"), is(true));

        LOG.info("Random word is [{}]", json.get("word").getAsString());
    }

    @Disabled
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

    @Test
    public void testWhenNotFound() throws Exception
    {
        String url = ENDPOINT + "/unknown";

        assertThrows(() -> http.go().get().at(url))
                .isInstanceOf(AlchemyHttpException.class);
    }

}
