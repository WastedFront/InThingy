package zemris.fer.hr.inthingy.utils;

/**
 * Class contains all constants that are used in application and are needed in multiple classes.
 */
public class Constants {
    /** Name for GPS sensor. */
    public static final String GPS_SENSOR_NAME = "GPS";
    /** Name for the error which can be return value of some methods. */
    public static final String STRING_ERROR = "ERROR";
    /** Name for the ok result which can be return value of some methods. */
    public static final String STRING_OK = "OK";
    /** Default sensor data. */
    public static final String DEFAULT_SENSOR_DATA = "NULL\nNULL\nNULL";
    /** Empty tab name. */
    public static final String EMPTY_TAB_TAG = "EMPTY";
    /** Key for saving found sensors. */
    public static final String KEY_FOUND_SENSORS = "FOUND_SENSORS";
    /** Key for saving selected sensors. */
    public static final String KEY_SEL_SENSORS = "SELECTED_SENSORS";
    /** Key auto reply service running. */
    public static final String KEY_AUTO_REPLY_ON = "AUTOREPLY_ON";
    /** Key for destination string. */
    public static final String KEY_DESTINATION = "DESTINATION";
    /** Key for encryption string. */
    public static final String KEY_ENCRYPTION = "ENCRYPTION";
    /** Key for send mode string. */
    public static final String KEY_SEND_MODE = "SEND_MODE";
    /** Key for device id string. */
    public static final String KEY_DEVICE_ID = "DEVICE_ID";
    /** Key for storing destination adresses. */
    public static final String KEY_DESTINATION_ADDRESSES = "DESTINATION_ADRESSES";
    /** Key for getting received messages. */
    public static final String KEY_RECEIVED_MESSAGES = "RECEIVED_MESSAGES";
    /** Key for getting sensors. */
    public static final String KEY_SENSORS = "FOUND_SENSORS";
    /** Regex for checking destination, format is: IP_ADDR:PORT DEST_THING_NAME */
    public static final String REGEX_DESTINATION_FORMAT = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})\\s+[a-zA-Z0-9\\_\\-]{8}";
    /** String which is used to split values for received message. */
    public static final String MSG_DELIM = "L;;R";
}
