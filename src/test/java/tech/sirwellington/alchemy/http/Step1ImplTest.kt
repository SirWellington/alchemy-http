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
package tech.sirwellington.alchemy.http

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers.RETURNS_SMART_NULLS
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.BinaryGenerators.Companion.binary
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.io.IOException

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner::class)
@Repeat
class Step1ImplTest
{

    @Mock(answer = RETURNS_SMART_NULLS)
    private lateinit var stateMachine: AlchemyHttpStateMachine

    private lateinit var  request: HttpRequest

    @Captor
    private lateinit var  requestCaptor: ArgumentCaptor<HttpRequest>

    private lateinit var  instance: Step1Impl

    @Before
    fun setUp()
    {
        request = HttpRequest.Builder
                .newInstance()
                .build()

        instance = Step1Impl(stateMachine, request)
    }

    @DontRepeat
    @Test
    fun testConstructor()
    {
        assertThrows { Step1Impl(null as AlchemyHttpStateMachine, request) }
        assertThrows { Step1Impl(stateMachine, null as HttpRequest) }

    }

    @Test
    @Throws(Exception::class)
    fun testGet()
    {
        instance.get()

        verify(stateMachine).jumpToStep3(requestCaptor.capture())

        val passedRequest = requestCaptor.value
        assertThat(passedRequest, notNullValue())
        assertThat(passedRequest.method, equalTo(RequestMethod.GET))
        assertThat(passedRequest.requestHeaders, equalTo(this.request.requestHeaders))

    }

    @Test
    @Throws(Exception::class)
    fun testPost()
    {
        instance.post()

        verify<AlchemyHttpStateMachine>(stateMachine).jumpToStep2(requestCaptor.capture())

        val passedRequest = requestCaptor.value
        assertThat(passedRequest, notNullValue())
        assertThat(passedRequest.method, equalTo(RequestMethod.POST))
        assertThat(passedRequest.requestHeaders, equalTo(this.request.requestHeaders))

    }

    @Test
    @Throws(Exception::class)
    fun testPut()
    {
        instance.put()

        verify(stateMachine).jumpToStep2(requestCaptor.capture())

        val passedRequest = requestCaptor.value
        assertThat(passedRequest, notNullValue())
        assertThat(passedRequest.method, equalTo(RequestMethod.PUT))
        assertThat(passedRequest.requestHeaders, equalTo(this.request.requestHeaders))
    }

    @Test
    @Throws(Exception::class)
    fun testDelete()
    {
        instance.delete()

        verify<AlchemyHttpStateMachine>(stateMachine).jumpToStep2(requestCaptor.capture())

        val passedRequest = requestCaptor.value
        assertThat(passedRequest, notNullValue())
        assertThat(passedRequest.method, equalTo(RequestMethod.DELETE))
        assertThat(passedRequest.requestHeaders, equalTo(this.request.requestHeaders))

    }

    @Test
    @Throws(IOException::class)
    fun testDownload()
    {
        val bytes = one(binary(100000))
        val tempFile = TestFile.writeToTempFile(bytes)

        val url = tempFile.toURI().toURL()

        val download = instance.download(url)
        assertThat(download, equalTo(bytes))
    }

    @Test
    fun testToString()
    {
        val toString = instance.toString()
        assertThat(toString, containsString(request.toString()))
        assertThat(toString, containsString(stateMachine.toString()))

    }

}
