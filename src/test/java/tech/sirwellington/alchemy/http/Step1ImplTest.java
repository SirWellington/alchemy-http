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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.BinaryGenerators.binary;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
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
        request = HttpRequest.Builder.Companion
                .newInstance()
                .build();

        instance = new Step1Impl(stateMachine, request);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new Step1Impl(null, request));
        assertThrows(() -> new Step1Impl(stateMachine, null));

    }

    @Test
    public void testGet() throws Exception
    {
        instance.get();

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.getMethod(), equalTo(RequestMethod.GET));
        assertThat(passedRequest.getRequestHeaders(), equalTo(this.request.getRequestHeaders()));

    }

    @Test
    public void testPost() throws Exception
    {
        instance.post();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.getMethod(), equalTo(RequestMethod.POST));
        assertThat(passedRequest.getRequestHeaders(), equalTo(this.request.getRequestHeaders()));

    }

    @Test
    public void testPut() throws Exception
    {
        instance.put();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.getMethod(), equalTo(RequestMethod.PUT));
        assertThat(passedRequest.getRequestHeaders(), equalTo(this.request.getRequestHeaders()));
    }

    @Test
    public void testDelete() throws Exception
    {
        instance.delete();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.getMethod(), equalTo(RequestMethod.DELETE));
        assertThat(passedRequest.getRequestHeaders(), equalTo(this.request.getRequestHeaders()));

    }

    @Test
    public void testDownload() throws IOException
    {
        byte[] bytes = one(binary(100_000));
        File tempFile = TestFile.writeToTempFile(bytes);

        URL url = tempFile.toURI().toURL();

        byte[] download = instance.download(url);
        assertThat(download, is(bytes));
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(toString, containsString(request.toString()));
        assertThat(toString, containsString(stateMachine.toString()));

    }

}
