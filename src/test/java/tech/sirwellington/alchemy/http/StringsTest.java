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

import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AlchemyTestRunner.class)
@Repeat
public class StringsTest
{

    @GenerateString
    private String string;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

    }

    @Test
    public void testNullToEmpty() throws Exception
    {
        String result = Strings.nullToEmpty(string);
        assertThat(result, equalTo(string));

        result = Strings.nullToEmpty(null);
        assertThat(result, notNullValue());
        assertThat(result, equalTo(""));
    }

    @Test
    public void testIsNullOrEmpty() throws Exception
    {
        assertFalse(Strings.isNullOrEmpty(string));
        assertTrue(Strings.isNullOrEmpty(null));
        assertTrue(Strings.isNullOrEmpty(""));
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }
}