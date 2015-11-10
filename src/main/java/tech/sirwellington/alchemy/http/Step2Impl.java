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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

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
        HttpRequest newRequest = HttpRequest.Builder.from(request)
                .usingBody(JsonNull.INSTANCE)
                .build();

        return stateMachine.jumpToStep3(newRequest);
    }

    @Override
    public AlchemyRequest.Step3 body(String jsonString) throws IllegalArgumentException
    {
        checkThat(jsonString)
                .usingMessage("use 'nothing()' for empty body")
                .is(nonEmptyString());

        JsonElement jsonBody;

        try
        {
            jsonBody = gson.fromJson(jsonString, JsonElement.class);
        }
        catch (Exception ex)
        {
            throw new JsonException("Failed to parse JSON Body: " + jsonString, ex);
        }

        HttpRequest newRequest = HttpRequest.Builder.from(request)
                .usingBody(jsonBody)
                .build();

        return stateMachine.jumpToStep3(newRequest);
    }

    @Override
    public AlchemyRequest.Step3 body(Object pojo) throws IllegalArgumentException
    {
        checkThat(pojo)
                .usingMessage("use 'nothing() for empty body")
                .is(notNull());

        JsonElement jsonBody;

        try
        {
            jsonBody = gson.toJsonTree(pojo);
        }
        catch (Exception ex)
        {
            LOG.error("Could not convert {} to JSON", pojo, ex);
            throw new AlchemyHttpException("Could not convert to JSON", ex);
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
