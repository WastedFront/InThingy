package zemris.fer.hr.inthingy.utils;

/**
 * Class which represents message. There are messages that are send to someone and which are received and they both have
 * the same format. Every message has its own id, source thing id, destination thing id and previous message id (if this id is
 * 0, it means there is no previous message). Also message contains sendMode, encryption, destination IP and destination port.
 *
 * @author Nikola Presečki
 * @version 1.0
 */
public class Message {
    /** Message ID. */
    private String messageID;
    /** Previous message ID . */
    private String previousMessageID;
    /** Source thing ID. */
    private String srcID;
    /** Destination thing ID. */
    private String destID;
    /** Unparsed JSON data. */
    private String jsonData;
    /** Destination IP. */
    private String destIP;
    /** Destination port. */
    private int destPort;
    /** Send mode. */
    private String sendMode;
    /** Encryption. */
    private String encryption;

    /**
     * Constructor.
     *
     * @param messageID
     *         Message ID as 64 bit number
     * @param srcID
     *         Source thing ID
     * @param destID
     *         Destination thing ID
     * @param jsonDATA
     *         Unparsed JSON data.
     * @param previousMessageID
     *         id of previous message (if exists, otherwise 0)
     * @param sendMode
     *         send mode (INTERNET, BLUETOOTH, WI-FI)
     * @param encryption
     *         encryption NONE, HMAC, FULL
     * @param destIP
     *         IP address of destination
     * @param destPort
     *         port of destination
     */
    public Message(String messageID, String srcID, String destID, String jsonDATA, String previousMessageID,
                   String sendMode, String encryption, String destIP, int destPort) {
        super();
        this.messageID = messageID;
        this.srcID = srcID;
        this.destID = destID;
        this.jsonData = jsonDATA;
        this.previousMessageID = previousMessageID;
        this.sendMode = sendMode;
        this.encryption = encryption;
        this.destIP = destIP;
        this.destPort = destPort;
    }

    /**
     * Method for parsing received message. Message is in following format: -
     * MSG_ID SRC_ID DEST_ID PREV_MSG_ID JSON_DATA - Every ID is 64 bit long.
     *
     * @param message
     *         properly formatted message.
     * @return newly created message.
     */
    public static Message parseReturnMessage(String message) {
//        String msgID = message.substring(0, 8);
//        String srcID = message.substring(8, 16);
//        String destID = message.substring(16, 24);
//        String pMsgID = message.substring(24, 32);
//        String jsonData = message.substring(32);
//        return new Message(msgID, srcID, destID, jsonData, pMsgID);
        return null;
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
}