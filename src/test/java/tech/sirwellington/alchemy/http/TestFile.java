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

import java.io.*;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
class TestFile
{

    private final static Logger LOG = LoggerFactory.getLogger(TestFile.class);

    TestFile() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }

    static File writeToTempFile(byte[] binary) throws IOException
    {

        checkThat(binary).is(notNull());

        String filename = one(alphabeticStrings(10));
        File tempFile = File.createTempFile(filename, ".txt");

        InputStream istream = new ByteArrayInputStream(binary);
        Files.copy(istream, tempFile.toPath(), REPLACE_EXISTING);
        LOG.debug("Wrote {} bytes to temp file at {}", binary.length, tempFile.getAbsolutePath());
        return tempFile;
    }
}
