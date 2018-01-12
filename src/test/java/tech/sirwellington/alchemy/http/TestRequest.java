
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

import java.net.URL;
import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.StringGenerators;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;

    /**
 *
 * @author SirWellington
 */
@Internal
class TestRequest implements HttpRequest
{

    Map<String, String> requestHeaders = CollectionGenerators.mapOf(StringGenerators.alphabeticStrings(), StringGenerators.alphabeticStrings(), 20);
    Map<String, String> queryParams = CollectionGenerators.mapOf(StringGenerators.alphabeticStrings(), StringGenerators.alphabeticStrings(), 6);
    URL url = one(Generators.validUrls());
    JsonElement body = one(Generators.jsonElements());
    HttpExecutor verb = Mockito.mock(HttpExecutor.class);

    @Override
    public Map<String, String> getRequestHeaders()
    {
        return requestHeaders;
    }

    @Override
    public Map<String, String> getQueryParams()
    {
        return queryParams;
    }

    @Override
    public boolean hasBody()
    {
        return body != null && body !=JsonNull.INSTANCE;
    }

    @Override
    public boolean hasQueryParams()
    {
        return Maps.isEmpty(queryParams);
    }

    @Override
    public boolean equals(@Nullable HttpRequest other)
    {
        return this.equals((Object) other);
    }

    @Override
    public URL getUrl()
    {
        return url;
    }

    @Override
    public JsonElement getBody()
    {
        return body;
    }

    @Override
    public HttpExecutor getHttpExecutor()
    {
        return verb;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.requestHeaders);
        hash = 19 * hash + Objects.hashCode(this.queryParams);
        hash = 19 * hash + Objects.hashCode(this.url);
        hash = 19 * hash + Objects.hashCode(this.body);
        hash = 19 * hash + Objects.hashCode(this.verb);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (!(obj instanceof HttpRequest))
        {
            return false;
        }

        final HttpRequest other = (HttpRequest) obj;
        if (!Objects.equals(this.requestHeaders, other.getRequestHeaders()))
        {
            return false;
        }
        if (!Objects.equals(this.queryParams, other.getQueryParams()))
        {
            return false;
        }
        if (!Objects.equals(this.url, other.getUrl()))
        {
            return false;
        }
        if (!Objects.equals(this.body, other.getBody()))
        {
            return false;
        }
        if (!Objects.equals(this.verb, other.getHttpExecutor()))
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return "TestRequest{" + "requestHeaders=" + requestHeaders + ", queryParams=" + queryParams + ", url=" + url + ", body=" + body + ", httpExecutor=" + verb + '}';
    }

}
