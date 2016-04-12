package zemris.fer.hr.inthingy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;

import zemris.fer.hr.inthingy.communication.SendToServerTask;

/**
 * Utility class which contains some method that are used by multiple activities/services.
 */
public class MyUtils {

    /** Generator for message ID. */
    private static final Random random = new Random();

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


    public static void sendMessage(String deviceId, String encryption, String sendMode, String destinationFormat,
                                   Map<String, String> sensorDataMap, Context context) {
        String[] splitDestinationFormat = destinationFormat.split(" ");
        String[] portIP = splitDestinationFormat[0].split(":");
        String destinationIP = portIP[0];
        String destinationPort = portIP[1];
        String destinationID = splitDestinationFormat[1];
        byte[] header = createHeader(deviceId, destinationID);
        (new SendToServerTask(context)).execute(destinationIP, destinationPort, "ABC");
    }

    private static byte[] createHeader(String deviceId, String destinationID) {
        byte[] messageId = new byte[8];
        random.nextBytes(messageId);
        String header = String.valueOf(messageId) + deviceId + destinationID;
        return header.getBytes(Charset.defaultCharset());
    }
}
