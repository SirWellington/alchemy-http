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

package tech.sirwellington.alchemy.http;

import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 * This {@link Executor} implementation runs commands in the
 * same thread they are called from.
 *
 * @author SirWellington
 */
@Internal
final class SynchronousExecutor implements Executor
{

    private static final Logger LOG = LoggerFactory.getLogger(SynchronousExecutor.class);

    static SynchronousExecutor newInstance()
    {
        return new SynchronousExecutor();
    }

    @Override
    public void execute(@NotNull Runnable command)
    {
        command.run();
    }
}
