/*
 * Copyright 2015 Sir Wellington.
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.http.client.HttpClient;
import sir.wellington.alchemy.http.operations.HttpOperation;
import sir.wellington.alchemy.http.operations.HttpVerb;

/**
 *
 * @author SirWellington
 */
interface OperationRunner<ResponseType> extends Callable<ResponseType>
{

    @Override
    public ResponseType call() throws Exception;

    
    class Sync<ResponseType> implements OperationRunner<ResponseType>
    {
        private HttpClient httpClient;
        private HttpVerb verb;
        Class<ResponseType> classOfResponseType;
        
        @Override
        public ResponseType call() throws Exception
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    class Async<ResponseType> implements OperationRunner<Void>
    {
        private HttpClient httpClient;
        private HttpVerb verb;
        private Class<ResponseType> classOfResponseType;
        private ExecutorService async;
        private HttpOperation.OnSuccess<ResponseType> successCallback;
        private HttpOperation.OnFailure failureCallback;
        
        @Override
        public Void call() throws Exception
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
