/*
 * Copyright © 2026. Sir Wellington.
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
package tech.sirwellington.alchemy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.BasicAssertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 * @author SirWellington
 */
final class TestFile
{

    private static final Logger LOG = LoggerFactory.getLogger(TestFile.class);

    private TestFile()
    {
        throw new AssertionError("non-instantiable");
    }

    static File writeToTempFile(byte[] binary) throws IOException
    {
        checkThat(binary).isA(notNull());

        String filename = one(alphabeticStrings(10));
        File tempFile = File.createTempFile(filename, ".txt");

        try (FileOutputStream fos = new FileOutputStream(tempFile))
        {
            fos.write(binary);
        }

        LOG.debug("Wrote {} bytes to temp file at {}", binary.length, tempFile.getAbsolutePath());

        return tempFile;
    }
}
