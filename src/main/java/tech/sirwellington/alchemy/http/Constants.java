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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

final class Constants
{
    private Constants()
    {
        throw new AssertionError("non-instantiable");
    }

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    static final Map<String, String> DEFAULT_HEADERS = Map.of(
        "Accept", ContentTypes.APPLICATION_JSON + ", " + ContentTypes.PLAIN_TEXT,
        "User-Agent", "Alchemy HTTP",
        "Content-Type", ContentTypes.APPLICATION_JSON + "; charset=UTF-8"
    );

    static final Gson DEFAULT_GSON = new GsonBuilder()
        .setDateFormat(DATE_FORMAT)
        .create();

    static final RequestMethod DEFAULT_REQUEST_METHOD = RequestMethod.GET;

    static final long DEFAULT_TIMEOUT = TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS);
}
