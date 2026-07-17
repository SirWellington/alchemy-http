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

import java.lang.reflect.Array;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.http.HttpAssertions.jsonArray;

/**
 * @author SirWellington
 */
class TestResponse implements HttpResponse
{

    public int statusCode = one(integers(200, 500));
    public Map<String, String> responseHeaders = mapOf(alphabeticStrings(), alphabeticStrings(), 10);
    public JsonElement responseBody = one(Generators.jsonElements());
    private final Gson gson = Constants.DEFAULT_GSON;

    @Override
    public boolean isOk()
    {
        return statusCode >= 200 && statusCode <= 208;
    }

    TestResponse copy()
    {
        TestResponse clone = new TestResponse();
        clone.statusCode = this.statusCode;
        clone.responseHeaders = new HashMap<>(this.responseHeaders);
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
        if (o instanceof HttpResponse other)
        {
            return HttpResponse.super.equals(other);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "TestResponse{statusCode=" + statusCode
                + ", responseHeaders=" + responseHeaders
                + ", responseBody=" + responseBody + "}";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> bodyAsArrayOf(Class<T> classOfT) throws JsonException
    {
        checkThat(this.responseBody).isA(jsonArray());

        var emptyArray = Array.newInstance(classOfT, 0);
        var arrayType = emptyArray.getClass();

        T[] array = (T[]) gson.fromJson(responseBody, arrayType);
        return Arrays.asList(array);
    }
}
