package zemris.fer.hr.inthingy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import zemris.fer.hr.inthingy.R;

/**
 * Class provides methods for storing values which need to be persistent.
 */
public class StoringUtils {

    /**
     * Method for storing array in {@code SharedPreferences} in JSON format and getting the same thing out depending
     * on given flag. If flag is true, given value will be stored into array, if flag is false and given value is null
     * method will return list of messages and if flag is false and value is not null then value is some message which
     * will be deleted from array.
     *
     * @param context
     *         some context
     * @param key
     *         key for stored value
     * @param value
     *         value if there needs to be stored
     * @param flag
     *         if true then given value will be stored in preferences, otherwise list of messages will be given
     *         or message will be deleted
     * @return list of messages if flag is false and value null, otherwise null
     */
    private static ArrayList<String> stringArrayPref(Context context, String key, String value, boolean flag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        try {
            SharedPreferences.Editor editor = prefs.edit();
            JSONArray jsonArray;
            if (flag) {
                if (json == null) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(json);
                }
                jsonArray.put(value);
                editor.putString(key, jsonArray.toString());
                editor.apply();
                return null;
            } else if (value != null) {
                if (json != null) {
                    jsonArray = new JSONArray(json);
                    JSONArray newJsonArray = new JSONArray();
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        String obj = jsonArray.optString(i);
                        if (!value.equals(obj)) {
                            newJsonArray.put(obj);
                        }
                    }
                    editor.putString(key, newJsonArray.toString());
                    editor.apply();
                    return null;
                }
            } else {
                ArrayList<String> messages = new ArrayList<>();
                if (json != null) {
                    jsonArray = new JSONArray(json);
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        messages.add(jsonArray.optString(i));
                    }
                }
                return messages;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Method for adding received message to collection of received messages.
     *
     * @param context
     *         some context
     * @param message
     *         new message to add
     */
    public static void addReceivedMessage(Context context, ReceivedMessage message) {
        stringArrayPref(context, Constants.KEY_RECEIVED_MESSAGES, message.storeMsgFormat(), true);
    }

    /**
     * Method for removing message from collection of received messages.
     *
     * @param context
     *         some context
     * @param message
     *         message which will be removed
     */
    public static void removeReceivedMessage(Context context, ReceivedMessage message) {
        stringArrayPref(context, Constants.KEY_RECEIVED_MESSAGES, message.storeMsgFormat(), false);
    }

    /**
     * Method for getting list of received messages.
     *
     * @param context
     *         some context
     * @return list of messages or ull
     */
    public static List<ReceivedMessage> getReceivedMessages(Context context) {
        List<String> unparsedMsgs = stringArrayPref(context, Constants.KEY_RECEIVED_MESSAGES, null, false);
        List<ReceivedMessage> messages = new ArrayList<>();
        if (unparsedMsgs != null) {
            for (String unpMsg : unparsedMsgs) {
                messages.add(ReceivedMessage.parseStoreMsg(unpMsg));
            }
        }
        return messages;
    }

    /**
     * Method for adding sensor into shared preferences.
     *
     * @param context
     *         some context
     * @param sensor
     *         new sensor to add
     */
    public static boolean addSensor(Context context, String sensor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.KEY_SENSORS, null);
        try {
            SharedPreferences.Editor editor = prefs.edit();
            JSONArray jsonArray;
            if (json == null) {
                jsonArray = new JSONArray();
            } else {
                jsonArray = new JSONArray(json);
            }
            jsonArray.put(sensor);
            editor.putString(Constants.KEY_SENSORS, jsonArray.toString());
            editor.apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method for getting sensors stored in shared preferences.
     *
     * @param context
     *         some context
     * @return list of sensors, or null
     */
    public static List<String> getSensors(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.KEY_SENSORS, null);
        try {
            JSONArray jsonArray;
            ArrayList<String> messages = new ArrayList<>();
            if (json != null) {
                jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    messages.add(jsonArray.optString(i));
                }
            }
            return messages;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Method for adding address into shared preferences as frequently used one, if address already exists, it won't
     * be added.
     *
     * @param context
     *         some context
     * @param address
     *         address in proper format
     */
    public static boolean addDestinationAddress(Context context, String address) {
        if (!Pattern.compile(Constants.REGEX_DESTINATION_FORMAT).matcher(address).matches()) {
            return false;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.KEY_DESTINATION_ADDRESSES, null);
        try {
            SharedPreferences.Editor editor = prefs.edit();
            JSONArray jsonArray;
            if (json == null) {
                jsonArray = new JSONArray();
            } else {
                jsonArray = new JSONArray(json);
            }
            for (int i = 0, len = jsonArray.length(); i < len; ++i) {
                if (jsonArray.optString(i).equals(address)) {
                    return true;
                }
            }
            if (jsonArray.length() == 5) {
                jsonArray.put(0, address);
            } else {
                jsonArray.put(address);
            }
            editor.putString(Constants.KEY_DESTINATION_ADDRESSES, jsonArray.toString());
            editor.apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method for getting some destination addresses which are used lately.
     * If there are no such addresses, then method will return some default ones saved in {@code strings.xml}.
     *
     * @param context
     *         application context
     * @return array of addresses or null if there is error while parsing
     */
    public static String[] getDestinationAddresses(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(Constants.KEY_DESTINATION_ADDRESSES, null);
        try {
            if (json == null) {
                String[] addresses = context.getResources().getStringArray(R.array.some_destinations);
                for (String address : addresses) {
                    addDestinationAddress(context, address);
                }
                return addresses;
            }
            JSONArray jsonArray = new JSONArray(json);
            String[] addresses = new String[jsonArray.length()];
            for (int i = 0, len = jsonArray.length(); i < len; ++i) {
                addresses[i] = jsonArray.optString(i);
            }
            return addresses;
        } catch (Exception e) {
            return null;
        }
    }
}
