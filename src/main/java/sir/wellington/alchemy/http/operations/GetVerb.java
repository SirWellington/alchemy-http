/*
 * Copyright 2015 Sir Wellington.
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
package sir.wellington.alchemy.http.operations;

import static com.google.common.base.Charsets.UTF_8;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.assertions.Assertions.notNull;
import static sir.wellington.alchemy.collections.maps.MapOperations.nullToEmpty;
import sir.wellington.alchemy.http.HttpResponse;
import sir.wellington.alchemy.http.exceptions.HttpException;

/**
 *
 * @author SirWellington
 */
class GetVerb implements HttpVerb
{

    private final static Logger LOG = LoggerFactory.getLogger(GetVerb.class);

    @Override
    public HttpResponse execute(HttpClient apacheHttpClient, HttpRequest request) throws HttpException
    {
        checkThat(request)
                .usingMessage("missing request")
                .is(notNull());

        checkThat(apacheHttpClient)
                .usingMessage("missing http client")
                .is(notNull());

        HttpPost post = new HttpPost();
        URL url = request.getUrl();
        HttpGet getRequest;
        try
        {
            getRequest = new HttpGet(url.toURI());
        }
        catch (URISyntaxException ex)
        {
            LOG.error("Failed to covert URL {} to URI", url, ex);
            throw new HttpException("Failed to convert URL to URI", ex);
        }

        Map<String, String> headers = nullToEmpty(request.getRequestHeaders());
        headers.forEach((k, v) -> getRequest.addHeader(k, v));
        
        try
        {
            apacheHttpClient.execute(getRequest);
        }
        catch(Exception ex)
        {
            LOG.error("Failed to execute GET Request on {}", url);
            throw new HttpException(ex);
        }

    }

    private HttpEntity extractBodyFrom(HttpRequest request)
    {
        HttpEntity entity = new StringEntity(request.getBody().toString(), UTF_8);
        return entity;
    }

}
