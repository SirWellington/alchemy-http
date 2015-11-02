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

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Objects;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.generator.StringGenerators;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

class TestResponse implements HttpResponse
{

    int statusCode = AlchemyGenerator.one(NumberGenerators.integers(200, 500));
    Map<String, String> responseHeaders = CollectionGenerators.mapOf(StringGenerators.alphabeticString(), StringGenerators.alphabeticString(), 10);
    JsonElement responseBody = AlchemyGenerator.one(Generators.jsonElements());
    private final Gson gson = Constants.getDefaultGson();

    TestResponse()
    {
    }

    TestResponse copy()
    {
        TestResponse clone = new TestResponse();
        clone.statusCode = this.statusCode;
        clone.responseHeaders = Maps.newHashMap(this.responseHeaders);
        clone.responseBody = gson.toJsonTree(responseBody);
        return clone;
    }

    @Override
    public int statusCode()
    {
        return statusCode;
    }

    @Override
    public Map<String, String> responseHeaders()
    {
        return responseHeaders;
    }

    @Override
    public String asString()
    {
        return responseBody.toString();
    }

    @Override
    public JsonElement asJSON() throws JsonException
    {
        return responseBody;
    }

    @Override
    public <Pojo> Pojo as(Class<Pojo> classOfPojo) throws JsonException
    {
        return gson.fromJson(responseBody, classOfPojo);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + this.statusCode;
        hash = 89 * hash + Objects.hashCode(this.responseHeaders);
        hash = 89 * hash + Objects.hashCode(this.responseBody);
        return hash;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
        {
            return false;
        }
        if (!(other instanceof HttpResponse))
        {
            return false;
        }
        return this.equals((HttpResponse) other);
    }

    @Override
    public String toString()
    {
        return "TestResponse{" + "statusCode=" + statusCode + ", responseHeaders=" + responseHeaders + ", responseBody=" + responseBody + '}';
    }

}
