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

import java.net.URL;
import java.util.Date;
import org.inferred.freebuilder.shaded.com.google.common.base.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;
import tech.sirwellington.alchemy.http.AlchemyRequest.OnSuccess;
import tech.sirwellington.alchemy.http.AlchemyRequest.Step4;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.http.Generators.validUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(MockitoJUnitRunner.class)
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

    private Step4 instance;

    @Before
    public void setUp()
    {
        instance = new Step4Impl(stateMachine, request, Pojo.class);
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

    @Test
    public void testAt()
    {
        //Edge cases
        assertThrows(() -> instance.at(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> instance.at((URL) null))
                .isInstanceOf(IllegalArgumentException.class);

        URL url = one(validUrls());

        instance.at(url);

        verify(stateMachine).executeSync(requestCaptor.capture(), eq(Pojo.class));

        HttpRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade, not(sameInstance(request)));
        assertThat(requestMade.getUrl(), is(url));
    }

    @Test
    public void testOnSuccess()
    {
        //Edge cases
        assertThrows(() -> instance.onSuccess(null))
                .isInstanceOf(IllegalArgumentException.class);

        instance.onSuccess(onSuccess);

        verify(stateMachine).jumpToStep5(request, Pojo.class, onSuccess);
    }

    @Test
    public void testToString()
    {
        assertThat(Strings.isNullOrEmpty(instance.toString()), is(false));
    }
    

    static class Pojo
    {

        private String firstName;
        private String lastName;
        private Date birthday;
        private String address;
        private int age;
    }

}
