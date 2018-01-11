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
