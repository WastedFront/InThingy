package zemris.fer.hr.inthingy.communication;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import zemris.fer.hr.inthingy.R;
import zemris.fer.hr.inthingy.utils.Constants;
import zemris.fer.hr.inthingy.utils.MyUtils;

/**
 * Class for handling communication with destination.
 * For now it can only send data through Internet.
 */
public class CommunicationTask {

    /**
     * Context of some activity which uses this class.
     */
    private Context mContext;

    /**
     * Constructor with multiple parameters.
     *
     * @param destIP
     *         IP adress of destination
     * @param destPort
     *         Port on which destination is listening
     * @param message
     *         Message for sending
     * @param context
     *         context of some activity
     * @param sendMode
     *         how message will be send (through Internet, Bluetooth, Wi-Fi).
     */
    public CommunicationTask(String destIP, String destPort, byte[] message, Context context, String sendMode) {
        mContext = context;
        switch (sendMode) {
            case "Internet":
                if (MyUtils.isNetworkAvailable(context)) {
                    (new SendToServerTask()).execute(destIP, destPort, new String(message));
                } else {
                    Toast.makeText(context, context.getResources().getText(R.string.error_no_internet_conn),
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                Toast.makeText(context, context.getResources().getText(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Task for sending message to some cloud using Internet.
     * It needs 3 parameters send to {#link doInBackground} method in following order:
     * DESTINATION_IP, DESTINATION_PORT, MESSAGE.
     */
    public class SendToServerTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            BufferedWriter out = null;
            BufferedReader in = null;
            Socket socket = new Socket();
            try {
                String destIP = params[0];
                int destPort = Integer.valueOf(params[1]);
                String message = params[2];
                socket.connect(new InetSocketAddress(destIP, destPort), 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.write(message + "\r\n");
                out.flush();
                String returnMessage = in.readLine();
                //if return message is different then idle, it will be stored so it can be replied to
                if (!"idle".equals(returnMessage.toLowerCase())) {
                    MyUtils.stringArrayPref(mContext, Constants.KEY_RECEIVED_MESSAGES, returnMessage, true);
                }

            } catch (Exception e) {
                return Constants.STRING_ERROR;
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    socket.close();
                } catch (Exception e) {
                    //ignore
                }
            }
            return Constants.STRING_OK;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        }
    }
}
