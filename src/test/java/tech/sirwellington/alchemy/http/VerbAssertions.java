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

import com.google.gson.Gson;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHttpResponse;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;


@Internal
class VerbAssertions
{

    private final static Logger LOG = LoggerFactory.getLogger(VerbAssertions.class);

    static void assertGetRequestMade(HttpVerb verb) throws Exception
    {
        assertRequestWith(verb, HttpGet.class);
    }

    static void assertPostRequestMade(HttpVerb verb) throws Exception
    {
        assertRequestWith(verb, HttpPost.class);
    }

    static void assertPutRequestMade(HttpVerb verb) throws Exception
    {
        assertRequestWith(verb, HttpPut.class);
    }

    static void assertDeleteRequestMade(HttpVerb verb) throws Exception
    {
        assertRequestWith(verb, HttpDelete.class);
    }

    private static void assertRequestWith(HttpVerb verb, Class<? extends HttpUriRequest> type) throws Exception
    {
        HttpClient mockClient = mock(HttpClient.class);
        when(mockClient.execute(any(HttpUriRequest.class)))
                .thenReturn(createFakeApacheResponse());

        URI uri = createFakeUri();

        Gson gson = Constants.getDefaultGson();
        HttpRequest request = HttpRequest.Builder.newInstance()
                .usingUrl(uri.toURL())
                .build();

        verb.execute(mockClient, gson, request);

        ArgumentCaptor<HttpUriRequest> captor = forClass(HttpUriRequest.class);

        verify(mockClient).execute(captor.capture());

        HttpUriRequest requestMade = captor.getValue();

        assertThat(requestMade, notNullValue());
        assertThat(requestMade.getURI(), is(uri));
        assertThat(requestMade.getClass(), sameInstance(type));
    }

    private static org.apache.http.HttpResponse createFakeApacheResponse()
    {
        org.apache.http.HttpResponse response;
        response = new BasicHttpResponse(statusLineWithCode(200));
        return response;
    }

    private static StatusLine statusLineWithCode(int code)
    {
        return new StatusLine()
        {

            @Override
            public ProtocolVersion getProtocolVersion()
            {
                return mock(ProtocolVersion.class);
            }

            @Override
            public int getStatusCode()
            {
                return code;
            }

            @Override
            public String getReasonPhrase()
            {
                return "just because";
            }
        };
    }

    private static URI createFakeUri() throws URISyntaxException
    {
        String uri = "http://" + one(alphabeticString());
        return new URI(uri);
    }
}
