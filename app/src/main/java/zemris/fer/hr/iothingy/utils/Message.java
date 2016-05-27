package zemris.fer.hr.iothingy.utils;

import java.util.Locale;
import java.util.Random;

/**
 * Class which represents message. There are messages that are send to someone and which are received and they both have
 * the same format. Every message has its own id, source thing id, destination thing id and previous message id (if this id is
 * 0, it means there is no previous message). Also message contains sendMode, encryption, destination IP and destination port.
 */
public class Message {
    /** Message ID. */
    protected String messageID;
    /** Previous message ID . */
    protected String previousMessageID;
    /** Source thing ID. */
    protected String srcID;
    /** Destination thing ID. */
    protected String destID;
    /** Unparsed JSON data. */
    protected String jsonData;
    /** Destination IP. */
    protected String destIP;
    /** Destination port. */
    protected int destPort;
    /** Send mode. */
    protected String sendMode;
    /** Encryption. */
    protected String encryption;
    /** Generator for message ID. */
    private static final Random random = new Random();

    /**
     * Constructor. If message ID is null, it will be randomly created, if previous message id is null, it will be 00000000.
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
    public Message(String messageID, String srcID, String destID, String jsonData, String previousMessageID,
                   String sendMode, String encryption, String destIP, int destPort) {
        if (messageID != null) {
            this.messageID = messageID;
        } else {
            this.messageID = String.format(Locale.getDefault(), "%08d", random.nextInt(79999999) + 1);
        }
        this.srcID = srcID;
        this.destID = destID;
        this.jsonData = jsonData;
        if (previousMessageID != null) {
            this.previousMessageID = previousMessageID;
        } else {
            this.previousMessageID = "00000000";
        }
        this.sendMode = sendMode;
        this.encryption = encryption;
        this.destIP = destIP;
        this.destPort = destPort;
    }

    /**
     * Constructor. If message ID is null, it will be randomly created, if previous message id is null, it will be 00000000.
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
     * @param destinationFormat
     *         format of destination IP, port and ID. It needs to be in following format:--destIP:destPort destID--
     */
    public Message(String messageID, String srcID, String jsonData, String previousMessageID,
                   String sendMode, String encryption, String destinationFormat) {
        this(messageID, srcID, destinationFormat.split(" ")[1], jsonData, previousMessageID, sendMode, encryption,
                destinationFormat.split(" ")[0].split(":")[0],
                Integer.parseInt(destinationFormat.split(" ")[0].split(":")[1]));
    }


    /**
     * Method for getting message in format which will be used to send to some other device.
     * That message is in following format:
     * --encryptionType messageID srcID destID preMsgID jsonDATA--
     *
     * @return message string in proper format
     */
    public String getComSendMessage() {
        String sendMsg = encryptMessage();
        char encryptType = '0';
        switch (encryption.toUpperCase()) {
            case "FULL":
                encryptType = '1';
                break;
        }
        return encryptType + sendMsg;
    }

    /**
     * Method for encrypting message.
     * Encryption types are NONE, FULL, HMAC.
     *
     * @return encrypted message
     */
    private String encryptMessage() {
        switch (encryption.toUpperCase()) {
            case "NONE":
                return messageID + srcID + destID + previousMessageID + jsonData;
            default:
                return messageID + srcID + destID + previousMessageID + jsonData;
        }
    }

    /**
     * Getter for previous message ID.
     *
     * @return previous message ID
     */
    public String getPreviousMessageID() {
        return previousMessageID;
    }

    /**
     * Getter for message ID.
     *
     * @return message ID
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * Getter for source ID.
     *
     * @return source ID
     */
    public String getSrcID() {
        return srcID;
    }

    /**
     * Getter for destination ID.
     *
     * @return destination ID
     */
    public String getDestID() {
        return destID;
    }

    /**
     * Getter for JSON data.
     *
     * @return JSON data
     */
    public String getJsonData() {
        return jsonData;
    }

    /**
     * Getter for destination IP address.
     *
     * @return destination IP address
     */
    public String getDestIP() {
        return destIP;
    }

    /**
     * Getter for destination port.
     *
     * @return destination port
     */
    public int getDestPort() {
        return destPort;
    }

    /**
     * Getter for encryption.
     *
     * @return encryption
     */
    public String getEncryption() {
        return encryption;
    }

    /**
     * Getter for send mode.
     *
     * @return send mode
     */
    public String getSendMode() {
        return sendMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;

        if (destPort != message.destPort) {
            return false;
        }
        if (!messageID.equals(message.messageID)) {
            return false;
        }
        if (previousMessageID != null ? !previousMessageID.equals(message.previousMessageID) : message.previousMessageID != null) {
            return false;
        }
        if (!srcID.equals(message.srcID)) {
            return false;
        }
        if (!destID.equals(message.destID)) {
            return false;
        }
        if (!jsonData.equals(message.jsonData)) {
            return false;
        }
        if (!destIP.equals(message.destIP)) {
            return false;
        }
        if (!sendMode.equals(message.sendMode)) {
            return false;
        }
        return encryption.equals(message.encryption);

    }

    @Override
    public int hashCode() {
        int result = messageID.hashCode();
        result = 31 * result + (previousMessageID != null ? previousMessageID.hashCode() : 0);
        result = 31 * result + srcID.hashCode();
        result = 31 * result + destID.hashCode();
        result = 31 * result + jsonData.hashCode();
        result = 31 * result + destIP.hashCode();
        result = 31 * result + destPort;
        result = 31 * result + sendMode.hashCode();
        result = 31 * result + encryption.hashCode();
        return result;
    }
}