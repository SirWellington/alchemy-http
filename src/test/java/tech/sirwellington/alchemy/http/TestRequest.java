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

import com.google.gson.JsonElement;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import org.mockito.Mockito;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.StringGenerators;

/**
 *
 * @author SirWellington
 */
@Internal
class TestRequest implements HttpRequest
{

    Map<String, String> requestHeaders = CollectionGenerators.mapOf(StringGenerators.alphabeticString(), StringGenerators.alphabeticString(), 20);
    Map<String, String> queryParams = CollectionGenerators.mapOf(StringGenerators.alphabeticString(), StringGenerators.alphabeticString(), 6);
    URL url = AlchemyGenerator.one(Generators.validUrls());
    JsonElement body = AlchemyGenerator.one(Generators.jsonElements());
    HttpVerb verb = Mockito.mock(HttpVerb.class);

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
    public HttpVerb getVerb()
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
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final TestRequest other = (TestRequest) obj;
        if (!Objects.equals(this.requestHeaders, other.requestHeaders))
        {
            return false;
        }
        if (!Objects.equals(this.queryParams, other.queryParams))
        {
            return false;
        }
        if (!Objects.equals(this.url, other.url))
        {
            return false;
        }
        if (!Objects.equals(this.body, other.body))
        {
            return false;
        }
        if (!Objects.equals(this.verb, other.verb))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TestRequest{" + "requestHeaders=" + requestHeaders + ", queryParams=" + queryParams + ", url=" + url + ", body=" + body + ", verb=" + verb + '}';
    }

}
