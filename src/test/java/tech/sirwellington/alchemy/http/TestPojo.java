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
import java.util.Objects;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.DateGenerators.pastDates;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.*;

/**
 * This is an example POJO (Plain Old Java Object) to be used for testing purposes only.
 *
 * @author SirWellington
 */
class TestPojo
{

    public String firstName;
    public String lastName;
    public Date birthday;
    public String address;
    public int age;

    public TestPojo()
    {
    }

    public TestPojo(String firstName, String lastName, Date birthday, String address, int age)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.address = address;
        this.age = age;
    }

    static TestPojo generate()
    {
        String firstName = one(alphabeticStrings());
        String lastName = one(hexadecimalString(10));
        Date birthday = one(pastDates());
        String address = one(strings(50));
        int age = one(smallPositiveIntegers());

        return new TestPojo(firstName, lastName, birthday, address, age);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TestPojo other)) return false;
        return age == other.age
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(birthday, other.birthday)
                && Objects.equals(address, other.address);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(firstName, lastName, birthday, address, age);
    }

    @Override
    public String toString()
    {
        return "TestPojo(firstName=" + firstName
                + ", lastName=" + lastName
                + ", birthday=" + birthday
                + ", address=" + address
                + ", age=" + age + ")";
    }
}
