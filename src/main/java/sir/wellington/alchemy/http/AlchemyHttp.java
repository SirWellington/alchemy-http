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

import java.net.URL;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.annotations.patterns.FluidAPIPattern;
import sir.wellington.alchemy.http.operations.HttpOperation;

/**
 *
 * @author SirWellington
 */
@FluidAPIPattern
public interface AlchemyHttp
{
    
    AlchemyHttp setDefaultHeader(String key, String value);
    
    HttpOperation<HttpResponse> at(URL url);
    
    static void test()
    {
        Logger LOG = LoggerFactory.getLogger(AlchemyHttp.class);
        
        AlchemyHttp http = null;
        URL url = null;
        
        http.at(url)
                .expecting(List.class)
                .onSuccess(list -> LOG.info(list.toString()))
                .get();
        
        List list = http.at(url)
                .expecting(List.class)
                .get();
        
        String response = http.at(url)
                .expecting(String.class)
                .post("this is my message");
    }
}
