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

import static com.google.common.base.Charsets.UTF_8;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import static tech.sirwellington.alchemy.arguments.Assertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;
import tech.sirwellington.alchemy.http.exceptions.OperationFailedException;

/**
 *
 * @author SirWellington
 */
public interface HttpVerb
{

    HttpResponse execute(HttpClient apacheHttpClient, HttpRequest request) throws AlchemyHttpException;

    static HttpVerb get()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);

        Function<HttpRequest, HttpUriRequest> requestMapper = r ->
        {
            try
            {
                HttpGet get = new HttpGet(r.getUrl().toURI());
                return get;
            }
            catch (Exception ex)
            {
                LOG.error("Could not convert to Apache GET Request", ex);
                return null;
            }
        };
        return new BaseVerb(requestMapper);
    }

    static HttpVerb post()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);

        Function<HttpRequest, HttpUriRequest> requestMapper = r ->
        {
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
                LOG.error("Failed to convert {} to Apache HTTP POST Request", r, ex);
                return null;
            }

        };

        return new BaseVerb(requestMapper);
    }

    static HttpVerb put()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);

        Function<HttpRequest, HttpUriRequest> requestMapper = r ->
        {
            try
            {
                HttpPut put = new HttpPut(r.getUrl().toURI());
                if (r.hasBody())
                {
                    HttpEntity entity = new StringEntity(r.getBody().toString());
                    put.setEntity(entity);
                }

                return put;
            }
            catch (Exception ex)
            {
                LOG.error("Failed to convery {} to Apache HTTP PUT Request", r, ex);
                return null;
            }
        };

        return new BaseVerb(requestMapper);
    }

    static HttpVerb delete()
    {
        Logger LOG = LoggerFactory.getLogger(HttpVerb.class);
        Function<HttpRequest, HttpUriRequest> requestMapper = r ->
        {
            try
            {
                HttpDelete delete = new HttpDelete(r.getUrl().toURI());
                return delete;
            }
            catch (Exception ex)
            {
                LOG.error("Failed to convert {} to Apache Http DELETE Request", r, ex);
                return null;
            }
        };

        return new BaseVerb(requestMapper);
    }


}
