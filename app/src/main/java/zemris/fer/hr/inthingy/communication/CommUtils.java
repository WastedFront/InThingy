package zemris.fer.hr.inthingy.communication;

import android.content.Context;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Random;

import zemris.fer.hr.inthingy.utils.MyUtils;
import zemris.fer.hr.inthingy.utils.StoringUtils;

/**
 * Class providing method for communication with server (sending message, etc).
 */
public class CommUtils {


    /** Generator for message ID. */
    private static final Random random = new Random();

    /**
     * Method for creating and sending message to some cloud (server), or to some other thing.
     *
     * @param deviceId
     *         source id
     * @param encryption
     *         type of encryption (NONE, HMAC, FULL, DATA).
     * @param sendMode
     *         mode which tells how message will be send (Internet, Bluetooth, Wi-Fi).
     * @param destinationFormat
     *         properly formatted string for destination in following format:
     *         "DESTINATION_IP:DESTINATION_PORT DESTINATION_NAME".
     * @param sensorDataMap
     *         map containing data about sensor's and their values
     * @param context
     *         context of some activity
     * @return true if everything is ok (that doesn't mean that message is sent), otherwise false
     */
    public static boolean sendMessage(String deviceId, String encryption, String sendMode, String destinationFormat,
                                      Map<String, String> sensorDataMap, Context context) {
        String[] splitDestinationFormat = destinationFormat.split(" ");
        String[] portIP = splitDestinationFormat[0].split(":");
        String destinationIP = portIP[0];
        String destinationPort = portIP[1];
        String destinationID = splitDestinationFormat[1];
        byte[] message = createMessage(deviceId, encryption, sensorDataMap, destinationID, null);
        if (message == null) {
            return false;
        }
        new CommunicationTask(destinationIP, destinationPort, message, context, sendMode, true);
        StoringUtils.addDestinationAddress(context, destinationFormat);
        return true;
    }

    /**
     * Method for creating message. It needs to get multiple parameters. Message consists of header and data.
     * Header consists of message_id, source_id, destination_id, previous_message_id (all parameters are 64 bit long).
     * It uses data from {#parameter sensorDataMap} and it parses it in JSON format.
     * Also it encrypt message if needed.
     *
     * @param deviceId
     *         source id
     * @param encryption
     *         type of encryption (NONE, HMAC, FULL, DATA).
     * @param sensorDataMap
     *         map containing data about sensor's and their values
     * @param destinationID
     *         destination id
     * @param messageID
     *         ID of the message, if it is null it means it is first message sent to server
     * @return properly formatted message as byte array.
     */
    public static byte[] createMessage(String deviceId, String encryption, Map<String, String> sensorDataMap,
                                       String destinationID, String messageID) {
        byte[] header = createHeader(deviceId, destinationID, messageID);
        JSONObject jsonData = new JSONObject();
        for (Map.Entry<String, String> entry : sensorDataMap.entrySet()) {
            try {
                String value = entry.getValue();
                String key = entry.getKey().toUpperCase();
                JSONObject sensorObject = MyUtils.parseSensorValueToJSON(value);
                jsonData.put(key, sensorObject);
            } catch (Exception e) {
                return null;
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(header);
            outputStream.write(jsonData.toString().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            return null;
        }
        return encryptMessage(outputStream.toByteArray(), encryption);
    }


    /**
     * Method for creating header. Header is in following format: message_id, source_id, destination_id previous_message_id.
     * Message id is random 64bit number, also source and destination id are 64bit strings.
     *
     * @param deviceId
     *         current device id which will be source id
     * @param destinationID
     *         id of destination device
     * @param messageID
     *         message ID, it can be null and if it is it will be created
     * @return message header as byte array
     */
    private static byte[] createHeader(String deviceId, String destinationID, String messageID) {
        long num, prevNum;
        if (messageID == null) {
            prevNum = 0;
            num = random.nextInt(79999999) + 1; //there can't be null
        } else {
            prevNum = Long.valueOf(messageID);
            num = prevNum + 1;
        }
        String header = String.format("%08d", num) + deviceId + destinationID + String.format("%08d", prevNum);
        return header.getBytes(Charset.defaultCharset());
    }


    /**
     * Method for encrypting message.
     *
     * @param message
     *         message as byte array
     * @param encryption
     *         type of the encryption (NONE, HMAC, FULL, DATA)
     * @return encrypted message as byte array
     */
    private static byte[] encryptMessage(byte[] message, String encryption) {
        switch (encryption) {
            case "NONE":
                return message;
            default:
                return message;
        }
    }

}
