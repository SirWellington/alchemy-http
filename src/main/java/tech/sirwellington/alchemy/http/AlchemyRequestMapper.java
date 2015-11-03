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

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.NonNull;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static com.google.common.base.Charsets.UTF_8;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.http.HttpAssertions.notNullAndHasURL;

/**
 * {@code AlchemyRequestMappers} convert {@linkplain HttpRequest Alchemy Requests} into a
 * corresponding {@linkplain org.apache.http.HttpRequest Apache Request}.
 *
 * @author SirWellington
 */
@Internal
interface AlchemyRequestMapper
{

    HttpUriRequest convertToApacheRequest(@NonNull HttpRequest alchemyRequest) throws AlchemyHttpException;

    static final AlchemyRequestMapper GET = r ->
    {
        checkThat(r).is(notNullAndHasURL());

        try
        {
            HttpGet get = new HttpGet(r.getUrl().toURI());
            return get;
        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(r, "Could not convert to Apache GET Request", ex);
        }
    };

    static final AlchemyRequestMapper POST = r ->
    {
        checkThat(r).is(notNullAndHasURL());

        try
        {
            HttpPost post = new HttpPost(r.getUrl().toURI());

            if (r.hasBody())
            {
                HttpEntity entity = new StringEntity(r.getBody().toString(), UTF_8);
                post.setEntity(entity);
            }

            return post;
        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(r, "Could not convert to Apache POST Request", ex);
        }
    };

    static final AlchemyRequestMapper PUT = r ->
    {
        checkThat(r).is(notNullAndHasURL());

        try
        {
            HttpPut put = new HttpPut(r.getUrl().toURI());

            if (r.hasBody())
            {
                HttpEntity entity = new StringEntity(r.getBody().toString(), UTF_8);
                put.setEntity(entity);
            }

            return put;
        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(r, "Could not convert to Apache PUT Request", ex);
        }
    };

    static final AlchemyRequestMapper DELETE = r ->
    {
        checkThat(r).is(notNullAndHasURL());

        try
        {
            if (r.hasBody())
            {
                //Custom Delete that allows a Body in the request
                HttpDeleteWithBody delete = new HttpDeleteWithBody(r.getUrl().toURI());
                HttpEntity entity = new StringEntity(r.getBody().toString(), UTF_8);
                delete.setEntity(entity);
                return delete;
            }
            else
            {
                //The stock Apache Method
                return new HttpDelete(r.getUrl().toURI());

            }

        }
        catch (Exception ex)
        {
            throw new AlchemyHttpException(r, "Could not convert to Apache DELETE Request", ex);
        }
    };

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
