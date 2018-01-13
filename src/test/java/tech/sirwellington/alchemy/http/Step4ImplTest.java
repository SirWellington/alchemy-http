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

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step4;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class Step4ImplTest
{

    @Mock
    private AlchemyHttpStateMachine stateMachine;

    @Mock
    private HttpRequest request;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    @Mock
    private OnSuccess onSuccess;

    private Class<TestPojo> responseClass;

    private Step4 instance;

    @Before
    public void setUp()
    {
        responseClass = TestPojo.class;

        instance = new Step4Impl(stateMachine, request, responseClass);
        verifyZeroInteractions(stateMachine);
    }

    @Test
    public void testConstructor()
    {
        assertThrows(() -> new Step4Impl<>(null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Step4Impl<>(stateMachine, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Step4Impl<>(stateMachine, request, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Repeat
    @Test
    public void testAt()
    {
        URL url = one(INSTANCE.validUrls());

        instance.at(url);

        verify(stateMachine).executeSync(requestCaptor.capture(), eq(responseClass));

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade, not(sameInstance(request)));
        assertThat(requestMade.getUrl(), is(url));
    }

    @Test
    public void testAtWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.at((URL) null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testOnSuccess()
    {
        //Edge cases
        assertThrows(() -> instance.onSuccess(null))
                .isInstanceOf(IllegalArgumentException.class);

        instance.onSuccess(onSuccess);

        verify(stateMachine).jumpToStep5(request, responseClass, onSuccess);
    }

    @Test
    public void testToString()
    {
        assertThat(Strings.INSTANCE.isNullOrEmpty(instance.toString()), is(false));
    }

}
