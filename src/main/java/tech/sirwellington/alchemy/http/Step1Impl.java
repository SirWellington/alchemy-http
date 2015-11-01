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

import tech.sirwellington.alchemy.http.verb.HttpVerb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class Step1Impl implements AlchemyRequest.Step1
{
    
    private final static Logger LOG = LoggerFactory.getLogger(Step1Impl.class);
    
    private final AlchemyHttpStateMachine stateMachine;
    private final HttpRequest request;
    
    Step1Impl(AlchemyHttpStateMachine stateMachine, HttpRequest request)
    {
        checkThat(stateMachine, request)
                .are(notNull());
        
        this.stateMachine = stateMachine;
        this.request = request;
    }
    
    @Override
    public AlchemyRequest.Step3 get()
    {
        HttpRequest newRequest = HttpRequest.Builder.from(this.request)
                .usingVerb(HttpVerb.get())
                .build();
        
        return stateMachine.jumpToStep3(newRequest);
    }
    
    @Override
    public AlchemyRequest.Step2 post()
    {
        HttpRequest newRequest = HttpRequest.Builder.from(this.request)
                .usingVerb(HttpVerb.post())
                .build();
        
        return stateMachine.jumpToStep2(newRequest);
    }
    
    @Override
    public AlchemyRequest.Step2 put()
    {
        HttpRequest newRequest = HttpRequest.Builder.from(this.request)
                .usingVerb(HttpVerb.put())
                .build();
        
        return stateMachine.jumpToStep2(newRequest);
    }
    
    @Override
    public AlchemyRequest.Step2 delete()
    {
        HttpRequest newRequest = HttpRequest.Builder.from(this.request)
                .usingVerb(HttpVerb.delete())
                .build();
        
        return stateMachine.jumpToStep2(newRequest);
    }
    
    @Override
    public String toString()
    {
        return "Step1Impl{" + "stateMachine=" + stateMachine + ", request=" + request + '}';
    }
    
}
