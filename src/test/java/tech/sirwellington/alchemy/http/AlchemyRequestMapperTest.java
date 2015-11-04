/*
 * Copyright 2015 SirWellington Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequestMapper.HttpDeleteWithBody;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.http.Generators.jsonElements;
import static tech.sirwellington.alchemy.http.Generators.validUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

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
        queryParams = mapOf(alphabeticString(), alphabeticString(), 10);
        url = one(validUrls());
        expandedUrl = expandUrl();

        when(request.getUrl()).thenReturn(url);
        when(request.getBody()).thenReturn(body);
        when(request.getQueryParams()).thenReturn(queryParams);
    }

    @Test
    public void testGet() throws Exception
    {
        instance = AlchemyRequestMapper.GET;
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result.getURI(), is(url.toURI()));
        assertThat(result, instanceOf(HttpGet.class));

    }

    @Test
    public void testGetEdgeConditions()
    {
        instance = AlchemyRequestMapper.GET;

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
        instance = AlchemyRequestMapper.GET;

        when(request.hasQueryParams())
                .thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result.getURI(), is(expandedUrl.toURI()));
    }

    @Test
    public void testPost() throws Exception
    {
        instance = AlchemyRequestMapper.POST;
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
        instance = AlchemyRequestMapper.POST;

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
        instance = AlchemyRequestMapper.POST;

        when(request.hasBody()).thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, instanceOf(HttpPost.class));

        HttpPost post = (HttpPost) result;
        assertEntity(post.getEntity());
    }

    @Test
    public void testPostExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.POST;

        when(request.hasQueryParams())
                .thenReturn(Boolean.TRUE);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result.getURI(), is(expandedUrl.toURI()));
    }

    @Test
    public void testPut() throws Exception
    {
        instance = AlchemyRequestMapper.PUT;
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
        instance = AlchemyRequestMapper.PUT;

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
        instance = AlchemyRequestMapper.PUT;

        when(request.hasBody()).thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, instanceOf(HttpPut.class));

        HttpPut put = (HttpPut) result;
        assertEntity(put.getEntity());
    }

    @Test
    public void testPutExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.PUT;

        when(request.hasQueryParams())
                .thenReturn(Boolean.TRUE);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result.getURI(), is(expandedUrl.toURI()));
    }

    @Test
    public void testDelete() throws Exception
    {
        instance = AlchemyRequestMapper.DELETE;
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
        instance = AlchemyRequestMapper.DELETE;

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
        instance = AlchemyRequestMapper.DELETE;

        when(request.hasBody()).thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, instanceOf(HttpDeleteWithBody.class));

        HttpDeleteWithBody delete = (HttpDeleteWithBody) result;
        assertEntity(delete.getEntity());
    }

    @Test
    public void testDeleteExpandsURL() throws Exception
    {
        instance = AlchemyRequestMapper.DELETE;

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
        URL result = AlchemyRequestMapper.expandUrlFromRequest(request);
        assertThat(result, is(url));

        //When there are query params
        when(request.hasQueryParams())
                .thenReturn(true);
        result = AlchemyRequestMapper.expandUrlFromRequest(request);
        assertThat(result, is(expandedUrl));

    }

    private URL expandUrl() throws URISyntaxException, MalformedURLException
    {
        URIBuilder builder = new URIBuilder(url.toURI());

        queryParams.forEach(builder::addParameter);

        return builder.build().toURL();
    }

}
