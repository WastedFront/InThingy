package zemris.fer.hr.inthingy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.guna.libmultispinner.MultiSelectionSpinner;

import org.json.JSONObject;

import java.util.List;

import zemris.fer.hr.inthingy.R;
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
     * Method for responding to received message. It parses message and sends response
     *
     * @param message
     *         message in format  SEND_MODE;PREV_MSG_ID;SRC_IP;SRC_PORT;THING_ID;DATA
     * @param context
     *         context of application or main activity
     */
    public static void respondToMessage(String message, Context context) {
        StringBuilder builder = new StringBuilder();
        String[] splits = message.split(Constants.RECEIVED_MSG_DELIM);
        if (splits.length != 6) {
            Toast.makeText(context, context.getResources().getText(R.string.error), Toast.LENGTH_SHORT).show();
            return;
        }
        String sendMode = splits[0];
        byte[] id = splits[1].getBytes();
        String destIP = splits[2];
        String destport = splits[3];
        String destID = splits[4];
        StoringUtils.removeReceivedMessage(context, message);
    }


    /**
     * Method for getting data from received messages that are stored locally.
     * Received message is in following format: SEND_MODE;PREV_MSG_ID;SRC_IP;SRC_PORT;THING_ID;MY_ID;DATA
     *
     * @return message in following format: THING_ID:\n CMD SENSOR1,SENSOR2, ...
     */
    public static String getReceivedMessageInfo(String message) {
        String[] splits = message.split(Constants.RECEIVED_MSG_DELIM);
        if (splits.length != 7) {
            return Constants.STRING_ERROR;
        }
        try {
            JSONObject jsonObject = new JSONObject(splits[6]);
            String cmd = jsonObject.getString("CMD");
            String sensors = jsonObject.getString("SENSOR").replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
            return splits[4] + ":\n" + cmd + " " + sensors;
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
                .append("SOURCE:  ").append(splits[2]).append(':').append(splits[3]).append('\n')
                .append('\n').append(splits[6]);
        return builder.toString();
    }
}
