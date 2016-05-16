package zemris.fer.hr.inthingy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;

import com.gdubina.multiprocesspreferences.MultiprocessPreferences;
import com.guna.libmultispinner.MultiSelectionSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zemris.fer.hr.inthingy.communication.CommUtils;
import zemris.fer.hr.inthingy.communication.CommunicationTask;
import zemris.fer.hr.inthingy.custom.DataForSpinnerTask;

/**
 * Utility class which contains some method that are used by multiple activities/services.
 */
public class MyUtils {
    /**
     * Method for checking if network is available and connected.
     *
     * @param context
     *         context for {@code ConectivityManager}.
     * @return true if device is connected to the Internet, otherwise false.
     */
    public static boolean isNetworkAvailable(final Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    /**
     * Method for checking if service is running or not.
     *
     * @param serviceClass
     *         class of the service.
     * @param context
     *         preferable activity context
     * @return true if service is already running, otherwise false.
     */
    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method for populating given {@code MultiSelectionSpinner} with sensor names.
     *
     * @param context
     *         context of activity which contains spinner
     * @param spDeviceSensors
     *         spinner for selecting sensors
     */
    public static void getSensors(Context context, MultiSelectionSpinner spDeviceSensors) {
        List<String> sensors = StoringUtils.getSensors(context);
        if (sensors == null || sensors.isEmpty()) {
            (new DataForSpinnerTask(context, spDeviceSensors)).execute();
        } else {
            spDeviceSensors.setItems(sensors);
        }
    }

    /**
     * Method for parsing sensor values into JSON format.
     * Example of input:
     * Proximity: 1.0 cm
     * Example of output:
     * "GPS":{"VALUES":[65.966,-18.533,15.044],"NAMES":["LATITUDE","LONGITUDE","ALTITUDE"]};
     *
     * @param value
     *         value of some sensor, value is stored in {@code MultiprocessPreferences}.
     * @return properly formatted JSONObject
     * @throws JSONException
     *         if there is problem while parsing
     */
    public static JSONObject parseSensorValueToJSON(String value) throws JSONException {
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
        return sensorObject;
    }


    /**
     * Method for responding to received message. It parses message and sends response
     *
     * @param message
     *         message in format  SEND_MODE;PREV_MSG_ID;SRC_IP;SRC_PORT;THING_ID;MY_ID;DATA
     * @param context
     *         context of application or main activity
     * @return true if everything is ok, otherwise false
     */
    public static boolean respondToMessage(String message, Context context) {
        String[] splits = message.split(Constants.RECEIVED_MSG_DELIM);
        if (splits.length != 7) {
            return false;
        }
        String sendMode = splits[0];
        String mID = splits[1];
        String destIP = splits[2];
        String destPort = splits[3];
        String destID = splits[4];
        String myID = splits[5];
        try {
            JSONObject jsonObject = new JSONObject(splits[6]);
            String cmd = jsonObject.getString("CMD");
            if ("GET".equals(cmd.toUpperCase())) {
                JSONArray sensorArray = jsonObject.getJSONArray("SENSOR");
                Map<String, String> sensorDataMap = new HashMap<>();
                for (int i = 0, len = sensorArray.length(); i < len; ++i) {
                    String name = sensorArray.getString(i);
                    String value = MultiprocessPreferences.getDefaultSharedPreferences(context).getString(name, "");
                    //if there is no such sensor, don't put its data
                    if (!"".equals(value)) {
                        sensorDataMap.put(name, value);
                    }
                }
                byte[] msg = CommUtils.createMessage(myID, "NONE", sensorDataMap, destID, mID);
                if (msg == null) {
                    return false;
                }
                new CommunicationTask(destIP, destPort, msg, context, sendMode, false);
            }
            StoringUtils.removeReceivedMessage(context, message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method for getting data from received messages that are stored locally.
     * Received message is in following format: SEND_MODE;PREV_MSG_ID;SRC_IP;SRC_PORT;THING_ID;MY_ID;DATA
     *
     * @return message in following format: THING_ID:\n CMD SENSOR1, SENSOR2, ...
     */
    public static String getReceivedMessageInfo(String message) {
        String[] splits = message.split(Constants.RECEIVED_MSG_DELIM);
        if (splits.length != 7) {
            return Constants.STRING_ERROR;
        }
        try {
            JSONObject jsonObject = new JSONObject(splits[6]);
            String cmd = jsonObject.getString("CMD");
            StringBuilder values = new StringBuilder(" ");
            if ("GET".equals(cmd.toUpperCase())) {
                JSONArray sensorArray = jsonObject.getJSONArray("SENSOR");
                for (int i = 0, len = sensorArray.length(); i < len - 1; ++i) {
                    values.append(sensorArray.getString(i)).append('\n');
                }
                values.append(sensorArray.getString(sensorArray.length() - 1));
            }
            return splits[4] + ":\n" + cmd + values.toString();
        } catch (Exception e) {
            return Constants.STRING_ERROR;
        }
    }

    /**
     * Method for formatting received messages that are stored locally in the way they are printable.
     * Received message is in following format: SEND_MODE;PREV_MSG_ID;SRC_IP;SRC_PORT;THING_ID;DATA
     *
     * @param message
     *         message in format  SEND_MODE;PREV_MSG_ID;SRC_IP;SRC_PORT;THING_ID;MY_ID;DATA
     * @return message in format for print
     */
    public static String parseStoredReceivedMessage(String message) {
        StringBuilder builder = new StringBuilder();
        String[] splits = message.split(Constants.RECEIVED_MSG_DELIM);
        if (splits.length != 7) {
            return Constants.STRING_ERROR;
        }
        builder.append("SEND MODE:  ").append(splits[0]).append('\n')
                .append("THING ID:  ").append(splits[4]).append('\n')
                .append("SOURCE:  ").append(splits[2]).append(':').append(splits[3]).append("\n\n");
        try {
            JSONObject jsonObject = new JSONObject(splits[6]);
            String cmd = jsonObject.getString("CMD");
            StringBuilder values = new StringBuilder();
            if ("GET".equals(cmd.toUpperCase())) {
                JSONArray sensorArray = jsonObject.getJSONArray("SENSOR");
                for (int i = 0, len = sensorArray.length(); i < len - 1; ++i) {
                    values.append(sensorArray.getString(i)).append('\n');
                }
                values.append(sensorArray.getString(sensorArray.length() - 1));
            }
            builder.append(cmd).append(':').append('\n');
            if (!"".equals(values.toString())) {
                builder.append(values.toString());
            }
        } catch (JSONException e) {
            //do nothing
        }
        return builder.toString();
    }
}
