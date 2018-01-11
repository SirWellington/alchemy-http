package tech.sirwellington.alchemy.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 * @author SirWellington
 */
@Internal
final class Strings
{

    private static final Logger LOG = LoggerFactory.getLogger(Strings.class);

    @Internal
    static String nullToEmpty(String string)
    {
        if (string == null)
        {
            return "";
        }
        else
        {
            return string;
        }
    }

    static boolean isNullOrEmpty(String string)
    {
        return string == null || string.isEmpty();
    }
}
