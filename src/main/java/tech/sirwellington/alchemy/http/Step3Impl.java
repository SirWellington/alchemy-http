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
package tech.sirwellington.alchemy.http;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.Assertions.notNull;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

/**
 *
 * @author SirWellington
 */
class Step3Impl<ResponseType> implements AlchemyRequest.Step3<ResponseType>
{

    private final static Logger LOG = LoggerFactory.getLogger(Step3Impl.class);

    private final AlchemyHttpStateMachine stateMachine;
    private final HttpRequest request;
    private final Class<ResponseType> classOfResponseType;

    public Step3Impl(AlchemyHttpStateMachine stateMachine, HttpRequest request, Class<ResponseType> classOfResponseType)
    {
        checkThat(stateMachine).is(notNull());
        checkThat(request).is(notNull());
        checkThat(classOfResponseType).is(notNull());

        this.stateMachine = stateMachine;
        this.request = request;
        this.classOfResponseType = classOfResponseType;
    }

    @Override
    public ResponseType at(URL url) throws AlchemyHttpException
    {
        checkThat(url)
                .usingMessage("missing url")
                .is(notNull());

        HttpRequest requestCopy = HttpRequest.Builder.from(request)
                .usingUrl(url)
                .build();

        return stateMachine.executeSync(requestCopy, classOfResponseType);
    }

    @Override
    public AlchemyRequest.Step4<ResponseType> onSuccess(AlchemyRequest.OnSuccess<ResponseType> onSuccessCallback)
    {
        checkThat(onSuccessCallback)
                .usingMessage("callback cannot be null")
                .is(notNull());

        return stateMachine.getStep4(request, classOfResponseType, onSuccessCallback);
    }

}
