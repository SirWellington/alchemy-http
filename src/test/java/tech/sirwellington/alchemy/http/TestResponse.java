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

import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.generator.*;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.http.HttpAssertions.jsonArray;

class TestResponse implements HttpResponse
{

    int statusCode = one(NumberGenerators.integers(200, 500));
    Map<String, String> responseHeaders = CollectionGenerators.mapOf(StringGenerators.alphabeticStrings(), StringGenerators.alphabeticStrings(), 10);
    JsonElement responseBody = one(Generators.INSTANCE.jsonElements());
    private final Gson gson = Constants.DEFAULT_GSON;

    TestResponse()
    {
    }

    TestResponse copy()
    {
        TestResponse clone = new TestResponse();
        clone.statusCode = this.statusCode;
        clone.responseHeaders = Maps.mutableCopyOf(this.responseHeaders);
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
    public String bodyAsString()
    {
        return responseBody.toString();
    }

    @Override
    public JsonElement body() throws JsonException
    {
        return responseBody;
    }

    @Override
    public <Pojo> Pojo bodyAs(Class<Pojo> classOfPojo) throws JsonException
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
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null)
        {
            return false;
        }

        if (!(o instanceof HttpResponse))
        {
            return false;
        }

        HttpResponse that = (HttpResponse) o;

        return statusCode == that.statusCode() &&
                Objects.equals(responseHeaders, that.responseHeaders()) &&
                Objects.equals(responseBody, that.body());
    }

    @Override
    public boolean equals(@Nullable HttpResponse other)
    {
        return this.equals((Object) other);
    }

    @Override
    public String toString()
    {
        return "TestResponse{" + "statusCode=" + statusCode + ", responseHeaders=" + responseHeaders + ", responseBody=" + responseBody + '}';
    }

    @Override
    public <T> List<T> bodyAsArrayOf(Class<T> classOfT) throws JsonException
    {
        checkThat(this.responseBody).is(jsonArray());

        Type type = new TypeToken<List<T>>() {}.getType();

        return gson.fromJson(responseBody, type);
    }

    @Override
    public boolean isOk()
    {
        return statusCode >= 200 && statusCode <= 208;
    }
}
