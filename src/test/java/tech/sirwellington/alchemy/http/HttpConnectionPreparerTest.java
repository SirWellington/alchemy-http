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
import tech.sirwellington.alchemy.test.AlchemyTest;
import tech.sirwellington.alchemy.test.generation.GenerateEnum;
import tech.sirwellington.alchemy.test.generation.GenerateMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;

/**
 * @author SirWellington
 */
@AlchemyTest
public class HttpConnectionPreparerTest {

    private final HttpConnectionPreparer instance = HttpConnectionPreparer.create();

    @GenerateEnum
    private RequestMethod requestMethod;
    private URL url;
    private URL expandedUrl;

    @Mock
    private HttpRequest request;

    private JsonElement body;

    @GenerateMap(keyType = String.class, valueType = String.class)
    private Map<String, String> queryParams;

    @BeforeEach
    public void setUp() throws Exception {
        body = one(Generators.jsonElements());
        url = one(Generators.validUrls());
        expandedUrl = expandUrl();

        lenient().when(request.url()).thenReturn(url);
        lenient().when(request.body()).thenReturn(body);
        lenient().when(request.queryParams()).thenReturn(queryParams);
        lenient().when(request.method()).thenReturn(requestMethod);
    }

    @Test
    public void testMap() throws Exception {
        // Given
        when(request.hasBody()).thenReturn(true);
        var result = instance.map(request);
        // Then
        assertThat(result, notNullValue());
        assertThat(result.getRequestMethod(), equalTo(requestMethod.asString));
        assertThat(result.getDoInput(), equalTo(true));
        assertThat(result.getDoOutput(), equalTo(true));

        for (var entry : result.getRequestProperties().entrySet()) {
            var key = entry.getKey();
            var value = String.join(", ", entry.getValue());

            assertThat(queryParams.containsKey(key), equalTo(true));
            assertThat(queryParams.get(key), equalTo(value));
        }
    }

    @Test
    public void testMapExpandsURL() throws Exception {
        // Given
        when(request.hasQueryParams()).thenReturn(true);
        // When
        var result = instance.map(request);
        // Then
        assertThat(result.getURL(), equalTo(expandedUrl));
    }

    @Test
    public void testExpandUrlFromRequestWhenNoQueryParams() throws Exception {
        when(request.hasQueryParams()).thenReturn(false);

        var result = HttpConnectionPreparer.expandUrlFromRequest(request);
        assertThat(result, equalTo(url));
    }

    @Test
    public void testExpandUrlFromRequestWhenQueryParamsPresent() throws Exception {
        // When there are query params
        when(request.hasQueryParams()).thenReturn(true);

        var result = HttpConnectionPreparer.expandUrlFromRequest(request);
        assertThat(result, equalTo(expandedUrl));
    }

    private URL expandUrl() throws URISyntaxException, MalformedURLException {
        var builder = UrlBuilder.fromUrl(url);

        for (var entry : queryParams.entrySet()) {
            builder = builder.addParameter(entry.getKey(), entry.getValue());
        }

        return builder.toUrl();
    }
}
