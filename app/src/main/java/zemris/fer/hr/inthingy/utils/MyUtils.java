package zemris.fer.hr.inthingy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import zemris.fer.hr.inthingy.R;
import zemris.fer.hr.inthingy.communication.SendToServerTask;

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
     * Method for getting IP adress of device if it is connected to the internet.
     *
     * @return valid IP address if everything is ok, ERROR if there is exception and there is return null which will never happen
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            return Constants.STRING_ERROR;
        }
        return null;
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
        message.append("'mid'=1").append("'src'=").append(id).append("'dest'=").append(destination).append("'data'=");
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            message.append(entry.getKey()).append("\n").append(entry.getValue()).append(";;"); //;; je simbol po kojem treba podatke splitati
        }
        return message.toString();
    }

    /**
     * Method for sending message with given parameters.
     * It checks if device is connected to the Internet or not.
     *
     * @param context
     *         context of activity/service which calls this method
     * @param thingId
     *         id of device which is sending message
     * @param source
     *         source address
     * @param destination
     *         destination address
     * @param encryption
     *         encryption which will be used in message
     * @param sendMode
     *         adapter through which message will be send
     * @param dataMap
     *         map containing data.
     * @return true if message is sent, otherwise false
     */
    public static boolean sendMessage(Context context, String thingId, String source, String destination,
                                      String encryption, String sendMode, Map<String, String> dataMap) {
        if (!isNetworkAvailable(context)) {
            Toast.makeText(context, context.getResources().getString(R.string.error_no_internet_conn), Toast.LENGTH_LONG).show();
            return false;
        }
        String message = MyUtils.createMessage(thingId, source, destination, encryption, dataMap);
        (new SendToServerTask(context)).execute(destination, message);
        return false;
    }

}
