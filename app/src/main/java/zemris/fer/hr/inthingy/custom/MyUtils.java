package zemris.fer.hr.inthingy.custom;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;

import java.util.Map;

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
     * Method for creating message with given parameters.
     * Message format is:
     *
     * @param id
     *         thing id
     * @param source
     *         this address
     * @param destination
     *         destination address
     * @param encryption
     *         encryption which will be used in message
     * @param dataMap
     *         map containing data.
     * @return message in valid format
     */
    public static String createMessage(String id, String source, String destination, String encryption, Map<String, String> dataMap) {
        StringBuilder message = new StringBuilder();

        return message.toString();
    }
}
