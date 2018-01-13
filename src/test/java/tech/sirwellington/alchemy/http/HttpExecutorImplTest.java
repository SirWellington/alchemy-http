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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import com.google.gson.*;
import kotlin.text.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.http.Generators.jsonElements;
import static tech.sirwellington.alchemy.http.Generators.jsonObjects;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat(100)
public class HttpExecutorImplTest
{

    @Mock
    private AlchemyRequestMapper requestMapper;

    @Mock
    private HttpRequest request;

    @Mock
    private HttpURLConnection httpConnection;

    private JsonElement responseBody;
    private String responseString;
    private Map<String, String> responseHeaders;
    private Gson gson = Constants.INSTANCE.DEFAULT_GSON;
    private long timeout;

    private HttpExecutor instance;


    @Before
    public void setUp() throws IOException
    {
        instance = new HttpExecutorImpl(requestMapper);

        verifyZeroInteractions(requestMapper);

        timeout = NumberGenerators.smallPositiveLongs().get();
        when(requestMapper.map(request)).thenReturn(httpConnection);

        setupResponse();
    }

    public void setupResponse() throws IOException
    {
        setupResponseBody();
        setupResponseHeaders();
        when(httpConnection.getResponseCode()).thenReturn(200);

    }

    private void setupResponseBody() throws IOException
    {
        responseBody = one(jsonElements());
        responseString = responseBody.toString();

        byte[] bytes = responseString.getBytes(Charsets.UTF_8);
        InputStream istream = new ByteArrayInputStream(bytes);

        when(httpConnection.getInputStream()).thenReturn(istream);
        when(httpConnection.getContentType()).thenReturn(ContentTypes.APPLICATION_JSON);
    }

    public void setupResponseHeaders()
    {
        responseHeaders = mapOf(alphabeticStrings(), hexadecimalString(10), 15);

        Map<String, List<String>> headers = Maps.create();

        for (Map.Entry<String, String> header : responseHeaders.entrySet())
        {
            headers.put(header.getKey(), Lists.createFrom(header.getValue()));
        }

        when(httpConnection.getHeaderFields()).thenReturn(headers);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new HttpExecutorImpl(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCreate()
    {
        HttpExecutorImpl result = HttpExecutorImpl.Companion.create(requestMapper);
        assertThat(result, notNullValue());

        assertThrows(() -> HttpExecutorImpl.Companion.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecute() throws IOException
    {
        HttpResponse response = instance.execute(request, gson, timeout);

        assertThat(response, notNullValue());
        assertThat(response.statusCode(), is(httpConnection.getResponseCode()));
        assertThat(response.isOk(), is(true));
        assertThat(response.body(), is(responseBody));
        assertThat(response.responseHeaders(), is(responseHeaders));
        assertThat(response.bodyAsString(), is(responseBody.toString()));

        verify(httpConnection).setConnectTimeout((int) timeout);
    }

    //Edge Cases
    @DontRepeat
    @Test
    public void testExecuteWithBadArgs()
    {
        assertThrows(() -> instance.execute(null, gson, 1L)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.execute(request, null, 1L)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.execute(request, gson, -1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExecuteWhenRequestMapperReturnsNull()
    {
        when(requestMapper.map(request)).thenReturn(null);
        assertThrows(() -> instance.execute(Mockito.any(), Mockito.any(), Mockito.anyLong()));
    }

    @Test
    public void testWhenRequestTimesOut() throws Exception
    {
        when(httpConnection.getInputStream())
                .thenThrow(SocketTimeoutException.class);

        assertThrows(() -> instance.execute(request, gson, timeout))
                .isInstanceOf(AlchemyHttpException.class);
    }

    @Test
    public void testWhenResponseBodyIsNull() throws Exception
    {
        when(httpConnection.getInputStream())
                .thenReturn(null);

        HttpResponse response = instance.execute(request, gson, timeout);
        assertThat(response, notNullValue());
        assertThat(response.body(), equalTo(JsonNull.INSTANCE));
    }

    @Test
    public void testWhenResponseBodyIsEmpty() throws Exception
    {
        byte[] binary = "".getBytes(Charsets.UTF_8);
        InputStream istream = new ByteArrayInputStream(binary);
        when(httpConnection.getInputStream()).thenReturn(istream);

        HttpResponse response = instance.execute(request, gson, timeout);
        assertThat(response, notNullValue());
        assertThat(response.body(), equalTo(JsonNull.INSTANCE));
    }

    @Test
    public void testWhenResponseContentTypeIsNotJson() throws Exception
    {
        when(httpConnection.getContentType()).thenReturn(ContentTypes.PLAIN_TEXT);

        HttpResponse response = instance.execute(request, gson, timeout);
        assertThat(response, notNullValue());
        assertTrue(response.isOk());
        assertThat(response.body(), equalTo(responseBody));
    }

    //=============================================
    // PERFORMANCE TESTS
    //=============================================

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
        System.out.printf("Parser took %dms across %d runs\n", time, iterations);

        task = () ->
        {
            for (int i = 0; i < iterations; ++i)
            {
                gson.fromJson(body, JsonElement.class);
            }
        };

        time = time(task);
        System.out.printf("Gson took %dms across %d runs\n", time, iterations);
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
