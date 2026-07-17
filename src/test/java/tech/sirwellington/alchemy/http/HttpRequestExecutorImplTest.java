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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.http.exceptions.AlchemyConnectionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@RepeatedTest(100)
public class HttpRequestExecutorImplTest
{

    @Mock
    private HttpConnectionPreparer requestMapper;

    @Mock
    private HttpRequest request;

    @Mock
    private HttpURLConnection httpConnection;

    @Mock
    private OutputStream output;

    @Mock
    private InputStream input;

    private JsonElement responseBody;
    private String responseString;

    private Map<String, String> responseHeaders;
    private final com.google.gson.Gson gson = Constants.DEFAULT_GSON;
    private long timeout;

    private HttpRequestExecutor instance;

    @Before
    public void setUp() throws IOException
    {
        instance = new HttpRequestExecutorImpl(requestMapper);
        verifyNoInteractions(requestMapper);

        timeout = NumberGenerators.smallPositiveLongs().get();
        when(requestMapper.map(request)).thenReturn(httpConnection);

        setupResponse();
    }

    private void setupResponse() throws IOException
    {
        setupResponseBody();
        setupResponseHeaders();
        when(httpConnection.getResponseCode()).thenReturn(200);
    }

    private void setupResponseBody() throws IOException
    {
        responseBody = one(Generators.jsonElements());
        responseString = responseBody.toString();

        byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
        input = new ByteArrayInputStream(bytes);

        when(httpConnection.getInputStream()).thenReturn(input);
        when(httpConnection.getOutputStream()).thenReturn(output);
        when(httpConnection.getContentType()).thenReturn(ContentTypes.APPLICATION_JSON);
    }

    private void setupResponseHeaders()
    {
        responseHeaders = CollectionGenerators.mapOf(alphabeticStrings(), hexadecimalString(10), 15);

        Map<String, List<String>> headers = Maps.create();

        for (var entry : responseHeaders.entrySet())
        {
            headers.put(entry.getKey(), Lists.createFrom(entry.getValue()));
        }

        when(httpConnection.getHeaderFields()).thenReturn(headers);
    }

    @Test
    public void testCreate()
    {
        HttpRequestExecutorImpl result = HttpRequestExecutorImpl.create(requestMapper);
        assertThat(result, notNullValue());

        assertThrows(() -> HttpRequestExecutorImpl.create(null));
    }

    @Test
    public void testExecute() throws IOException
    {
        HttpResponse response = instance.execute(request, gson, timeout);

        assertThat(response, notNullValue());
        assertThat(response.statusCode(), equalTo(httpConnection.getResponseCode()));
        assertThat(response.isOk(), equalTo(true));
        assertThat(response.body(), equalTo(responseBody));
        assertThat(response.responseHeaders(), equalTo(responseHeaders));
        assertThat(response.bodyAsString(), equalTo(responseBody.toString()));

        verify(httpConnection).setConnectTimeout((int) timeout);
    }

    // Edge Cases
    @DontRepeat
    @Test
    public void testExecuteWithBadArgs()
    {
        assertThrows(() -> instance.execute(request, gson, -1L));
    }

    @Test
    public void testExecuteWhenRequestMapperReturnsNull()
    {
        when(requestMapper.map(request)).thenReturn(null);
        assertThrows(() -> instance.execute(request, gson, timeout));
    }

    @Test
    public void testWhenRequestTimesOut() throws Exception
    {
        when(httpConnection.getInputStream())
                .thenThrow(SocketTimeoutException.class);

        assertThrows(() -> instance.execute(request, gson, timeout))
                .isInstanceOf(AlchemyConnectionException.class);
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
        byte[] binary = "".getBytes(StandardCharsets.UTF_8);
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

        JsonPrimitive expected = new JsonPrimitive(responseBody.toString());
        JsonElement result = response.body();
        assertThat(result, equalTo(expected));
    }

    @RepeatedTest(5)
    @Test
    public void testWhenConnectionFails()
    {
        java.net.URL url = Generators.validUrls().get();
        request = HttpRequest.Builder.from(request).usingUrl(url).build();

        var realConnection;
        try
        {
            realConnection = (HttpURLConnection) url.openConnection();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        when(requestMapper.map(request)).thenReturn(realConnection);

        assertThrows(() -> instance.execute(request, gson))
                .isInstanceOf(AlchemyConnectionException.class);
    }

    // =============================================
    // PERFORMANCE TESTS
    // =============================================

    @DontRepeat
    @Test
    public void testPerformance()
    {
        var parser = new JsonParser();

        System.out.println("performance test");
        var body = one(Generators.jsonObjects()).toString();

        long time = time(() -> parser.parse(body));
        System.out.println("Parser took " + time);

        time = time(() -> gson.fromJson(body, JsonElement.class));
        System.out.println("Gson took " + time);

        var iterations = 100;

        time = time(() -> {
            for (int i = 0; i < iterations; i++)
            {
                parser.parse(body);
            }
        });

        System.out.printf("Parser took %dms across %d runs%n", time, iterations);

        time = time(() -> {
            for (int i = 0; i < iterations; i++)
            {
                gson.fromJson(body, JsonElement.class);
            }
        });
        System.out.printf("Gson took %dms across %d runs%n", time, iterations);
    }

    @DontRepeat
    @Test
    public void compareGsonMethods()
    {
        responseBody = one(Generators.jsonObjects());

        var text = responseBody.toString();

        var fromJson = gson.fromJson(text, JsonElement.class);
        var toJsonTree = gson.toJsonTree(text);

        boolean equals = fromJson.equals(toJsonTree);
        System.out.println("Equal? " + equals);
    }

    private long time(Runnable task)
    {
        long start = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();
        return end - start;
    }
}
