/*
 * Copyright © 2019. Sir Wellington.
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

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 * @author SirWellington
 */
class TestRequest implements HttpRequest
{

    public Map<String, String> queryParams = mapOf(alphabeticStrings(), alphabeticStrings(), 6);
    public URL url = one(Generators.validUrls());
    public JsonElement body = one(Generators.jsonElements());
    public RequestMethod method = Constants.DEFAULT_REQUEST_METHOD;
    public Map<String, String> requestHeaders = mapOf(alphabeticStrings(), alphabeticStrings(), 20);

    @Override
    public Map<String, String> requestHeaders()
    {
        return requestHeaders;
    }

    @Override
    public Map<String, String> queryParams()
    {
        return queryParams;
    }

    @Override
    public URL url()
    {
        return url;
    }

    @Override
    public JsonElement body()
    {
        return body;
    }

    @Override
    public RequestMethod method()
    {
        return method;
    }

    @Override
    public boolean hasBody()
    {
        return body != null && !(body instanceof JsonNull);
    }

    @Override
    public boolean hasQueryParams()
    {
        return queryParams != null && !queryParams.isEmpty();
    }

    @Override
    public boolean hasMethod()
    {
        return method != null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof HttpRequest other)
        {
            return HttpRequest.super.equals(other);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(queryParams, url, body, method, requestHeaders);
    }

    @Override
    public String toString()
    {
        return "TestRequest(queryParams=" + queryParams
                + ", url=" + url
                + ", body=" + body
                + ", method=" + method
                + ", requestHeaders=" + requestHeaders + ")";
    }
}
