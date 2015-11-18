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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.generator.StringGenerators.strings;
import static tech.sirwellington.alchemy.http.Generators.jsonElements;
import static tech.sirwellington.alchemy.http.Generators.jsonObjects;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat(100)
public class HttpVerbImplTest
{

    @Mock
    private AlchemyRequestMapper requestMapper;

    @Mock
    private HttpClient apacheClient;

    @Mock
    private HttpRequest request;

    @Mock
    private HttpUriRequest apacheRequest;

    @Mock
    private org.apache.http.HttpResponse apacheResponse;

    @Mock
    private StatusLine statusLine;

    private HttpEntity entity;

    private JsonElement responseBody;

    private Map<String, String> responseHeaders;

    private HttpVerb instance;

    private final Gson gson = Constants.getDefaultGson();

    @Before
    public void setUp() throws IOException
    {
        instance = new HttpVerbImpl(requestMapper);
        verifyZeroInteractions(requestMapper);

        when(requestMapper.convertToApacheRequest(request))
                .thenReturn(apacheRequest);

        responseBody = one(jsonElements());

        entity = new StringEntity(responseBody.toString(), ContentType.APPLICATION_JSON);

        setupResponse();
    }

    public void setupResponse() throws IOException
    {
        when(apacheResponse.getEntity())
                .thenReturn(entity);

        when(apacheResponse.getStatusLine())
                .thenReturn(statusLine);

        when(statusLine.getStatusCode())
                .thenReturn(one(integers(200, 209)));

        when(apacheClient.execute(apacheRequest))
                .thenReturn(apacheResponse);

        setupResponseHeaders();
    }

    public void setupResponseHeaders()
    {
        responseHeaders = mapOf(alphabeticString(), hexadecimalString(10), 15);
        List<Header> headers = Lists.newArrayList();
        responseHeaders.forEach((k, v) -> headers.add(new BasicHeader(k, v)));

        Header[] headerArray = headers.toArray(new Header[headers.size()]);
        when(apacheResponse.getAllHeaders())
                .thenReturn(headerArray);
    }

    @Test
    public void testUsing()
    {
        HttpVerbImpl result = HttpVerbImpl.using(requestMapper);
        assertThat(result, notNullValue());

        assertThrows(() -> HttpVerbImpl.using(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecute()
    {
        HttpResponse response = instance.execute(apacheClient, request);

        assertThat(response, notNullValue());
        assertThat(response.statusCode(), is(statusLine.getStatusCode()));
        assertThat(response.isOk(), is(true));
        assertThat(response.body(), is(responseBody));
        assertThat(response.responseHeaders(), is(responseHeaders));
        assertThat(response.bodyAsString(), is(responseBody.toString()));
    }

    //Edge Cases
    @DontRepeat
    @Test
    public void testExecuteWithBadArgs()
    {
        assertThrows(() -> instance.execute(null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.execute(null, request))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.execute(apacheClient, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecuteWhenRequestMapperReturnsNull()
    {
        when(requestMapper.convertToApacheRequest(request))
                .thenReturn(null);

        assertThrows(() -> instance.execute(apacheClient, request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testExecuteWhenApacheClientThrows() throws IOException
    {
        when(apacheClient.execute(apacheRequest))
                .thenThrow(new IOException());

        assertThrows(() -> instance.execute(apacheClient, request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @DontRepeat
    @Test
    public void testExecuteWhenResponseIsNull() throws IOException
    {
        when(apacheClient.execute(apacheRequest))
                .thenReturn(null);

        assertThrows(() -> instance.execute(apacheClient, request))
                .isInstanceOf(AlchemyHttpException.class);

    }

    @DontRepeat
    @Test
    public void testExecuteWhenEntityIsNull()
    {
        when(apacheResponse.getEntity())
                .thenReturn(null);

        HttpResponse result = instance.execute(apacheClient, request);
        assertThat(result, notNullValue());
        assertThat(result.body(), is(JsonNull.INSTANCE));
    }

    @DontRepeat
    @Test
    public void testExecuteWhenReadingEntityFails() throws IOException
    {
        HttpEntity badEntity = mock(HttpEntity.class);
        when(apacheResponse.getEntity())
                .thenReturn(badEntity);

        when(badEntity.getContent())
                .thenThrow(new IOException());

        assertThrows(() -> instance.execute(apacheClient, request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @DontRepeat
    @Test
    public void testExecuteWhenBodyIsEmpty() throws Exception
    {
        entity = new StringEntity("");
        when(apacheResponse.getEntity())
                .thenReturn(entity);

        HttpResponse result = instance.execute(apacheClient, request);
        assertThat(result.body(), instanceOf(JsonNull.class));
    }

    @Test
    public void testExecuteWhenContentTypeInvalid()
    {
        List<ContentType> invalidContentTypes = Arrays.asList(ContentType.APPLICATION_ATOM_XML,
                                                              ContentType.TEXT_HTML,
                                                              ContentType.TEXT_XML,
                                                              ContentType.APPLICATION_XML,
                                                              ContentType.APPLICATION_OCTET_STREAM,
                                                              ContentType.create(one(alphabeticString())));

        ContentType invalidContentType = invalidContentTypes.stream().findAny().get();

        entity = new StringEntity(one(strings(1000)), invalidContentType);

        when(apacheResponse.getEntity())
                .thenReturn(entity);

        assertThrows(() -> instance.execute(apacheClient, request))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @DontRepeat
    @Test
    public void testExecuteWhenNoResponseHeaders() throws Exception
    {
        when(apacheResponse.getAllHeaders())
                .thenReturn(null);

        HttpResponse result = instance.execute(apacheClient, request);
        assertThat(result.responseHeaders(), notNullValue());
        assertThat(result.responseHeaders().isEmpty(), is(true));
    }

    @Test
    public void testExecuteWhenEntityIsNotJson()
    {
        String html = String.format("<%s>%s</%s>",
                                    "someTag",
                                    one(hexadecimalString(100)),
                                    "someTag");
        entity = new StringEntity(html, ContentType.APPLICATION_JSON);

        when(apacheResponse.getEntity())
                .thenReturn(entity);

        assertThrows(() -> instance.execute(apacheClient, request))
                .isInstanceOf(JsonException.class);
    }

    @Test
    public void testExecuteWhenEntityIsText()
    {
        String text = one(alphabeticString());
        entity = new StringEntity(text, ContentType.TEXT_PLAIN);
        when(apacheResponse.getEntity())
                .thenReturn(entity);

        HttpResponse result = instance.execute(apacheClient, request);
        assertThat(result, notNullValue());
        assertThat(result.bodyAsString(), is(text));
        JsonElement asJSON = result.body();
        assertThat(asJSON.isJsonPrimitive(), is(true));
        assertThat(asJSON.getAsJsonPrimitive().isString(), is(true));
    }

    @DontRepeat
    @Test
    public void testPerformance()
    {
        JsonParser parser = new JsonParser();

        System.out.println("performance test");
        String body = one(jsonObjects()).toString();

        long time = time(() -> parser.parse(body));
        System.out.println("Parser took " + time);

        time = time(() -> gson.fromJson(body, JsonElement.class));
        System.out.println("Gson took " + time);

        int iterations = 100;

        Runnable task;

        task = () ->
        {
            for (int i = 0; i < iterations; ++i)
            {
                parser.parse(body);
            }
        };

        time = time(task);
        System.out.printf("Parser took %dms accross %d runs\n", time, iterations);

        task = () ->
        {
            for (int i = 0; i < iterations; ++i)
            {
                gson.fromJson(body, JsonElement.class);
            }
        };

        time = time(task);
        System.out.printf("Gson took %dms accross %d runs\n", time, iterations);

    }

    @DontRepeat
    @Test
    public void compareGsonMethods()
    {
        responseBody = one(jsonObjects());

        String text = responseBody.toString();

        JsonElement fromJson = gson.fromJson(text, JsonElement.class);
        JsonElement toJsonTree = gson.toJsonTree(text);

        boolean equals = fromJson.equals(toJsonTree);
        System.out.println("Equal? " + equals);

    }

    private static long time(Runnable task)
    {
        long start = currentTimeMillis();
        task.run();
        long end = currentTimeMillis();
        return end - start;
    }
}
