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

import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.DateGenerators.Companion.pastDates
import tech.sirwellington.alchemy.generator.NumberGenerators.Companion.smallPositiveIntegers
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.hexadecimalString
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.strings
import java.util.Date

/**
 * This is an example POJO (Plain Old Java Object) to be used for testing purposes only.
 *
 * @author SirWellington
 */
@Internal
data class TestPojo(var firstName: String,
                    var lastName: String,
                    var birthday: Date,
                    var address: String,
                    var age: Int = 0)
{

    companion object
    {

        fun generate(): TestPojo
        {
            val firstName = one(alphabeticStrings())
            val lastName = one(hexadecimalString(10))
            val birthday = one(pastDates())
            val address = one(strings(50))
            val age = one(smallPositiveIntegers())

            return TestPojo(firstName, lastName, birthday, address, age)
        }
    }

}
