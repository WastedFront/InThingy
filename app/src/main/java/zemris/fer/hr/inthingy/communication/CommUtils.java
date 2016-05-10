package zemris.fer.hr.inthingy.communication;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Random;

import zemris.fer.hr.inthingy.R;
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
     */
    public static void sendMessage(String deviceId, String encryption, String sendMode, String destinationFormat,
                                   Map<String, String> sensorDataMap, Context context) {
        String[] splitDestinationFormat = destinationFormat.split(" ");
        String[] portIP = splitDestinationFormat[0].split(":");
        String destinationIP = portIP[0];
        String destinationPort = portIP[1];
        String destinationID = splitDestinationFormat[1];
        byte[] message = createMessage(deviceId, encryption, sensorDataMap, destinationID, context);
        if (message == null) {
            return;
        }
        new CommunicationTask(destinationIP, destinationPort, message, context, sendMode);
        StoringUtils.addDestinationAddress(context, destinationFormat);
    }

    /**
     * Method for creating message. It needs to get multiple parameters. Message consists of header and data.
     * Header consists of message_id, source_id, destination_id (all parameters are 64 bit long).
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
     * @param context
     *         context of some activity
     * @return properly formatted message as byte array.
     */
    private static byte[] createMessage(String deviceId, String encryption, Map<String, String> sensorDataMap,
                                        String destinationID, Context context) {
        byte[] header = createHeader(deviceId, destinationID);
        JSONObject jsonData = new JSONObject();
        for (Map.Entry<String, String> entry : sensorDataMap.entrySet()) {
            try {
                String value = entry.getValue();
                String key = entry.getKey().toUpperCase();
                String[] lines = value.split("\n");
                JSONArray valueArray = new JSONArray();
                JSONArray nameArray = new JSONArray();
                for (String line : lines) {
                    String[] lineSplit = line.split(": ");
                    String valueName = lineSplit[0].trim().toUpperCase();
                    String valueNumber = lineSplit[1].substring(0, lineSplit[1].indexOf(' ') - 1);
                    valueArray.put(Float.valueOf(valueNumber));
                    nameArray.put(valueName);
                }
                JSONObject sensorObject = new JSONObject();
                sensorObject.put("VALUES", valueArray);
                sensorObject.put("NAMES", nameArray);
                jsonData.put(key, sensorObject);
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getText(R.string.error), Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(header);
            outputStream.write(jsonData.toString().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            Toast.makeText(context, context.getResources().getText(R.string.error), Toast.LENGTH_SHORT).show();
            return null;
        }
        return encryptMessage(outputStream.toByteArray(), encryption);
    }


    /**
     * Method for creating header. Header is in following format: message_id, source_id, destination_id.
     * Message id is random 64bit number, also source and destination id are 64bit strings.
     *
     * @param deviceId
     *         current device id which will be source id
     * @param destinationID
     *         id of destination device
     * @return message header as byte array
     */
    private static byte[] createHeader(String deviceId, String destinationID) {
        String header = String.format("%08d", random.nextInt(99999999)) + deviceId + destinationID;
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
