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
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import javax.swing.ImageIcon;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;


@RunWith(AlchemyTestRunner.class)
@IntegrationTest
public class ClearbitAPITest
{

    private static final Logger LOG = LoggerFactory.getLogger(ClearbitAPITest.class);

    private static final String AUTOCOMPLETE_ENDPOINT = "https://autocomplete.clearbit.com/v1/companies/suggest";
    private static final String LOGO_ENDPOINT = "https://logo.clearbit.com";

    private final AlchemyHttp http = AlchemyHttp.newBuilder().build();

    private static class AutocompleteResponse
    {
        String name;
        String domain;
        String logo;

        @Override
        public String toString()
        {
            return "AutocompleteResponse{" +
                   "name='" + name + '\'' +
                   ", domain='" + domain + '\'' +
                   ", logo='" + logo + '\'' +
                   '}';
        }
    }

    @Test
    public void testGetGoogleLogo()
    {
        String url = LOGO_ENDPOINT + "/google.com";

        byte[] response = http.go().download(url);
        testDownloadedLogo(response);
    }

    @Test
    public void testGetAmazonLogo()
    {
        String url = LOGO_ENDPOINT + "/amazon.com";

        byte[] response = http.go().download(url);
        testDownloadedLogo(response);
    }

    @Test
    public void testGithubLogo()
    {
        String url = LOGO_ENDPOINT + "/github.com";

        byte[] response = http.go().download(url);
        testDownloadedLogo(response);
    }

    @Test
    public void testAutocomplete()
    {
        testAutocompleteWithText("Am");
        testAutocompleteWithText("Cen");
        testAutocompleteWithText("Goo");
        testAutocompleteWithText("Ver");
    }

    private void testDownloadedLogo(byte[] response)
    {
        assertThat(response, notNullValue());
        assertFalse(response.length == 0);

        ImageIcon image = new ImageIcon(response);
        LOG.info("Downloaded logo: [{}, {}x{}]", image.getDescription(), image.getIconWidth(), image.getIconHeight());
    }

    private void testAutocompleteWithText(String text)
    {
        String url = AUTOCOMPLETE_ENDPOINT;

        AutocompleteResponse[] responseArray = http.go()
                                                   .get()
                                                   .usingQueryParam("query", text)
                                                   .expecting(AutocompleteResponse[].class)
                                                   .at(url);

        assertThat(responseArray, notNullValue());
        List<AutocompleteResponse> response = Arrays.asList(responseArray);
        assertThat(response, not(empty()));

        for (AutocompleteResponse item : response)
        {
            assertThat(item.name, not(isEmptyOrNullString()));
            assertThat(item.domain, not(isEmptyOrNullString()));
            assertThat(item.logo, not(isEmptyOrNullString()));

            checkThat(item.logo).isA(validURL());
        }
    }

}
