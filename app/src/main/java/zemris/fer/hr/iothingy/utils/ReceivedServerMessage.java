package zemris.fer.hr.iothingy.utils;

import android.content.Context;
import com.gdubina.multiprocesspreferences.MultiprocessPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Message that is received from some server.
 */
public class ReceivedServerMessage extends Message {

    /** Command of return message. */
    private String cmd;
    /** Command values. */
    private String cmdValue;

    /**
     * Constructor. If message ID is null, it will be randomly created, if previous message id is null, it will be
     * 00000000. It automatically parses jsonData as CMD and CMD_VALUE.
     *
     * @param messageID
     *         message ID
     * @param srcID
     *         source thing ID
     * @param jsonData
     *         unparsed JSON data.
     * @param previousMessageID
     *         id of previous message (if exists, otherwise null)
     * @param sendMode
     *         send mode (INTERNET, BLUETOOTH, WI-FI)
     * @param encryption
     *         encryption NONE, HMAC, FULL
     * @param destIP
     *         IP address of destination
     * @param destPort
     *         port of destination
     */
    public ReceivedServerMessage(String messageID, String srcID, String destID, String jsonData, String
            previousMessageID,
                                 String sendMode, String encryption, String destIP, int destPort) {
        super(messageID, srcID, destID, jsonData.toUpperCase(), previousMessageID, sendMode, encryption, destIP,
                destPort);
        try {
            JSONObject jsonObject = new JSONObject(jsonData.toUpperCase());
            cmd = jsonObject.getString("CMD");
            //handle get command
            if (cmd.equals("GET")) {
                StringBuilder values = new StringBuilder();
                JSONArray sensorArray = jsonObject.getJSONArray("SENSOR");
                for (int i = 0, len = sensorArray.length(); i < len - 1; ++i) {
                    values.append(sensorArray.getString(i)).append(' ');
                }
                values.append(sensorArray.getString(sensorArray.length() - 1));
                cmdValue = values.toString();
            }
        } catch (Exception e) {
            cmd = cmdValue = Constants.STRING_ERROR;
        }
    }

    /**
     * Method for creating response to this message. Currently it supports only command GET.
     *
     * @param context
     *         application context
     * @return respond message or null if command is not supported by this method.
     */
    public Message responseMessage(Context context) {
        if (!isGetCommand()) {
            return null;
        }
        String jsonResponse = handleGetCommand(context);
        return new Message(
                String.format(Locale.getDefault(), "%08d", Integer.parseInt(getMessageID()) + 1),
                getDestID(),
                getSrcID(),
                jsonResponse,
                getMessageID(),
                getSendMode(),
                getEncryption(),
                getDestIP(),
                getDestPort()
        );
    }

    /**
     * Method for parsing GET command values and making JSON data for response message.
     *
     * @param context
     *         application context
     * @return json data in string format
     */
    private String handleGetCommand(Context context) {
        String[] splits = cmdValue.split(" ");
        Map<String, String> sensorDataMap = new HashMap<>();
        for (int i = 0, len = splits.length; i < len; ++i) {
            String key = splits[i].trim();
            String value = MultiprocessPreferences.getDefaultSharedPreferences(context).getString(key, "");
            //if there is no such sensor, don't put its data
            if (!"".equals(value)) {
                sensorDataMap.put(key, value);
            }
        }
        return MyUtils.createJSONData(sensorDataMap);
    }

    /**
     * Method for parsing JSON data if this message is received from some other message.
     *
     * @return message in following format: COMMAND OTHER_DATA
     */
    public String returnMsgDataInfo() {
        return cmd + ":\n" + cmdValue;
    }

    public boolean isGetCommand() {
        return cmd.equals("GET");
    }

    /**
     * Method for getting string format of this object which can be saved to some file or something else. Every variable
     * is separated with delimiter saved in {@link Constants}. Variables are in following order: msgID, srcID, destID,
     * prevMsgID, jsonData, send mode, encryption, destIP, destPort.
     *
     * @return string format of message
     */
    public String storeMsgFormat() {
        String delm = Constants.MSG_DELIM;
        return messageID + delm + srcID + delm + destID + delm + previousMessageID + delm + jsonData + delm +
                sendMode + delm + encryption + delm + destIP + delm + destPort;
    }

    /**
     * Method for parsing string representation of this object. Given string must be in valid format. Variables are in
     * following order: msgID, srcID, destID, prevMsgID, jsonData, send mode, encryption, destIP, destPort.
     *
     * @param storeMsg
     *         string representation of message
     * @return {@code Message} format
     */
    public static ReceivedServerMessage parseStoreMsg(String storeMsg) {
        String[] splits = storeMsg.split(Constants.MSG_DELIM);
        if (splits.length != 9) {
            throw new IllegalArgumentException("Illegal message format");
        }
        return new ReceivedServerMessage(splits[0], splits[1], splits[2], splits[4], splits[3], splits[5], splits[6],
                splits[7],
                Integer.parseInt(splits[8]));
    }

    /**
     * Method for creating message summary which represents this message in user readable format.
     *
     * @return message summary
     */
    public String messageSummary() {
        String summary = "Message:\n";
        summary += "message id:           " + messageID;
        summary += "\nprevious message id:  " + previousMessageID;
        summary += "\nsource id:            " + srcID;
        summary += "\ndestination id:       " + destID;
        summary += "\nsend mode:            " + sendMode;
        summary += "\nencryption:           " + encryption;
        summary += "\ndata:\n" + cmd + " " + cmdValue;
        return summary;
    }

    /**
     * Method for parsing received message.
     *
     * @param params
     *         params in following order: received message, send mode, encryption, destination IP, destination port.
     * @return new message
     */
    public static ReceivedServerMessage parseReceivedMessage(String... params) {
        String rtnMsg = decryptReceivedMessage(params[0], params[2]);
        return new ReceivedServerMessage(
                rtnMsg.substring(1, 9),          //message ID
                rtnMsg.substring(9, 17),        //src ID
                rtnMsg.substring(17, 25),         //dest ID
                rtnMsg.substring(33),            //JSON data
                rtnMsg.substring(25, 33),        //previous message id
                params[1],                       //send mode
                params[2],                       //encryption
                params[3],                       //destination IP
                Integer.parseInt(params[4])      //destination port
        );
    }

    /**
     * Method for decrypting received message.
     *
     * @param rtnMsg
     *         received message
     * @param encryption
     *         encryption name (NONE, FULL, HMAC ...)
     * @return decrypted message
     */
    private static String decryptReceivedMessage(String rtnMsg, String encryption) {
        switch (encryption.toUpperCase()) {
            case "NONE":
                return rtnMsg;
            default:
                return rtnMsg;
        }
    }

}
