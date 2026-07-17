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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import io.mikael.urlbuilder.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.http.HttpAssertions.validRequest;

/**
 * @author SirWellington
 */
interface HttpConnectionPreparer
{

    HttpURLConnection map(HttpRequest request) throws AlchemyHttpException;

    static URL expandUrlFromRequest(HttpRequest request) throws AlchemyHttpException, URISyntaxException, MalformedURLException
    {
        checkThat(request).isA(validRequest());

        URL url = request.url();

        if (url == null)
        {
            throw new OperationFailedException("request is missing URL");
        }

        if (!request.hasQueryParams())
        {
            return url;
        }

        var uriBuilder = UrlBuilder.fromUrl(url);

        Map<String, String> queryParams = request.queryParams();

        if (queryParams != null)
        {
            for (var entry : queryParams.entrySet())
            {
                uriBuilder = uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }

        return uriBuilder.toUrl();
    }

    static HttpConnectionPreparer create()
    {
        return Impl.INSTANCE;
    }

    /**
     * Default implementation of {@link HttpConnectionPreparer}.
     */
    final class Impl implements HttpConnectionPreparer
    {

        private static final Logger LOG = LoggerFactory.getLogger(Impl.class);

        static final Impl INSTANCE = new Impl();

        private Impl()
        {
        }

        @Override
        public HttpURLConnection map(HttpRequest request) throws AlchemyHttpException
        {
            try
            {
                URL url = HttpConnectionPreparer.expandUrlFromRequest(request);
                var connection = url.openConnection();

                if (connection instanceof HttpURLConnection http)
                {
                    http.setRequestMethod(request.method().asString);
                    http.setDoInput(true);

                    if (request.hasBody())
                    {
                        http.setDoOutput(true);
                    }

                    var headers = request.requestHeaders();

                    if (headers != null)
                    {
                        for (var entry : headers.entrySet())
                        {
                            http.setRequestProperty(entry.getKey(), entry.getValue());
                        }
                    }

                    return http;
                }
                else
                {
                    throw new OperationFailedException("URL is not an HTTP URL: [" + url + "]");
                }
            }
            catch (AlchemyHttpException ex)
            {
                throw ex;
            }
            catch (Exception ex)
            {
                throw new OperationFailedException("Failed to prepare HTTP connection", ex);
            }
        }
    }
}
