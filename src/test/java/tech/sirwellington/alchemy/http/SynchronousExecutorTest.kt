/*
 * Copyright © 2018. Sir Wellington.
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

package tech.sirwellington.alchemy.http

import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner

@RunWith(AlchemyTestRunner::class)
class SynchronousExecutorTest
{

    @Mock
    private lateinit var command: Runnable

    private lateinit var instance: SynchronousExecutor

    @Before
    @Throws(Exception::class)
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = SynchronousExecutor.newInstance()
    }

    @Test
    @Throws(Exception::class)
    fun testNewInstance()
    {
        instance = SynchronousExecutor.newInstance()
        assertThat(instance, notNullValue())
    }

    @Test
    @Throws(Exception::class)
    fun testExecute()
    {
        instance.execute(command)

        verify(command).run()
    }

    @Throws(Exception::class)
    private fun setupData()
    {

    }

    @Throws(Exception::class)
    private fun setupMocks()
    {

    }
}