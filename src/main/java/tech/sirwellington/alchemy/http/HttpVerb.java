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

import org.apache.http.client.HttpClient;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
@Internal
public interface HttpVerb
{

    HttpResponse execute(HttpClient apacheHttpClient, HttpRequest request) throws AlchemyHttpException;

    static HttpVerb get()
    {
        return HttpVerbImpl.using(AlchemyRequestMapper.GET);
    }

    static HttpVerb post()
    {
        return HttpVerbImpl.using(AlchemyRequestMapper.POST);
    }

    static HttpVerb put()
    {
        return HttpVerbImpl.using(AlchemyRequestMapper.PUT);
    }

    static HttpVerb delete()
    {
        return HttpVerbImpl.using(AlchemyRequestMapper.DELETE);
    }
}
