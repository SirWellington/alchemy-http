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
import java.util.Map;
import sir.wellington.alchemy.http.exceptions.HttpException;
import sir.wellington.alchemy.http.operations.GetOperation;
import sir.wellington.alchemy.http.operations.PostOperation;


/**
 *
 * @author SirWellington
 */
public interface AlchemyHttp 
{
    AlchemyHttp setDefaultHeader(String key, String value);
    
    GetOperation<HttpResponse> get();
    
    PostOperation<HttpResponse> post();
    
    static void test()
    {
        AlchemyHttp http = null;
        URL url = null;
        
        byte[] download = http.get()
                .downloadBinary()
                .at(url);
        
        http.get()
                .onSuccess(r -> r.asString())
                .onFailure(null)
                .at(url);
        
        HttpException at = http.get()
                .expecting(HttpException.class)
                .at(url);
        
        Map perfect = http.get()
                .usingHeader(null, null)
                .usingHeader("someKey", "val")
                .expecting(Map.class)
                .at(url);
        
        http.post()
                .body("some body")
                .expecting(String.class)
                .at(url);
    }
}
