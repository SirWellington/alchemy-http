/*
 * Copyright 2015 Wellington.
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
package sir.wellington.alchemy.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.concurrency.Immutable;
import sir.wellington.alchemy.annotations.concurrency.ThreadSafe;
import sir.wellington.alchemy.annotations.patterns.FluidAPIPattern;
import sir.wellington.alchemy.http.operations.HttpOperation;

/**
 *
 * @author SirWellington
 */
@Immutable
@ThreadSafe
@FluidAPIPattern
public interface AlchemyHttp
{

    AlchemyHttp setDefaultHeader(String key, String value);

    HttpOperation.Step1 begin();

    public static void main(String[] args) throws MalformedURLException
    {
        Logger LOG = LoggerFactory.getLogger(AlchemyHttp.class);

        AlchemyHttp http = null;
        URL url = new URL("google.com");

        http.begin()
                .body("this is my message")
                .post()
                .onSuccess(r -> LOG.info(r.asString()))
                .onFailure(HttpOperation.OnFailure.NO_OP)
                .at(url);

        List list = http.begin()
                .get()
                .expecting(List.class)
                .at(url);

        String response = http.begin()
                .body("this is my message")
                .post()
                .expecting(String.class)
                .at(url);

        http.begin()
                .post()
                .usingHeader("header key", "value")
                .usingHeader("another key", "val")
                .expecting(Map.class)
                .onSuccess(m -> LOG.info(m.toString()))
                .onFailure(HttpOperation.OnFailure.NO_OP)
                .at(url);
    }
}
