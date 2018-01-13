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
package tech.sirwellington.alchemy.http;

import java.net.*;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import io.mikael.urlbuilder.UrlBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.http.Generators.jsonElements;
import static tech.sirwellington.alchemy.http.Generators.validUrls;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class AlchemyRequestMapperTest
{

    private AlchemyRequestMapper instance;

    @GenerateURL
    private URL url;

    @GenerateURL
    private URL expandedUrl;

    @GenerateEnum
    private RequestMethod requestMethod;

    @Mock
    private HttpRequest request;

    private JsonElement body;

    private Map<String, String> queryParams;

    @Before
    public void setUp() throws Exception
    {
        body = one(jsonElements());
        queryParams = mapOf(alphabeticStrings(10), alphabeticStrings(10), 10);
        url = one(validUrls());
        expandedUrl = expandUrl();

        when(request.getUrl()).thenReturn(url);
        when(request.getBody()).thenReturn(body);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getMethod()).thenReturn(requestMethod);

        instance = AlchemyRequestMapper.Companion.create();
    }


    @Test
    public void testMap() throws Exception
    {

        when(request.hasBody()).thenReturn(true);

        HttpURLConnection result = instance.map(request);
        assertThat(result, notNullValue());
        assertThat(result.getRequestMethod(), equalTo(requestMethod.getAsString()));
        assertThat(result.getDoInput(), is(true));
        assertThat(result.getDoOutput(), is(true));

        for (Map.Entry<String, List<String>> param : result.getRequestProperties().entrySet())
        {
            String key = param.getKey();
            String value = String.join(", ", param.getValue());

            assertThat(queryParams.containsKey(key), is(true));
            assertThat(queryParams.get(key), is(value));
        }
    }

    @Test
    public void testMapExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.create();

        when(request.hasQueryParams())
                .thenReturn(Boolean.TRUE);

        HttpURLConnection result = instance.map(request);
        assertThat(result.getURL(), equalTo(expandedUrl));
    }

    @DontRepeat
    @Test
    public void testExpandUrlFromRequestWhenNoQueryParams() throws Exception
    {
        when(request.hasQueryParams()).thenReturn(false);

        URL result = AlchemyRequestMapper.Companion.expandUrlFromRequest(request);
        assertThat(result, is(url));
    }

    @Test
    public void testExpandUrlFromRequestWhenQueryParamsPresent() throws Exception
    {
        //When there are query params
        when(request.hasQueryParams()).thenReturn(true);

        URL result = AlchemyRequestMapper.Companion.expandUrlFromRequest(request);
        assertThat(result, is(expandedUrl));
    }

    private URL expandUrl() throws URISyntaxException, MalformedURLException
    {
        UrlBuilder builder = UrlBuilder.fromUrl(url);

        for(Map.Entry<String, String> param : queryParams.entrySet())
        {
            builder = builder.addParameter(param.getKey(), param.getValue());
        }

        return builder.toUrl();
    }

}
