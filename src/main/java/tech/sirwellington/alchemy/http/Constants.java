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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 *
 * @author SirWellington
 */
@NonInstantiable
@Internal
final class Constants
{

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Internal
    final static Map<String, String> DEFAULT_HEADERS = ImmutableMap.<String, String>builder()
            .put("Accept", "application/json, text/plain")
            .put("User-Agent", "Alchemy HTTP")
            .put("Content-Type", "application/json")
            .build();
    
    static Gson getDefaultGson()
    {
        return new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .create();
    }
}
