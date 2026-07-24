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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;


@AlchemyTest
@IntegrationTest
@Disabled
public class NumValidateTest {

    private static final String ENDPOINT = "https://numvalidate.com/api/validate";
    private static final Logger LOG = LoggerFactory.getLogger(NumValidateTest.class);

    private final AlchemyHttp http = AlchemyHttp.newBuilder().build();

    @Disabled
    @Test
    public void testPhone() throws Exception {
        var url = ENDPOINT;
        var phone = one(alphabeticStrings(10));

        var response = http.go()
                           .get()
                           .usingQueryParam("number", phone)
                           .at(url);

        assertThat(response, notNullValue());
        assertThat(response.body().isJsonObject(), is(true));

        var json = response.body().getAsJsonObject();

        LOG.info("Received response for phone number [{}] | [{}]", phone, json);
    }
}
