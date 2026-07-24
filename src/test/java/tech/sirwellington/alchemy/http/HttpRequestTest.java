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

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 * @author SirWellington
 */
@AlchemyTest
public class HttpRequestTest {

    private TestRequest testRequest;

    private HttpRequest instance;

    @BeforeEach
    public void setUp() throws Exception {
        testRequest = new TestRequest();
        instance = HttpRequest.copyOf(testRequest);
    }

    @Test
    public void testGetRequestHeaders() {
        assertThat(instance.requestHeaders(), equalTo(testRequest.requestHeaders));
    }

    @Test
    public void testGetQueryParams() {
        assertThat(instance.queryParams(), equalTo(testRequest.queryParams));
    }

    @Test
    public void testHasQueryParams() {
        testRequest.queryParams = Collections.emptyMap();
        instance = HttpRequest.copyOf(testRequest);
        assertThat(instance.hasQueryParams(), equalTo(false));

        testRequest.queryParams = CollectionGenerators.mapOf(
            alphabeticStrings(),
            alphabeticStrings(),
            10
        );
        instance = HttpRequest.copyOf(testRequest);
        assertThat(instance.hasQueryParams(), equalTo(true));
    }

    @Test
    public void testGetUrl() {
        assertThat(instance.url(), equalTo(testRequest.url));
    }

    @Test
    public void testGetBody() {
        assertThat(instance.body(), equalTo(testRequest.body));
    }

    @Test
    public void testGetRequestMethod() throws Exception {
        assertThat(instance.method(), equalTo(testRequest.method));
    }

    @Test
    public void testHasBody() {
        assertThat(instance.body(), equalTo(testRequest.body));
    }

    @Test
    public void testEquals() {
        assertThat(instance.equals(testRequest), equalTo(true));
        assertThat(testRequest.equals(instance), equalTo(true));
    }

    @Test
    public void testCopyOf() {
        var result = HttpRequest.copyOf(instance);
        assertThat(result, notNullValue());
        assertThat(result, equalTo(instance));
        assertThat(instance, equalTo(result));
    }

    @Test
    public void testFrom() {
        var result = HttpRequest.Builder.from(null);
        assertThat(result, notNullValue());
    }
}
