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
package tech.sirwellington.alchemy.http;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import com.google.gson.JsonElement;
import io.mikael.urlbuilder.UrlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 * @author SirWellington
 */
@AlchemyTest
public class HttpConnectionPreparerTest
{

    private HttpConnectionPreparer instance;

    private URL url;

    private URL expandedUrl;

    private RequestMethod requestMethod;

    @Mock
    private HttpRequest request;

    private JsonElement body;

    private Map<String, String> queryParams;

    @BeforeEach
    public void setUp() throws Exception
    {
        body = one(Generators.jsonElements());
        queryParams = CollectionGenerators.mapOf(alphabeticStrings(10),
                                                  alphabeticStrings(10),
                                                  10);
        url = one(Generators.validUrls());
        expandedUrl = expandUrl();

        when(request.url()).thenReturn(url);
        when(request.body()).thenReturn(body);
        when(request.queryParams()).thenReturn(queryParams);
        when(request.method()).thenReturn(requestMethod);

        instance = HttpConnectionPreparer.create();
    }

    @Test
    public void testMap() throws Exception
    {
        when(request.hasBody()).thenReturn(true);

        java.net.HttpURLConnection result = instance.map(request);
        assertThat(result, notNullValue());
        assertThat(result.getRequestMethod(), equalTo(requestMethod.asString));
        assertThat(result.getDoInput(), equalTo(true));
        assertThat(result.getDoOutput(), equalTo(true));

        for (var entry : result.getRequestProperties().entrySet())
        {
            var key = entry.getKey();
            var value = String.join(", ", entry.getValue());

            assertThat(queryParams.containsKey(key), equalTo(true));
            assertThat(queryParams.get(key), equalTo(value));
        }
    }

    @Test
    public void testMapExpandsURL() throws Exception
    {
        instance = HttpConnectionPreparer.create();

        when(request.hasQueryParams()).thenReturn(true);

        java.net.HttpURLConnection result = instance.map(request);
        assertThat(result.getURL(), equalTo(expandedUrl));
    }

    @Test
    public void testExpandUrlFromRequestWhenNoQueryParams() throws Exception
    {
        when(request.hasQueryParams()).thenReturn(false);

        URL result = HttpConnectionPreparer.expandUrlFromRequest(request);
        assertThat(result, equalTo(url));
    }

    @Test
    public void testExpandUrlFromRequestWhenQueryParamsPresent() throws Exception
    {
        // When there are query params
        when(request.hasQueryParams()).thenReturn(true);

        URL result = HttpConnectionPreparer.expandUrlFromRequest(request);
        assertThat(result, equalTo(expandedUrl));
    }

    private URL expandUrl() throws URISyntaxException, MalformedURLException
    {
        UrlBuilder builder = UrlBuilder.fromUrl(url);

        for (var entry : queryParams.entrySet())
        {
            builder = builder.addParameter(entry.getKey(), entry.getValue());
        }

        return builder.toUrl();
    }
}
