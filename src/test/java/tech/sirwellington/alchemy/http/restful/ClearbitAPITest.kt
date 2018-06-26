/*
 * Copyright © 2018. Sir Wellington.
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

package tech.sirwellington.alchemy.http.restful

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest
import tech.sirwellington.alchemy.arguments.assertions.validURL
import tech.sirwellington.alchemy.arguments.checkThat
import tech.sirwellington.alchemy.http.AlchemyHttp
import tech.sirwellington.alchemy.http.restful.Clearbit.AutocompleteResponse
import tech.sirwellington.alchemy.http.restful.Clearbit.Endpoints
import tech.sirwellington.alchemy.test.hamcrest.nonEmptyString
import tech.sirwellington.alchemy.test.hamcrest.notEmpty
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import javax.swing.ImageIcon
import kotlin.test.assertFalse


private object Clearbit
{
    object Endpoints
    {

        const val AUTOCOMPLETE = "https://autocomplete.clearbit.com/v1/companies/suggest"
        const val LOGO = "https://logo.clearbit.com"
    }

    data class AutocompleteResponse(val name: String,
                                    val domain: String,
                                    val logo: String)


}

@RunWith(AlchemyTestRunner::class)
@IntegrationTest
class ClearbitAPITest
{

    private val LOG = LoggerFactory.getLogger(this::class.java)

    private val http = AlchemyHttp.newBuilder().build()

    @Test
    fun testGetGoogleLogo()
    {
        val url = "${Endpoints.LOGO}/google.com"

        val response = http.go().download(url)
        testDownloadedLogo(response)
    }

    @Test
    fun testGetAmazonLogo()
    {
        val url = "${Endpoints.LOGO}/amazon.com"

        val response = http.go().download(url)
        testDownloadedLogo(response)
    }

    @Test
    fun testGithubLogo()
    {
        val url = "${Endpoints.LOGO}/github.com"

        val response = http.go().download(url)
        testDownloadedLogo(response)
    }

    @Test
    fun testAutocomplete()
    {
        testAutocompleteWithText("Am")
        testAutocompleteWithText("Cen")
        testAutocompleteWithText("Goo")
        testAutocompleteWithText("Ver")
    }


    private fun testDownloadedLogo(response: ByteArray)
    {
        assertThat(response, notNull)
        assertFalse { response.isEmpty() }

        val image = ImageIcon(response)
        LOG.info("Downloaded logo: [${image.description}, ${image.iconWidth}x${image.iconHeight}]")
    }


    private fun testAutocompleteWithText(text: String)
    {
        val url = Endpoints.AUTOCOMPLETE

        val response = http.go()
                           .get()
                           .usingQueryParam("query", text)
                           .expecting(Array<AutocompleteResponse>::class.java)
                           .at(url)
                           .toList()

        assertThat(response, notNull)
        assertThat(response, notEmpty)

        response.forEach {
            assertThat(it.name, nonEmptyString)
            assertThat(it.domain, nonEmptyString)
            assertThat(it.logo, nonEmptyString)

            checkThat(it.logo).isA(validURL())
        }
    }


}