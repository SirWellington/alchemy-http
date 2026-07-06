/*
 * Copyright © 2019. Sir Wellington.
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.generator.BinaryGenerators;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class Step1ImplTest
{
    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private AlchemyHttpStateMachine stateMachine;

    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

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
    public void testGet() throws Exception
    {
        instance.get();

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.GET));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testPost() throws Exception
    {
        instance.post();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.POST));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testPut() throws Exception
    {
        instance.put();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.PUT));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testDelete() throws Exception
    {
        instance.delete();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.DELETE));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testCustomMethod() throws Exception
    {
        RequestMethod method = RequestMethod.any();
        instance.method(method);

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        HttpRequest passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(method));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testDownload() throws IOException
    {
        byte[] bytes = one(BinaryGenerators.binary(100000));
        File tempFile = TestFile.writeToTempFile(bytes);

        URL url = tempFile.toURI().toURL();

        byte[] download = instance.download(url);
        assertThat(download, equalTo(bytes));
    }

    @Test
    public void testDownloadString() throws Exception
    {
        byte[] binary = one(BinaryGenerators.binary(10_000));
        File tempFile = TestFile.writeToTempFile(binary);

        String urlString = tempFile.toURI().toURL().toString();

        byte[] download = instance.download(urlString);

        assertThat(download, equalTo(binary));
    }

    @Test
    public void testToString()
    {
        String toString = instance.toString();
        assertThat(toString, containsString(request.toString()));
        assertThat(toString, containsString(stateMachine.toString()));
    }
}
