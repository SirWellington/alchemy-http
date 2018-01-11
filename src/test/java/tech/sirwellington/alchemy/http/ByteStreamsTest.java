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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.*;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.test.junit.runners.*;

@RunWith(AlchemyTestRunner.class)
@Repeat
public class ByteStreamsTest
{

    private Integer binarySize;

    private byte[] binary;

    private InputStream istream;


    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

    }

    @Test
    public void testToByteArray() throws Exception
    {
        byte[] result = ByteStreams.toByteArray(istream);
        Assert.assertArrayEquals(binary, result);
    }

    private void setupData() throws Exception
    {
        binarySize = NumberGenerators.integers(100, 1000).get();
        binary = new byte[binarySize];
        istream = new ByteArrayInputStream(binary);
    }

    private void setupMocks() throws Exception
    {

    }
}