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

import sir.wellington.alchemy.collections.maps.Maps
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.STEP
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.greaterThanOrEqualTo
import tech.sirwellington.alchemy.arguments.assertions.nonEmptyString
import tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import java.net.URL

/**
 *
 * @author SirWellington
 */
@Internal
@StepMachineDesign(role = STEP)
internal class Step3Impl(private val stateMachine: AlchemyHttpStateMachine,
                         private var request: HttpRequest?) : AlchemyRequest.Step3
{

    @Throws(IllegalArgumentException::class)
    override fun usingHeader(key: String, value: String): AlchemyRequest.Step3
    {
        checkThat(key)
                .usingMessage("missing key")
                .isA(nonEmptyString())

        //Value of an HTTP Header can be empty ?
        val cleanValue = Strings.nullToEmpty(value)

        val newRequestHeaders = Maps.create<String, String>()

        //Keep existing headers
        request?.requestHeaders?.let {
            newRequestHeaders.putAll(it)
        }

        //Add the new header
        newRequestHeaders[key] = cleanValue

        this.request = HttpRequest.Builder
                                  .from(request)
                                  .usingRequestHeaders(newRequestHeaders)
                                  .build()

        return this
    }

    @Throws(IllegalArgumentException::class)
    override fun usingQueryParam(name: String, value: String): AlchemyRequest.Step3
    {
        checkThat(name, value)
                .usingMessage("missing name or value")
                .are(nonEmptyString())

        val queryParams = Maps.create<String, String>()

        //Keep existing Query Params
        request?.queryParams?.let { queryParams.putAll(it) }

        //Add the new query param
        queryParams[name] = value

        request = HttpRequest.Builder
                             .from(request)
                             .usingQueryParams(queryParams)
                             .build()

        return this
    }

    @Throws(IllegalArgumentException::class)
    override fun followRedirects(maxNumberOfTimes: Int): AlchemyRequest.Step3
    {
        checkThat(maxNumberOfTimes).isA(greaterThanOrEqualTo(1))

        //TODO: Implement this
        //Not doing anything with this yet.

        return this
    }

    @Throws(AlchemyHttpException::class)
    override fun at(url: URL): HttpResponse
    {
        //Ready to do a sync request
        val requestCopy = HttpRequest.Builder
                                     .from(request)
                                     .usingUrl(url)
                                     .build()

        //Tell the Step Machine to perform a Sync Request
        return stateMachine.executeSync(requestCopy)
    }

    override fun onSuccess(onSuccessCallback: AlchemyRequest.OnSuccess<HttpResponse>): AlchemyRequest.Step5<HttpResponse>
    {
        return stateMachine.jumpToStep5(request, HttpResponse::class.java, onSuccessCallback)
    }

    @Throws(IllegalArgumentException::class)
    override fun <ResponseType> expecting(classOfResponseType: Class<ResponseType>): AlchemyRequest.Step4<ResponseType>
    {
        checkThat(classOfResponseType).isA(validResponseClass())

        return stateMachine.jumpToStep4(request, classOfResponseType)
    }

    override fun toString(): String
    {
        return "Step3Impl{request=$request, stateMachine=$stateMachine}"
    }

}
