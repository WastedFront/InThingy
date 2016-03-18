package zemris.fer.hr.inthingy.custom;

import java.util.Map;

/**
 * Utility class which contains some method that are used by multiple activities/services.
 */
public class MyUtils {
    /**
     * Method for creating message with given parameters.
     * Message format is:
     *
     * @param id
     *         thing id
     * @param source
     *         this address
     * @param destination
     *         destination address
     * @param encryption
     *         encryption which will be used in message
     * @param dataMap
     *         map containing data.
     * @return message in valid format
     */
    public static String createMessage(String id, String source, String destination, String encryption, Map<String, String> dataMap) {
        StringBuilder message = new StringBuilder();

        return message.toString();
    }
}
