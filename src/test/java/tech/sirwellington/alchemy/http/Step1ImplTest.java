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

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.sirwellington.alchemy.generator.BinaryGenerators;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;

/**
 * @author SirWellington
 */
@AlchemyTest
public class Step1ImplTest {
    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private AlchemyHttpStateMachine stateMachine;

    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private Step1Impl instance;

    @BeforeEach
    public void setUp() {
        request = HttpRequest.Builder
            .newInstance()
            .build();

        instance = new Step1Impl(stateMachine, request);
    }

    @Test
    public void testGet() throws Exception {
        instance.get();

        verify(stateMachine).jumpToStep3(requestCaptor.capture());

        var passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.GET));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testPost() throws Exception {
        instance.post();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        var passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.POST));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testPut() throws Exception {
        instance.put();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        var passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.PUT));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testDelete() throws Exception {
        instance.delete();

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        var passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(RequestMethod.DELETE));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testCustomMethod() throws Exception {
        RequestMethod method = RequestMethod.any();
        instance.method(method);

        verify(stateMachine).jumpToStep2(requestCaptor.capture());

        var passedRequest = requestCaptor.getValue();
        assertThat(passedRequest, notNullValue());
        assertThat(passedRequest.method(), equalTo(method));
        assertThat(passedRequest.requestHeaders(), equalTo(this.request.requestHeaders()));
    }

    @Test
    public void testDownload() throws IOException {
        var bytes = one(BinaryGenerators.binary(100000));
        var tempFile = TestFile.writeToTempFile(bytes);

        var url = tempFile.toURI().toURL();

        var download = instance.download(url);
        assertThat(download, equalTo(bytes));
    }

    @Test
    public void testDownloadString() throws Exception {
        var binary = one(BinaryGenerators.binary(10_000));
        var tempFile = TestFile.writeToTempFile(binary);

        var urlString = tempFile.toURI().toURL().toString();

        var download = instance.download(urlString);

        assertThat(download, equalTo(binary));
    }

    @Test
    public void testToString() {
        var toString = instance.toString();
        assertThat(toString, containsString(request.toString()));
        assertThat(toString, containsString(stateMachine.toString()));
    }
}
