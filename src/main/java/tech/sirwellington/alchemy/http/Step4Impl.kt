/*
 * Copyright © 2018. Sir Wellington.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http

import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.STEP
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@StepMachineDesign(role = STEP)
internal class Step4Impl<ResponseType>(private val stateMachine: AlchemyHttpStateMachine, private val request: HttpRequest, private val classOfResponseType: Class<ResponseType>) : AlchemyRequest.Step4<ResponseType>
{

    init
    {
        checkThat(classOfResponseType).isA(validResponseClass())
    }

    @Throws(AlchemyHttpException::class)
    override fun at(url: URL): ResponseType
    {
        val newRequest = HttpRequest.Builder
                                    .from(request)
                                    .usingUrl(url)
                                    .build()

        return stateMachine.executeSync(newRequest, classOfResponseType)
    }

    override fun onSuccess(onSuccessCallback: AlchemyRequest.OnSuccess<ResponseType>): AlchemyRequest.Step5<ResponseType>
    {
        return stateMachine.jumpToStep5(request, classOfResponseType, onSuccessCallback)
    }

    override fun toString(): String
    {
        return "Step4Impl{stateMachine=$stateMachine, request=$request, classOfResponseType=$classOfResponseType}"
    }

}
