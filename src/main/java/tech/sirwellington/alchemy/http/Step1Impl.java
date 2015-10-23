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
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.exceptions.JsonException;

/**
 *
 * @author SirWellington
 */
class Step1Impl implements AlchemyRequest.Step1
{

    private final static Logger LOG = LoggerFactory.getLogger(Step1Impl.class);

    private final AlchemyHttpStateMachine stateMachine;
    private final JsonParser jsonParser;
    private final Gson gson = new Gson();
    private final HttpRequest request;

    private JsonElement body;

    Step1Impl(AlchemyHttpStateMachine stateMachine,
              HttpRequest request,
              JsonParser jsonParser)
    {
        checkThat(stateMachine).is(notNull());
        checkThat(request).is(notNull());
        checkThat(jsonParser).is(notNull());

        this.stateMachine = stateMachine;
        this.request = request;
        this.jsonParser = jsonParser;
    }

    @Override
    public AlchemyRequest.Step1 body(String jsonBody) throws IllegalArgumentException
    {
        if (Strings.isNullOrEmpty(jsonBody))
        {
            this.body = JsonNull.INSTANCE;
        }
        else
        {
            try
            {
                this.body = gson.toJsonTree(jsonBody);
            }
            catch (Exception ex)
            {
                throw new JsonException("Failed to parse JSON Body: " + jsonBody, ex);
            }
        }
        return this;
    }

    @Override
    public AlchemyRequest.Step1 body(Object body) throws IllegalArgumentException
    {
        if (body == null)
        {
            this.body = JsonNull.INSTANCE;
        }
        else
        {
            try
            {
                this.body = gson.toJsonTree(body);
            }
            catch (Exception ex)
            {
                LOG.error("Could not convert {} to JSON", body, ex);
                throw new AlchemyHttpException("Could not convert to JSON", ex);
            }
        }
        return this;
    }

    @Override
    public AlchemyRequest.Step2 get() throws AlchemyHttpException
    {
        return customVerb(HttpVerb.get());
    }

    @Override
    public AlchemyRequest.Step2 post() throws AlchemyHttpException
    {
        return customVerb(HttpVerb.post());
    }

    @Override
    public AlchemyRequest.Step2 put() throws AlchemyHttpException
    {
        return customVerb(HttpVerb.put());
    }

    @Override
    public AlchemyRequest.Step2 delete() throws AlchemyHttpException
    {
        return customVerb(HttpVerb.delete());
    }

    @Override
    public AlchemyRequest.Step2 customVerb(HttpVerb verb) throws AlchemyHttpException
    {
        checkThat(verb).is(notNull());

        HttpRequest newRequest = HttpRequest.Builder.from(this.request)
                .usingBody(body)
                .usingVerb(verb)
                .build();

        return stateMachine.getStep2(newRequest);
    }

}
