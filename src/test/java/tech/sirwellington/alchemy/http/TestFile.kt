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
package tech.sirwellington.alchemy.http

import org.slf4j.LoggerFactory
import tech.sirwellington.alchemy.annotations.access.Internal
import tech.sirwellington.alchemy.annotations.access.NonInstantiable
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.nonNullReference
import tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one
import tech.sirwellington.alchemy.generator.StringGenerators.Companion.alphabeticStrings
import java.io.File
import java.io.IOException

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
internal object TestFile
{

    private val LOG = LoggerFactory.getLogger(TestFile::class.java)

    @Throws(IOException::class)
    @JvmStatic
    fun writeToTempFile(binary: ByteArray): File
    {
        checkThat(binary).isA(nonNullReference())

        val filename = one(alphabeticStrings(10))
        val tempFile = File.createTempFile(filename, ".txt")

        tempFile.writeBytes(binary)
        LOG.debug("Wrote {} bytes to temp file at {}", binary.size, tempFile.absolutePath)

        return tempFile
    }
}
