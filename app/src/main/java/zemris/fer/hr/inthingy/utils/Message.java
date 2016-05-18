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
     */
    public Message(String messageID, String srcID, String destID, String jsonDATA, String previousMessageID) {
        super();
        this.messageID = messageID;
        this.srcID = srcID;
        this.destID = destID;
        this.jsonData = jsonDATA;
        this.previousMessageID = previousMessageID;
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
        String msgID = message.substring(0, 8);
        String srcID = message.substring(8, 16);
        String destID = message.substring(16, 24);
        String pMsgID = message.substring(24, 32);
        String jsonData = message.substring(32);
        return new Message(msgID, srcID, destID, jsonData, pMsgID);
    }


    /**
     * Method for parsing simulator message to client in string format. Return
     * format of message is: -messageID srcID destID prevMsgID JSON_DATA-.
     *
     * @return message in described format
     */
    public String makeMessage() {
        return messageID + srcID + destID + previousMessageID + jsonData;
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

    @Override
    public String toString() {
        return "Message:\nmessageID=" + messageID + "\nsrcID=" + srcID + "\ndestID=" + destID + "\nprevMessageID="
                + previousMessageID + "\ndata:" + jsonData + "\n";
    }

}