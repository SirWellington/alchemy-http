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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.http.exceptions.AlchemyConnectionException;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@AlchemyTest
public class HttpRequestExecutorImplTest {

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

    @BeforeEach
    public void setUp() throws IOException {
        instance = new HttpRequestExecutorImpl(requestMapper);
        verifyNoInteractions(requestMapper);

        timeout = NumberGenerators.smallPositiveLongs().get();
        when(requestMapper.map(request)).thenReturn(httpConnection);

        setupResponse();
    }

    private void setupResponse() throws IOException {
        setupResponseBody();
        setupResponseHeaders();
        when(httpConnection.getResponseCode()).thenReturn(200);
    }

    private void setupResponseBody() throws IOException {
        responseBody = one(Generators.jsonElements());
        responseString = responseBody.toString();

        var bytes = responseString.getBytes(StandardCharsets.UTF_8);
        input = new ByteArrayInputStream(bytes);

        when(httpConnection.getInputStream()).thenReturn(input);
        when(httpConnection.getOutputStream()).thenReturn(output);
        when(httpConnection.getContentType()).thenReturn(ContentTypes.APPLICATION_JSON);
    }

    private void setupResponseHeaders() {
        responseHeaders = CollectionGenerators.mapOf(alphabeticStrings(), hexadecimalString(10), 15);

        Map<String, List<String>> headers = Maps.create();

        for (var entry : responseHeaders.entrySet()) {
            headers.put(entry.getKey(), Lists.createFrom(entry.getValue()));
        }

        when(httpConnection.getHeaderFields()).thenReturn(headers);
    }

    @Test
    public void testCreate() {
        var result = HttpRequestExecutorImpl.create(requestMapper);
        assertThat(result, notNullValue());

        assertThrows(() -> HttpRequestExecutorImpl.create(null));
    }

    @Test
    public void testExecute() throws IOException {
        var response = instance.execute(request, gson, timeout);

        assertThat(response, notNullValue());
        assertThat(response.statusCode(), equalTo(httpConnection.getResponseCode()));
        assertThat(response.isOk(), equalTo(true));
        assertThat(response.body(), equalTo(responseBody));
        assertThat(response.responseHeaders(), equalTo(responseHeaders));
        assertThat(response.bodyAsString(), equalTo(responseBody.toString()));

        verify(httpConnection).setConnectTimeout((int) timeout);
    }

    // Edge Cases
    @Test
    public void testExecuteWithBadArgs() {
        assertThrows(() -> instance.execute(request, gson, -1L));
    }

    @Test
    public void testExecuteWhenRequestMapperReturnsNull() {
        when(requestMapper.map(request)).thenReturn(null);
        assertThrows(() -> instance.execute(request, gson, timeout));
    }

    @Test
    public void testWhenRequestTimesOut() throws Exception {
        when(httpConnection.getInputStream())
            .thenThrow(SocketTimeoutException.class);

        assertThrows(() -> instance.execute(request, gson, timeout))
            .isInstanceOf(AlchemyConnectionException.class);
    }

    @Test
    public void testWhenResponseBodyIsNull() throws Exception {
        when(httpConnection.getInputStream())
            .thenReturn(null);

        var response = instance.execute(request, gson, timeout);
        assertThat(response, notNullValue());
        assertThat(response.body(), equalTo(JsonNull.INSTANCE));
    }

    @Test
    public void testWhenResponseBodyIsEmpty() throws Exception {
        var binary = "".getBytes(StandardCharsets.UTF_8);
        var istream = new ByteArrayInputStream(binary);
        when(httpConnection.getInputStream()).thenReturn(istream);

        var response = instance.execute(request, gson, timeout);
        assertThat(response, notNullValue());
        assertThat(response.body(), equalTo(JsonNull.INSTANCE));
    }

    @Test
    public void testWhenResponseContentTypeIsNotJson() throws Exception {
        when(httpConnection.getContentType()).thenReturn(ContentTypes.PLAIN_TEXT);

        var response = instance.execute(request, gson, timeout);
        assertThat(response, notNullValue());
        assertThat(response.isOk(), is(true));

        var expected = new JsonPrimitive(responseBody.toString());
        var result = response.body();
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testWhenConnectionFails() {
        var url = Generators.validUrls().get();
        request = HttpRequest.Builder.from(request).usingUrl(url).build();

        HttpURLConnection realConnection;
        try {
            realConnection = (HttpURLConnection) url.openConnection();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        when(requestMapper.map(request)).thenReturn(realConnection);

        assertThrows(() -> instance.execute(request, gson, timeout))
            .isInstanceOf(AlchemyConnectionException.class);
    }

    // =============================================
    // PERFORMANCE TESTS
    // =============================================

    @Test
    public void testPerformance() {
        System.out.println("performance test");
        var body = one(Generators.jsonObjects()).toString();

        long time = time(() -> JsonParser.parseString(body));
        System.out.println("Parser took " + time);

        time = time(() -> gson.fromJson(body, JsonElement.class));
        System.out.println("Gson took " + time);

        var iterations = 100;

        time = time(() -> {
            for (int i = 0; i < iterations; i++) {
                var _ = JsonParser.parseString(body);
            }
        });

        System.out.printf("Parser took %dms across %d runs%n", time, iterations);

        time = time(() -> {
            for (int i = 0; i < iterations; i++) {
                gson.fromJson(body, JsonElement.class);
            }
        });
        System.out.printf("Gson took %dms across %d runs%n", time, iterations);
    }

    @Test
    public void compareGsonMethods() {
        responseBody = one(Generators.jsonObjects());

        var text = responseBody.toString();

        var fromJson = gson.fromJson(text, JsonElement.class);
        var toJsonTree = gson.toJsonTree(text);

        var equals = fromJson.equals(toJsonTree);
        System.out.println("Equal? " + equals);
    }

    private long time(Runnable task) {
        var start = System.currentTimeMillis();
        task.run();
        var end = System.currentTimeMillis();
        return end - start;
    }
}
