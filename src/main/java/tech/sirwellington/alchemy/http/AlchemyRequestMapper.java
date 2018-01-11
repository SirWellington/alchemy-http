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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CLIENT;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.http.HttpAssertions.notNullAndHasURL;

/**
 * {@code AlchemyRequestMappers} convert {@linkplain HttpRequest Alchemy Requests} into a
 * corresponding {@linkplain org.apache.http.HttpRequest Apache Request}.
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
@Internal
interface AlchemyRequestMapper
{

    HttpUriRequest convertToApacheRequest(@Required HttpRequest alchemyRequest) throws AlchemyHttpException;

    @Internal
    static URL expandUrlFromRequest(@Required HttpRequest request) throws URISyntaxException, MalformedURLException
    {
        checkThat(request).is(notNullAndHasURL());

        if (!request.hasQueryParams())
        {
            return request.getUrl();
        }
        else
        {
            URI uri = request.getUrl().toURI();
            URIBuilder uriBuilder = new URIBuilder(uri);

            request.getQueryParams()
                    .forEach(uriBuilder::addParameter);

            return uriBuilder.build().toURL();
        }
    }

    @StrategyPattern(role = CLIENT)
    static final AlchemyRequestMapper GET = request ->
    {
        checkThat(request).is(notNullAndHasURL());

        try
        {
            URL url = expandUrlFromRequest(request);
            HttpGet get = new HttpGet(url.toURI());
            return get;
        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(request, "Could not convert to Apache GET Request", ex);
        }
    };

    static final AlchemyRequestMapper POST = request ->
    {
        checkThat(request).is(notNullAndHasURL());

        try
        {
            URL url = expandUrlFromRequest(request);
            HttpPost post = new HttpPost(url.toURI());

            if (request.hasBody())
            {
                HttpEntity entity = new StringEntity(request.getBody().toString(), UTF_8);
                post.setEntity(entity);
            }

            return post;
        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(request, "Could not convert to Apache POST Request", ex);
        }
    };

    static final AlchemyRequestMapper PUT = request ->
    {
        checkThat(request).is(notNullAndHasURL());

        try
        {
            URL url = expandUrlFromRequest(request);
            HttpPut put = new HttpPut(url.toURI());

            if (request.hasBody())
            {
                HttpEntity entity = new StringEntity(request.getBody().toString(), UTF_8);
                put.setEntity(entity);
            }

            return put;
        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(request, "Could not convert to Apache PUT Request", ex);
        }
    };

    static final AlchemyRequestMapper DELETE = request ->
    {
        checkThat(request).is(notNullAndHasURL());

        try
        {
            URL url = expandUrlFromRequest(request);
            if (request.hasBody())
            {
                //Custom Delete that allows a Body in the request
                HttpDeleteWithBody delete = new HttpDeleteWithBody(url.toURI());
                HttpEntity entity = new StringEntity(request.getBody().toString(), UTF_8);
                delete.setEntity(entity);
                return delete;
            }
            else
            {
                //The stock Apache Method
                return new HttpDelete(url.toURI());
            }
        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(request, "Could not convert to Apache DELETE Request", ex);
        }
    };

    /**
     * The stock {@linkplain HttpDelete Apache Delete Request} does not allow a Body. This one does.
     */
    @Internal
    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase
    {

        public static final String METHOD_NAME = new HttpDelete().getMethod();

        @Override
        public String getMethod()
        {
            return METHOD_NAME;
        }

        public HttpDeleteWithBody(final String uri)
        {
            super();
            setURI(URI.create(uri));
        }

        public HttpDeleteWithBody(final URI uri)
        {
            super();
            setURI(uri);
        }

        public HttpDeleteWithBody()
        {
            super();
        }

    }

}
