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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BinaryGenerators.binary;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.http.CommonAssertions.assertDeleteRequestMade;
import static tech.sirwellington.alchemy.http.CommonAssertions.assertGetRequestMade;
import static tech.sirwellington.alchemy.http.CommonAssertions.assertPostRequestMade;
import static tech.sirwellington.alchemy.http.CommonAssertions.assertPutRequestMade;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
public class Step1ImplTest
{

    @Mock(answer = RETURNS_SMART_NULLS)
    private AlchemyHttpStateMachine stateMachine;

    private HttpRequest request;

    @Captor
    ArgumentCaptor<HttpRequest> requestCaptor;

    private Step1Impl instance;

    @Before
    public void setUp()
    {
        request = HttpRequest.Builder
                .newInstance()
                .build();

        instance = new Step1Impl(stateMachine, request);
    }

    @Test
    public void testConstructor()
    {
        System.out.println("testConstructor");

        assertThrows(() -> new Step1Impl(null, request));
        assertThrows(() -> new Step1Impl(stateMachine, null));

    }

    @Test
    public void testGet() throws Exception
    {
        System.out.println("testGet");

        instance.get();

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertRequestMade(requestMade);
        assertGetRequestMade(requestMade.getVerb());
    }

    @Test
    public void testPost() throws Exception
    {
        System.out.println("testPost");

        instance.post();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertRequestMade(requestMade);
        assertPostRequestMade(requestMade.getVerb());
    }

    @Test
    public void testPut() throws Exception
    {
        System.out.println("testPut");

        instance.put();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertRequestMade(requestMade);
        assertPutRequestMade(requestMade.getVerb());
    }

    @Test
    public void testDelete() throws Exception
    {
        System.out.println("testDelete");

        instance.delete();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest requestMade = requestCaptor.getValue();
        assertRequestMade(requestMade);
        assertDeleteRequestMade(requestMade.getVerb());
    }

    @Test
    public void testDownload() throws IOException
    {
        byte[] bytes = one(binary(100_000));
        String filename = one(hexadecimalString(10));
        File tempFile = File.createTempFile(filename, ".txt");
        Files.write(bytes, tempFile);

        URL url = tempFile.toURI().toURL();
        
        byte[] download = instance.download(url);
        assertThat(download, is(bytes));
    }

    @Test
    public void testToString()
    {
        System.out.println("testToString");

        String toString = instance.toString();
        assertThat(toString, containsString(request.toString()));
        assertThat(toString, containsString(stateMachine.toString()));

    }

    private void assertRequestMade(HttpRequest requestMade)
    {
        assertThat(requestMade, notNullValue());
        assertThat(requestMade, not(sameInstance(request)));
    }

}
