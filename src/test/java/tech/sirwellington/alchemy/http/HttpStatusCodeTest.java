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

import org.junit.jupiter.api.Test;
import tech.sirwellington.alchemy.test.AlchemyTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

@AlchemyTest
public class HttpStatusCodeTest
{

    private HttpStatusCode status;

    @Test
    public void testMatchesCode()
    {
        int code = status.getCode();
        assertThat(status.matchesCode(code), equalTo(true));

        HttpStatusCode other = HttpStatusCode.anyExcept(status);
        assertThat(other.matchesCode(code), equalTo(false));
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
