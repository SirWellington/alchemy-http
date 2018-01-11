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

import java.io.IOException;
import java.net.*;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequestMapper.HttpDeleteWithBody;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.http.Generators.jsonElements;
import static tech.sirwellington.alchemy.http.Generators.validUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AlchemyRequestMapperTest
{

    private AlchemyRequestMapper instance;
    private URL url;
    private URL expandedUrl;

    @Mock
    private HttpRequest request;

    private JsonElement body;

    private Map<String, String> queryParams;

    @Before
    public void setUp() throws Exception
    {
        body = one(jsonElements());
        queryParams = mapOf(alphabeticStrings(), alphabeticStrings(), 10);
        url = one(validUrls());
        expandedUrl = expandUrl();

        when(request.getUrl()).thenReturn(url);
        when(request.getBody()).thenReturn(body);
        when(request.getQueryParams()).thenReturn(queryParams);
    }

    @Test
    public void testGet() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getGET();
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result.getURI(), is(url.toURI()));
        assertThat(result, instanceOf(HttpGet.class));

    }

    @Test
    public void testGetEdgeConditions()
    {
        instance = AlchemyRequestMapper.Companion.getGET();

        //Edge conditions
        assertThrows(() -> instance.convertToApacheRequest(null))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl()).thenReturn(null);
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl())
                .thenReturn(url)
                .thenThrow(new RuntimeException());
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testGetExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getGET();

        when(request.hasQueryParams())
                .thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result.getURI(), is(expandedUrl.toURI()));
    }

    @Test
    public void testPost() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getPOST();
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(HttpPost.class));
        assertThat(result.getURI(), is(url.toURI()));

    }

    @DontRepeat
    @Test
    public void testPostEdgeConditions()
    {
        instance = AlchemyRequestMapper.Companion.getPOST();

        //Edge conditions
        assertThrows(() -> instance.convertToApacheRequest(null))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl()).thenReturn(null);
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl())
                .thenReturn(url)
                .thenThrow(new RuntimeException());
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testPostWithBody() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getPOST();

        when(request.hasBody()).thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, instanceOf(HttpPost.class));

        HttpPost post = (HttpPost) result;
        assertEntity(post.getEntity());
    }

    @Test
    public void testPostExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getPOST();

        when(request.hasQueryParams())
                .thenReturn(Boolean.TRUE);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result.getURI(), is(expandedUrl.toURI()));
    }

    @Test
    public void testPut() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getPUT();
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(HttpPut.class));
        assertThat(result.getURI(), is(url.toURI()));

    }

    @DontRepeat
    @Test
    public void testPutEdgeConditions()
    {
        //Edge Conditions
        instance = AlchemyRequestMapper.Companion.getPUT();

        assertThrows(() -> instance.convertToApacheRequest(null))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl()).thenReturn(null);
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl())
                .thenReturn(url)
                .thenThrow(new RuntimeException());
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testPutWithBody() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getPUT();

        when(request.hasBody()).thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, instanceOf(HttpPut.class));

        HttpPut put = (HttpPut) result;
        assertEntity(put.getEntity());
    }

    @Test
    public void testPutExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getPUT();

        when(request.hasQueryParams())
                .thenReturn(Boolean.TRUE);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result.getURI(), is(expandedUrl.toURI()));
    }

    @Test
    public void testDelete() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getDELETE();
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(HttpDelete.class));
        assertThat(result.getURI(), is(url.toURI()));

    }

    @DontRepeat
    @Test
    public void testDeleteEdgeConditions()
    {
        //Edge conditions
        instance = AlchemyRequestMapper.Companion.getDELETE();

        assertThrows(() -> instance.convertToApacheRequest(null))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl()).thenReturn(null);
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(IllegalArgumentException.class);

        when(request.getUrl())
                .thenReturn(url)
                .thenThrow(new RuntimeException());
        assertThrows(() -> instance.convertToApacheRequest(request))
                .isInstanceOf(AlchemyHttpException.class);

    }

    @Test
    public void testDeleteWithBody() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getDELETE();

        when(request.hasBody()).thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, instanceOf(HttpDeleteWithBody.class));

        HttpDeleteWithBody delete = (HttpDeleteWithBody) result;
        assertEntity(delete.getEntity());
    }

    @Test
    public void testDeleteExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.Companion.getDELETE();

        when(request.hasQueryParams())
                .thenReturn(Boolean.TRUE);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result.getURI(), is(expandedUrl.toURI()));
    }

    private void assertEntity(HttpEntity entity) throws IOException
    {
        assertThat(entity, notNullValue());
        assertThat(entity, instanceOf(StringEntity.class));

        byte[] content = ByteStreams.toByteArray(entity.getContent());
        assertThat(content, notNullValue());
        String stringBody = new String(content, UTF_8);

        Gson gson = Constants.getDefaultGson();
        JsonElement jsonBody = gson.fromJson(stringBody, JsonElement.class);
        assertThat(jsonBody, is(body));
    }

    public void testDeleteWithBodyClass() throws Exception
    {
        AlchemyRequestMapper.HttpDeleteWithBody delete;
        delete = new AlchemyRequestMapper.HttpDeleteWithBody(url.toURI());

        assertThat(delete.getMethod(), is(new HttpDelete().getMethod()));

        HttpEntity entity = mock(HttpEntity.class);
        delete.setEntity(entity);

        HttpEntity result = delete.getEntity();
        assertThat(result, is(entity));
    }

    @Test
    public void testExpandUrlFromRequest() throws Exception
    {
        //When no queryparams
        URL result = AlchemyRequestMapper.Companion.expandUrlFromRequest(request);
        assertThat(result, is(url));

        //When there are query params
        when(request.hasQueryParams())
                .thenReturn(true);
        result = AlchemyRequestMapper.Companion.expandUrlFromRequest(request);
        assertThat(result, is(expandedUrl));

    }

    private URL expandUrl() throws URISyntaxException, MalformedURLException
    {
        URIBuilder builder = new URIBuilder(url.toURI());

        queryParams.forEach(builder::addParameter);

        return builder.build().toURL();
    }

}
