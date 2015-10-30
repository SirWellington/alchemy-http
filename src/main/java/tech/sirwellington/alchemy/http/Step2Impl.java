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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class Step2Impl implements AlchemyRequest.Step2
{
    
    private final static Logger LOG = LoggerFactory.getLogger(Step2Impl.class);
    
    private final HttpRequest request;
    private final AlchemyHttpStateMachine stateMachine;
    private final Gson gson;
    
    Step2Impl(HttpRequest request, AlchemyHttpStateMachine stateMachine, Gson gson)
    {
        checkThat(request, stateMachine, gson)
                .are(notNull());
        
        this.request = request;
        this.stateMachine = stateMachine;
        this.gson = gson;
    }
    
    @Override
    public AlchemyRequest.Step3 nothing()
    {
        return stateMachine.jumpToStep3(request);
    }
    
    @Override
    public AlchemyRequest.Step3 body(String jsonBody) throws IllegalArgumentException
    {
        JsonElement body;
        
        if (Strings.isNullOrEmpty(jsonBody))
        {
            body = JsonNull.INSTANCE;
        }
        else
        {
            try
            {
                body = gson.toJsonTree(jsonBody);
            }
            catch (Exception ex)
            {
                throw new JsonException("Failed to parse JSON Body: " + jsonBody, ex);
            }
        }
        
        HttpRequest newRequest = HttpRequest.Builder.from(request)
                .usingBody(body)
                .build();
        
        return stateMachine.jumpToStep3(newRequest);
    }
    
    @Override
    public AlchemyRequest.Step3 body(Object body) throws IllegalArgumentException
    {
        JsonElement jsonBody;
        
        if (body == null)
        {
            jsonBody = JsonNull.INSTANCE;
        }
        else
        {
            try
            {
                jsonBody = gson.toJsonTree(body);
            }
            catch (Exception ex)
            {
                LOG.error("Could not convert {} to JSON", body, ex);
                throw new AlchemyHttpException("Could not convert to JSON", ex);
            }
        }
        
        HttpRequest newRequest = HttpRequest.Builder.from(request)
                .usingBody(jsonBody)
                .build();
        
        return stateMachine.jumpToStep3(newRequest);
    }
    
    @Override
    public String toString()
    {
        return "Step2Impl{" + "request=" + request + ", stateMachine=" + stateMachine + '}';
    }
    
}
