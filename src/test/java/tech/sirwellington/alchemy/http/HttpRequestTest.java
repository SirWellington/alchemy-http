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

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat(50)
public class HttpRequestTest
{

    private TestRequest testRequest;

    private HttpRequest instance;

    @Before
    public void setUp() throws Exception
    {
        testRequest = new TestRequest();
        instance = HttpRequest.Companion.copyOf(testRequest);
    }

    @Test
    public void testGetRequestHeaders()
    {
        assertThat(instance.getRequestHeaders(), is(testRequest.getRequestHeaders()));
    }

    @Test
    public void testGetQueryParams()
    {
        assertThat(instance.getQueryParams(), is(testRequest.getQueryParams()));
    }

    @Test
    public void testHasQueryParams()
    {
        testRequest.setQueryParams(null);
        instance = HttpRequest.Companion.copyOf(testRequest);
        assertThat(instance.hasQueryParams(), is(false));

        testRequest.setQueryParams(Collections.emptyMap());
        instance = HttpRequest.Companion.copyOf(testRequest);
        assertThat(instance.hasQueryParams(), is(false));

        testRequest.setQueryParams(mapOf(alphabeticStrings(), alphabeticStrings(), 10));
        instance = HttpRequest.Companion.copyOf(testRequest);
        assertThat(instance.hasQueryParams(), is(true));

    }

    @Test
    public void testGetUrl()
    {
        assertThat(instance.getUrl(), is(testRequest.getUrl()));
    }

    @Test
    public void testGetBody()
    {
        assertThat(instance.getBody(), is(testRequest.getBody()));
    }

    @Test
    public void testGetRequestMethod() throws Exception
    {
        assertThat(instance.getMethod(), equalTo(testRequest.getMethod()));
    }

    @Test
    public void testHasBody()
    {
        assertThat(instance.getBody(), is(testRequest.getBody()));
    }

    @Test
    public void testEquals()
    {
        assertThat(instance.equals(testRequest), is(true));
        assertThat(testRequest.equals(instance), is(true));
    }

    @Test
    public void testCopyOf()
    {
        HttpRequest result = HttpRequest.Companion.copyOf(instance);
        assertThat(result, notNullValue());
        assertThat(result, is(instance));
        assertThat(instance, is(result));
    }

    @Test
    public void testFrom()
    {
        HttpRequest.Builder result = HttpRequest.Builder.Companion.from(null);
        assertThat(result, notNullValue());
    }
}
