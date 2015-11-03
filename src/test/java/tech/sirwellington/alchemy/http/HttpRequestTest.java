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

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpRequestTest
{
    private TestRequest testRequest;

    private HttpRequest instance;

    @Before
    public void setUp() throws Exception
    {
        testRequest = new TestRequest();
        instance = HttpRequest.copyOf(testRequest);
    }

    @Test
    public void testGetRequestHeaders()
    {
        assertThat(instance.getRequestHeaders(), is(testRequest.requestHeaders));
    }

    @Test
    public void testGetQueryParams()
    {
        assertThat(instance.getQueryParams(), is(testRequest.queryParams));
    }

    @Test
    public void testHasQueryParams()
    {
        testRequest.queryParams = null;
        instance = HttpRequest.copyOf(testRequest);
        assertThat(instance.hasQueryParams(), is(false));

        testRequest.queryParams = Collections.emptyMap();
        instance = HttpRequest.copyOf(testRequest);
        assertThat(instance.hasQueryParams(), is(false));

        testRequest.queryParams = mapOf(alphabeticString(), alphabeticString(), 10);
        instance = HttpRequest.copyOf(testRequest);
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
        assertThat(instance.getBody(), is(testRequest.body));
    }

    @Test
    public void testGetVerb()
    {
        assertThat(instance.getVerb(), is(testRequest.getVerb()));
    }

    @Test
    public void testHasBody()
    {
        assertThat(instance.getBody(), is(testRequest.body));
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
        HttpRequest result = HttpRequest.copyOf(instance);
        assertThat(result, notNullValue());
        assertThat(result, is(instance));
        assertThat(instance, is(result));
    }

}
