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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.concurrency.Immutable;
import sir.wellington.alchemy.annotations.concurrency.ThreadSafe;
import sir.wellington.alchemy.annotations.patterns.FluidAPIPattern;
import static sir.wellington.alchemy.arguments.Arguments.checkThat;
import static sir.wellington.alchemy.arguments.assertions.Assertions.nonEmptyString;
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
    
    HttpOperation.Step1 at(URL url);
    
    default HttpOperation.Step1 at(String url) throws MalformedURLException
    {
        checkThat(url)
                .usingMessage("missing URL")
                .is(nonEmptyString());
        
        return at(new URL(url));
    }
    
    static void test()
    {
        Logger LOG = LoggerFactory.getLogger(AlchemyHttp.class);
        
        AlchemyHttp http = null;
        URL url = null;
        
        http.at(url)
                .then()
                .onSuccess(list -> LOG.debug(list.toString()))
                .onFailure(ex -> LOG.error(ex.toString()))
                .get();
        
        List list = http.at(url)
                .then()
                .get()
                .as(List.class);
        
        String response = http.at(url)
                .then()
                .post("this is my message")
                .asString();
        
        http.at(url)
                .usingHeader("header key", "value")
                .then()
                .onSuccess(r -> LOG.info(r.asString()))
                .onFailure(HttpOperation.OnFailure.NO_OP)
                .post();
    }
}
