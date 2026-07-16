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

import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

@RunWith(AlchemyTestRunner.class)
@Repeat
public class HttpStatusCodeTest
{

    @GenerateEnum
    private HttpStatusCode status;

    @Test
    public void testMatchesCode()
    {
        int code = status.getCode();
        assertTrue(status.matchesCode(code));

        HttpStatusCode other = HttpStatusCode.anyExcept(status);
        assertFalse(other.matchesCode(code));
    }

    @Test
    public void testForCode()
    {
        int code = status.getCode();
        HttpStatusCode result = HttpStatusCode.forCode(code);
        assertThat(result, equalTo(status));
    }

    @Test
    public void testForCodeWhenUnknown()
    {
        int code = one(integers(-100, 100));
        HttpStatusCode result = HttpStatusCode.forCode(code);
        assertThat(result, nullValue());
    }

    @Test
    public void testAnyExcept()
    {
        HttpStatusCode result = HttpStatusCode.anyExcept(status);
        assertThat(result, notNullValue());
        assertThat(result, not(equalTo(status)));
    }
}
