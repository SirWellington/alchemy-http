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

import java.util.Date;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.DateGenerators.pastDates;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.*;

/**
 * This is an example POJO (Plain Old Java Object) to be used for testing purposes only.
 *
 * @author SirWellington
 */
record TestPojo(
    String firstName,
    String lastName,
    Date birthday,
    String address,
    int age
) {

    static TestPojo generate() {
        var firstName = one(alphabeticStrings());
        var lastName = one(hexadecimalString(10));
        var birthday = one(pastDates());
        var address = one(strings(50));
        var age = one(smallPositiveIntegers());

        return new TestPojo(
            firstName,
            lastName,
            birthday,
            address,
            age
        );
    }
}
