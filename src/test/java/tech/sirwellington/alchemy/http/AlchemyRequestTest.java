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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.http.AlchemyRequest.*;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.BinaryGenerators.binary;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.mapOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.http.Generators.validUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AlchemyRequestTest
{

    private URL url;

    private Map<String, String> headers;

    @Before
    public void setUp()
    {
        url = one(validUrls());
        headers = mapOf(alphabeticStrings(), alphabeticStrings(), 14);
    }

    @Test
    public void testSomeMethod()
    {
    }

    @Test
    public void testStep1() throws IOException
    {
        class TestImpl implements Step1
        {

            @Override
            public AlchemyRequest.Step3 get()
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public AlchemyRequest.Step2 post()
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public AlchemyRequest.Step2 put()
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public AlchemyRequest.Step2 delete()
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        }
        TestImpl instance = new TestImpl();

        //Test the built-in download()
        byte[] data = binary(100_000).get();
        File tempFile = TestFile.writeToTempFile(data);
        byte[] result = instance.download(tempFile.toURI().toURL());
        assertThat(result, is(data));
    }

    @Test
    public void testStep3() throws Exception
    {
        class TestImpl implements AlchemyRequest.Step3
        {

            private URL url;
            private Map<String, String> savedHeaders = Maps.create();

            @Override
            public AlchemyRequest.Step3 usingHeader(String key, String value) throws IllegalArgumentException
            {
                savedHeaders.put(key, value);
                return this;
            }

            @Override
            public AlchemyRequest.Step3 usingQueryParam(String name, String value) throws IllegalArgumentException
            {
                return this;
            }

            @Override
            public AlchemyRequest.Step3 followRedirects(int maxNumberOfTimes) throws IllegalArgumentException
            {
                return this;
            }

            @Override
            public HttpResponse at(URL url) throws AlchemyHttpException
            {
                this.url = url;
                return null;
            }

            @Override
            public AlchemyRequest.Step5<HttpResponse> onSuccess(OnSuccess<HttpResponse> onSuccessCallback)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public <ResponseType> AlchemyRequest.Step4<ResponseType> expecting(Class<ResponseType> classOfResponseType) throws IllegalArgumentException
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }

        TestImpl instance = new TestImpl();

        //Test the built-in at(String) function
        instance.at(url.toString());
        assertThat(instance.url, is(this.url));
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);

        //Test the built-in accepts(String...) function
        AlchemyGenerator<String> types = alphabeticStrings();
        String first = types.get();
        String second = types.get();
        String third = types.get();
        instance.accept(first, second, third);

        assertThat(instance.savedHeaders, hasKey("Accept"));
        String expected = first + "," + second + "," + third;
        assertThat(instance.savedHeaders.get("Accept"), is(expected));

        //Edge cases
        assertThrows(() -> instance.accept("", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testStep4() throws Exception
    {
        class TestImpl<T> implements AlchemyRequest.Step4<T>
        {

            private URL url;
            private OnSuccess onSuccess;

            @Override
            public T at(URL url) throws IllegalArgumentException, AlchemyHttpException
            {
                this.url = url;
                return null;
            }

            @Override
            public AlchemyRequest.Step5<T> onSuccess(OnSuccess<T> onSuccessCallback)
            {
                this.onSuccess = onSuccessCallback;
                return null;
            }

        }

        TestImpl<String> instance = new TestImpl<>();

        //Test built-in URL method
        instance.at(url.toString());
        assertThat(instance.url, is(this.url));

         //Edge cases
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testStep6() throws Exception
    {
        class TestImpl<T> implements AlchemyRequest.Step6<T>
        {

            private URL url;

            @Override
            public void at(URL url)
            {
                this.url = url;
            }

        }

        TestImpl<String> instance = new TestImpl<>();

        //Test built-in at() method
        instance.at(url.toString());
        assertThat(instance.url, is(this.url));

        //Edge cases
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testOnSuccessNoOp()
    {
        OnSuccess instance = OnSuccess.NO_OP;
        assertThat(instance, notNullValue());

        String response = one(alphabeticStrings());
        instance.processResponse(response);
    }

    @Test
    public void testOnFailureNoOp()
    {
        OnFailure instance = OnFailure.NO_OP;
        assertThat(instance, notNullValue());

        AlchemyHttpException ex = new AlchemyHttpException();
        instance.handleError(ex);
    }

}
