/*
 * Copyright © 2019. Sir Wellington.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.sirwellington.alchemy.http

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign
import tech.sirwellington.alchemy.annotations.designs.StepMachineDesign.Role.MACHINE
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.positiveLong
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnFailure
import tech.sirwellington.alchemy.http.AlchemyRequestSteps.OnSuccess
import tech.sirwellington.alchemy.http.HttpAssertions.okResponse
import tech.sirwellington.alchemy.http.HttpAssertions.ready
import tech.sirwellington.alchemy.http.HttpAssertions.validResponseClass
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException
import java.util.concurrent.Executor

/**
 *
 * @author SirWellington
 */
@Internal
@StepMachineDesign(role = MACHINE)
internal class AlchemyMachineImpl @JvmOverloads constructor(private val async: Executor,
                                                            private val gson: Gson,
                                                            private val requestExecutor: HttpRequestExecutor,
                                                            private val timeoutMillis: Long = Constants.DEFAULT_TIMEOUT) : AlchemyHttpStateMachine
{
    init
    {
        checkThat(timeoutMillis).isA(positiveLong())
    }

    private val LOG = LoggerFactory.getLogger(AlchemyMachineImpl::class.java)

    @Throws(IllegalArgumentException::class)
    override fun begin(initialRequest: HttpRequest): AlchemyRequestSteps.Step1
    {
        val requestCopy = HttpRequest.copyOf(initialRequest)
        LOG.debug("Beginning HTTP request {}", requestCopy)

        return Step1Impl(this, requestCopy)
    }

    @Throws(IllegalArgumentException::class)
    override fun jumpToStep2(request: HttpRequest): AlchemyRequestSteps.Step2
    {
        val requestCopy = HttpRequest.copyOf(request)
        return Step2Impl(requestCopy, this, gson)
    }

    @Throws(IllegalArgumentException::class)
    override fun jumpToStep3(request: HttpRequest): AlchemyRequestSteps.Step3
    {
        val requestCopy = HttpRequest.copyOf(request)
        return Step3Impl(this, requestCopy)
    }

    @Throws(IllegalArgumentException::class)
    override fun <ResponseType> jumpToStep4(request: HttpRequest,
                                            classOfResponseType: Class<ResponseType>): AlchemyRequestSteps.Step4<ResponseType>
    {
        checkThat(classOfResponseType).isA(validResponseClass())

        val requestCopy = HttpRequest.copyOf(request)
        return Step4Impl(this, requestCopy, classOfResponseType)
    }

    @Throws(IllegalArgumentException::class)
    override fun <ResponseType> jumpToStep5(request: HttpRequest,
                                            classOfResponseType: Class<ResponseType>,
                                            successCallback: OnSuccess<ResponseType>): AlchemyRequestSteps.Step5<ResponseType>
    {
        checkThat(classOfResponseType).isA(validResponseClass())

        val requestCopy = HttpRequest.copyOf(request)
        return Step5Impl(this, requestCopy, classOfResponseType, successCallback)
    }

    override fun <ResponseType> jumpToStep6(request: HttpRequest,
                                            classOfResponseType: Class<ResponseType>,
                                            successCallback: OnSuccess<ResponseType>,
                                            failureCallback: OnFailure): AlchemyRequestSteps.Step6<ResponseType>
    {
        checkThat(classOfResponseType).isA(validResponseClass())

        val requestCopy = HttpRequest.copyOf(request)
        return Step6Impl(this, requestCopy, classOfResponseType, successCallback, failureCallback)
    }

    override fun <ResponseType> executeSync(request: HttpRequest, classOfResponseType: Class<ResponseType>): ResponseType
    {
        LOG.debug("Executing synchronous HTTP Request {}", request)

        checkThat(classOfResponseType).isA(validResponseClass())
        checkThat(request).isA(ready())

        val response = try
        {
            requestExecutor.execute(request, gson)
        }
        catch (ex: AlchemyHttpException)
        {
            throw ex
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to execute request {}", request, ex)
            throw AlchemyHttpException(request, ex)
        }

        checkThat(response)
                .throwing { ex -> AlchemyHttpException(request, response, "Http Response not OK.") }
                .isA(okResponse())

        LOG.trace("HTTP Request {} successfully executed: {}", request, response)

        return if (classOfResponseType == HttpResponse::class.java)
        {
            response as ResponseType
        }
        else if (classOfResponseType == String::class.java)
        {
            response.bodyAsString() as ResponseType
        }
        else
        {
            LOG.trace("Attempting to parse response {} as {}", response, classOfResponseType)
            response.bodyAs(classOfResponseType)
        }
    }

    override fun <ResponseType> executeAsync(request: HttpRequest,
                                             classOfResponseType: Class<ResponseType>,
                                             successCallback: OnSuccess<ResponseType>,
                                             failureCallback: OnFailure)
    {
        checkThat(request).isA(ready())
        checkThat(classOfResponseType).isA(validResponseClass())

        LOG.debug("Submitting Async HTTP Request {}", request)

        async.execute block@ {

            LOG.debug("Starting Async HTTP Request {}", request)

            val response = try
            {
                executeSync(request, classOfResponseType)
            }
            catch (ex: AlchemyHttpException)
            {
                LOG.trace("Async request failed", ex)
                failureCallback.handleError(ex)
                return@block
            }
            catch (ex: Exception)
            {
                LOG.trace("Async request failed", ex)
                failureCallback.handleError(AlchemyHttpException(ex))
                return@block
            }

            try
            {
                successCallback.processResponse(response)
            }
            catch (ex: Exception)
            {
                val message = "Success Callback threw exception"
                LOG.warn(message, ex)
                failureCallback.handleError(AlchemyHttpException(message, ex))
            }

        }

    }

    override fun toString(): String
    {
        return "AlchemyMachineImpl(async=$async, timeoutMillis=$timeoutMillis)"
    }

}
