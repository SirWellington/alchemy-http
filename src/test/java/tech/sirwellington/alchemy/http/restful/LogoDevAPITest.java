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

import java.util.Arrays;
import javax.swing.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;


@AlchemyTest
@IntegrationTest
public class LogoDevAPITest {

    private static final Logger LOG = LoggerFactory.getLogger(LogoDevAPITest.class);

    private static final String AUTOCOMPLETE_ENDPOINT = "https://autocomplete.clearbit.com/v1/companies/suggest";
    private static final String LOGO_ENDPOINT = "https://logos.hunter.io";

    private final AlchemyHttp http = AlchemyHttp.newBuilder().build();

    private record AutocompleteResponse(
        String name,
        String domain,
        String logo
    ) {}

    @Test
    public void testGetGoogleLogo() {
        var url = LOGO_ENDPOINT + "/google.com";

        var response = http.go().download(url);
        testDownloadedLogo(response);
    }

    @Test
    public void testGetAmazonLogo() {
        var url = LOGO_ENDPOINT + "/amazon.com";

        var response = http.go().download(url);
        testDownloadedLogo(response);
    }

    @Test
    public void testGithubLogo() {
        var url = LOGO_ENDPOINT + "/github.com";

        var response = http.go().download(url);
        testDownloadedLogo(response);
    }

    @Test
    public void testAutocomplete() throws Exception {
        testAutocompleteWithText("Am");
        testAutocompleteWithText("Cen");
        testAutocompleteWithText("Goo");
        testAutocompleteWithText("Ver");
    }

    private void testDownloadedLogo(byte[] response) {
        assertThat(response, notNullValue());
        assertThat(response.length == 0, is(false));

        var image = new ImageIcon(response);
        LOG.info("Downloaded logo: [{}, {}x{}]", image.getDescription(), image.getIconWidth(), image.getIconHeight());
    }

    private void testAutocompleteWithText(String text) throws Exception {
        var url = AUTOCOMPLETE_ENDPOINT;

        var responseArray = http.go()
                                .get()
                                .usingQueryParam("query", text)
                                .expecting(AutocompleteResponse[].class)
                                .at(url);

        assertThat(responseArray, notNullValue());
        var response = Arrays.asList(responseArray);
        assertThat(response, not(empty()));

        for (var item : response) {
            assertThat(item.name, not(emptyOrNullString()));
            assertThat(item.domain, not(emptyOrNullString()));
            assertThat(item.logo, not(emptyOrNullString()));

            try {
                checkThat(item.logo).isA(validURL());
            }
            catch (Exception _) {
            }
        }
    }

}
