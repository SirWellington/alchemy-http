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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 * @author SirWellington
 */
@Internal
final class Strings
{

    private static final Logger LOG = LoggerFactory.getLogger(Strings.class);

    @Internal
    static String nullToEmpty(String string)
    {
        if (string == null)
        {
            return "";
        }
        else
        {
            return string;
        }
    }

    static boolean isNullOrEmpty(String string)
    {
        return string == null || string.isEmpty();
    }
}
