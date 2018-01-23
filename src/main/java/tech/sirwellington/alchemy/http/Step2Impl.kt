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

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.STEP
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonEmptyString
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import tech.sirwellington.alchemy.http.exceptions.JsonException

/**
 *
 * @author SirWellington
 */
@Internal
@StepMachineDesign(role = STEP)
internal class Step2Impl(private val request: HttpRequest,
                         private val stateMachine: AlchemyHttpStateMachine,
                         private val gson: Gson) : AlchemyRequestSteps.Step2
{


    override fun nothing(): AlchemyRequestSteps.Step3
    {
        val newRequest = HttpRequest.Builder
                                    .from(request)
                                    .usingBody(JsonNull.INSTANCE)
                                    .build()

        return stateMachine.jumpToStep3(newRequest)
    }

    @Throws(IllegalArgumentException::class)
    override fun body(jsonString: String): AlchemyRequestSteps.Step3
    {
        checkThat(jsonString)
                .usingMessage("use 'nothing()' for empty body")
                .isA(nonEmptyString())

        val jsonBody = try
        {
            gson.fromJson(jsonString, JsonElement::class.java)
        }
        catch (ex: Exception)
        {
            throw JsonException("Failed to parse JSON Body: " + jsonString, ex)
        }

        val newRequest = HttpRequest.Builder
                                    .from(request)
                                    .usingBody(jsonBody)
                                    .build()

        return stateMachine.jumpToStep3(newRequest)
    }

    @Throws(IllegalArgumentException::class)
    override fun body(pojo: Any): AlchemyRequestSteps.Step3
    {

        val jsonBody = try
        {
            gson.toJsonTree(pojo)
        }
        catch (ex: Exception)
        {
            LOG.error("Could not convert {} to JSON", pojo, ex)
            throw AlchemyHttpException("Could not convert to JSON", ex)
        }

        val newRequest = HttpRequest.Builder
                                    .from(request)
                                    .usingBody(jsonBody)
                                    .build()

        return stateMachine.jumpToStep3(newRequest)
    }

    override fun toString(): String
    {
        return "Step2Impl{request=$request, stateMachine=$stateMachine}"
    }

    companion object
    {

        private val LOG = LoggerFactory.getLogger(Step2Impl::class.java)
    }

}
