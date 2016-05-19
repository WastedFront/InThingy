package zemris.fer.hr.inthingy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;

import com.guna.libmultispinner.MultiSelectionSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import zemris.fer.hr.inthingy.custom.DataForSpinnerTask;

/**
 * Utility class which contains some methods that are used by multiple activities/services.
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
     * Method for creating JSON data from sensors map and returning it as string.
     *
     * @param sensorDataMap
     *         map contains sensors and their values
     * @return string representation of JSON data
     */
    public static String createJSONData(Map<String, String> sensorDataMap) {
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
        return jsonData.toString();
    }
}
