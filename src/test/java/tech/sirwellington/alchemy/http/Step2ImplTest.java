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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.http.JsonGenerators.jsonObjects;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class Step2ImplTest
{

    @Mock
    private AlchemyHttpStateMachine stateMachine;

    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private JsonElement expectedBody;

    private Gson gson;

    private Step2Impl instance;

    @Before
    public void setUp()
    {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();

        request = HttpRequest.Builder.newInstance().build();

        instance = new Step2Impl(request, stateMachine, gson);

        expectedBody = one(jsonObjects());
    }

    @Test
    public void testNothing()
    {
        instance.nothing();

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        expectedBody = JsonNull.INSTANCE;
        HttpRequest requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }

    @Test
    public void testStringBody()
    {
        String stringBody = gson.toJson(expectedBody);
        instance.body(stringBody);

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }

    @Test
    public void testStringBodyWhenEmpty()
    {
        assertThrows(() -> instance.body(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testObjectBody()
    {

        Pojo pojo = new Pojo();

        instance.body(pojo);

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        expectedBody = gson.toJsonTree(pojo);
        HttpRequest requestMade = requestCaptor.getValue();
        verifyRequestMade(requestMade);
    }

    @Test
    public void testObjectBodyWhenNull()
    {
        assertThrows(() -> instance.body(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void verifyRequestMade(HttpRequest requestMade)
    {
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.getBody(), is(expectedBody));
    }

    static class Pojo
    {

        private String name = one(alphabeticString());
        private int age = one(integers(1, 100));
        private String lastName = one(alphabeticString());
        private String address = one(alphabeticString());

        public Pojo()
        {
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 97 * hash + Objects.hashCode(this.name);
            hash = 97 * hash + this.age;
            hash = 97 * hash + Objects.hashCode(this.lastName);
            hash = 97 * hash + Objects.hashCode(this.address);
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
            final Pojo other = (Pojo) obj;
            if (!Objects.equals(this.name, other.name))
            {
                return false;
            }
            if (this.age != other.age)
            {
                return false;
            }
            if (!Objects.equals(this.lastName, other.lastName))
            {
                return false;
            }
            if (!Objects.equals(this.address, other.address))
            {
                return false;
            }
            return true;
        }

    }
}
