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
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tech.sirwellington.alchemy.http.AlchemyRequestMapper.HttpDeleteWithBody;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.http.Generators.jsonElements;
import static tech.sirwellington.alchemy.http.Generators.validUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class AlchemyRequestMapperTest
{

    private AlchemyRequestMapper instance;
    private URL url;

    @Mock
    private HttpRequest request;

    private JsonElement body;

    @Before
    public void setUp()
    {
        url = one(validUrls());
        body = one(jsonElements());

        when(request.getUrl()).thenReturn(url);
        when(request.getBody()).thenReturn(body);
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

        //Edge cases
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
    public void testPost() throws Exception
    {
        instance = AlchemyRequestMapper.POST;
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(HttpPost.class));
        assertThat(result.getURI(), is(url.toURI()));

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
    public void testPut() throws Exception
    {
        instance = AlchemyRequestMapper.PUT;
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(HttpPut.class));
        assertThat(result.getURI(), is(url.toURI()));

        //Edge Conditions
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
    public void testDelete() throws Exception
    {
        instance = AlchemyRequestMapper.DELETE;
        assertThat(instance, notNullValue());

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, notNullValue());
        assertThat(result, instanceOf(HttpDelete.class));
        assertThat(result.getURI(), is(url.toURI()));

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
    public void testDeleteWithBody() throws Exception
    {
        instance = AlchemyRequestMapper.DELETE;

        when(request.hasBody()).thenReturn(true);

        HttpUriRequest result = instance.convertToApacheRequest(request);
        assertThat(result, instanceOf(HttpDeleteWithBody.class));

        HttpDeleteWithBody delete = (HttpDeleteWithBody) result;
        assertEntity(delete.getEntity());
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

}
