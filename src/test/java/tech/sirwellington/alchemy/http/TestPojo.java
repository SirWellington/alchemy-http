/*
 * Copyright 2015 SirWellington Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.DateGenerators.pastDates;
import static tech.sirwellington.alchemy.generator.NumberGenerators.smallPositiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.generator.StringGenerators.strings;

/**
 * This is an example POJO (Plain Old Java Object) to be used for testing purposes only.
 *
 * @author SirWellington
 */
@Internal
class TestPojo
{

    //deliberate packag access, for testing purporses
    String firstName;
    String lastName;
    Date birthday;
    String address;
    int age;

    TestPojo()
    {
    }

    static TestPojo generate()
    {
        TestPojo pojo = new TestPojo();

        pojo.firstName = one(alphabeticString());
        pojo.lastName = one(hexadecimalString(10));
        pojo.birthday = one(pastDates());
        pojo.address = one(strings(50));
        pojo.age = one(smallPositiveIntegers());

        return pojo;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.firstName);
        hash = 19 * hash + Objects.hashCode(this.lastName);
        hash = 19 * hash + Objects.hashCode(this.birthday);
        hash = 19 * hash + Objects.hashCode(this.address);
        hash = 19 * hash + this.age;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final TestPojo other = (TestPojo) obj;
        if (!Objects.equals(this.firstName, other.firstName))
        {
            return false;
        }
        if (!Objects.equals(this.lastName, other.lastName))
        {
            return false;
        }
        if (!Objects.equals(this.birthday, other.birthday))
        {
            return false;
        }
        if (!Objects.equals(this.address, other.address))
        {
            return false;
        }
        if (this.age != other.age)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Pojo{" + "firstName=" + firstName + ", lastName=" + lastName + ", birthday=" + birthday + ", address=" + address + ", age=" + age + '}';
    }

}
