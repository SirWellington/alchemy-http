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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;

/**
 *
 * @author SirWellington
 */
class TestFile
{

    private final static Logger LOG = LoggerFactory.getLogger(TestFile.class);

    static File writeToTempFile(byte[] binary) throws IOException
    {
        String filename = one(alphabeticString(10));
        File tempFile = File.createTempFile(filename, ".txt");
        Files.write(binary, tempFile);
        return tempFile;
    }
}
