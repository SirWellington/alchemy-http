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

import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.STEP

/**
 *
 * @author SirWellington
 */
@Internal
@StepMachineDesign(role = STEP)
internal class Step1Impl(private val stateMachine: AlchemyHttpStateMachine,
                         private val request: HttpRequest) : AlchemyRequest.Step1
{


    override fun get(): AlchemyRequest.Step3
    {
        val newRequest = HttpRequest.Builder
                                    .from(this.request)
                                    .usingVerb(HttpVerb.GET)
                                    .build()

        return stateMachine.jumpToStep3(newRequest)
    }

    override fun post(): AlchemyRequest.Step2
    {
        val newRequest = HttpRequest.Builder
                                    .from(this.request)
                                    .usingVerb(HttpVerb.POST)
                                    .build()

        return stateMachine.jumpToStep2(newRequest)
    }

    override fun put(): AlchemyRequest.Step2
    {
        val newRequest = HttpRequest.Builder
                                    .from(this.request)
                                    .usingVerb(HttpVerb.PUT)
                                    .build()

        return stateMachine.jumpToStep2(newRequest)
    }

    override fun delete(): AlchemyRequest.Step2
    {
        val newRequest = HttpRequest.Builder
                                    .from(this.request)
                                    .usingVerb(HttpVerb.DELETE)
                                    .build()

        return stateMachine.jumpToStep2(newRequest)
    }

    override fun toString(): String
    {
        return "Step1Impl{stateMachine=$stateMachine, request=$request}"
    }

}
