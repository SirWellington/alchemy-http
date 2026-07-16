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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.BinaryGenerators;
import tech.sirwellington.alchemy.generator.CollectionGenerators;
import tech.sirwellington.alchemy.generator.StringGenerators;
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.*;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RepeatedTest(100)
@RunWith(AlchemyTestRunner.class)
public class AlchemyRequestTest
{
    private URL url;
    private Map<String, String> headers;

    @Before
    public void setUp()
    {
        url = one(Generators.validUrls());
        headers = CollectionGenerators.mapOf(alphabeticStrings(), alphabeticStrings(), 14);
    }

    @Test
    public void testSomeMethod()
    {
    }

    @Test
    public void testStep1() throws IOException
    {
        Step1 instance = new Step1()
        {
            @Override
            public Step3 get()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Step2 post()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Step2 put()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Step2 delete()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Step2 method(RequestMethod requestMethod)
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        // Test the built-in download()
        byte[] data = BinaryGenerators.binary(100000).get();
        File tempFile = TestFile.writeToTempFile(data);
        byte[] result = instance.download(tempFile.toURI().toURL());
        assertThat(result, is(data));
    }

    @Test
    public void testStep3() throws Exception
    {
        class Step3TestImpl implements Step3
        {
            URL url;
            final Map<String, String> savedHeaders = new HashMap<>();

            @Override
            public Step3 usingHeader(String key, String value)
            {
                savedHeaders.put(key, value);
                return this;
            }

            @Override
            public Step3 usingQueryParam(String name, String value)
            {
                return this;
            }

            @Override
            public Step3 followRedirects(int maxNumberOfTimes)
            {
                return this;
            }

            @Override
            public HttpResponse at(URL url) throws AlchemyHttpException
            {
                this.url = url;
                return mock(HttpResponse.class);
            }

            @Override
            public Step5<HttpResponse> onSuccess(OnSuccess<HttpResponse> onSuccessCallback)
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public <ResponseType> Step4<ResponseType> expecting(Class<ResponseType> classOfResponseType)
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        Step3TestImpl instance = new Step3TestImpl();

        // Test the built-in at(String) function
        instance.at(url.toString());
        assertThat(instance.url, equalTo(this.url));
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);

        // Test the built-in accept(String...) function
        AlchemyGenerator<String> types = alphabeticStrings();
        String first = types.get();
        String second = types.get();
        String third = types.get();

        instance.accept(first, second, third);

        assertThat(instance.savedHeaders, hasKey("Accept"));
        String expected = first + "," + second + "," + third;
        assertThat(instance.savedHeaders.get("Accept"), is(expected));

        // Edge cases
        assertThrows(() -> instance.accept("", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testStep4() throws Exception
    {
        class TestImpl implements Step4<String>
        {
            URL url;
            OnSuccess<?> onSuccess;

            @Override
            public String at(URL url) throws AlchemyHttpException
            {
                this.url = url;
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Step5<String> onSuccess(OnSuccess<String> onSuccessCallback)
            {
                this.onSuccess = onSuccessCallback;
                return mock(Step5.class);
            }
        }

        TestImpl instance = new TestImpl();

        // Test built-in URL method
        instance.at(url.toString());
        assertThat(instance.url, equalTo(this.url));

        // Edge cases
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testStep6() throws Exception
    {
        class TestImpl implements Step6<String>
        {
            URL url;

            @Override
            public void at(URL url)
            {
                this.url = url;
            }
        }

        TestImpl instance = new TestImpl();

        // Test built-in at() method
        instance.at(url.toString());
        assertThat(instance.url, equalTo(this.url));

        // Edge cases
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOnSuccessNoOp()
    {
        OnSuccess<String> instance = OnSuccess.NO_OP;
        assertThat(instance, notNullValue());

        String response = one(alphabeticStrings());
        instance.processResponse(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOnSuccessCreate()
    {
        String string = StringGenerators.hexadecimalString(30).get();
        OnSuccess<String> mockOnSuccess = mock(OnSuccess.class);

        OnSuccess<String> result = OnSuccess.create(r -> mockOnSuccess.processResponse(string));

        result.processResponse(string);
        verify(mockOnSuccess).processResponse(string);
    }

    @Test
    public void testOnFailureNoOp()
    {
        OnFailure instance = OnFailure.NO_OP;
        assertThat(instance, notNullValue());

        AlchemyHttpException ex = new AlchemyHttpException();
        instance.handleError(ex);
    }

    @Test
    public void testOnFailureCreate()
    {
        OnFailure mockOnFailure = mock(OnFailure.class);
        AlchemyHttpException ex = new AlchemyHttpException(Generators.validUrls().get().toString());
        OnFailure result = OnFailure.create(e -> mockOnFailure.handleError(ex));

        result.handleError(ex);
        verify(mockOnFailure).handleError(ex);
    }

    public interface MockInterface<ResponseType>
    {
        void handleError(AlchemyHttpException ex);

        void handleSuccess(ResponseType response);
    }
}
