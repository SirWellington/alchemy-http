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
import tech.sirwellington.alchemy.test.generation.GenerateEnum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

@AlchemyTest
final class HttpStatusCodeTest {

    @GenerateEnum
    private HttpStatusCode status;

    @Test
    public void testMatchesCode() {
        var code = status.getCode();
        assertThat(status.matchesCode(code), equalTo(true));

        var otherCode = HttpStatusCode.anyExcept(status);
        assertThat(otherCode.matchesCode(code), equalTo(false));
    }

    @Test
    public void testForCode() {
        var code = status.getCode();
        var resultCode = HttpStatusCode.forCode(code);
        assertThat(resultCode, equalTo(status));
    }

    @Test
    public void testForCodeWhenUnknown() {
        var code = one(integers(-100, 100));
        var resultCode = HttpStatusCode.forCode(code);
        assertThat(resultCode, nullValue());
    }

    @Test
    public void testAnyExcept() {
        var resultCode = HttpStatusCode.anyExcept(status);
        assertThat(resultCode, notNullValue());
        assertThat(resultCode, not(equalTo(status)));
    }
}
