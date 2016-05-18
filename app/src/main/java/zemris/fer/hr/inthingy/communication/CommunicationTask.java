package zemris.fer.hr.inthingy.communication;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

import zemris.fer.hr.inthingy.R;
import zemris.fer.hr.inthingy.utils.Constants;
import zemris.fer.hr.inthingy.utils.MyUtils;
import zemris.fer.hr.inthingy.utils.StoringUtils;

/**
 * Class for handling communication with some destination device. It handles all types of connections (Internet, Wi-Fi, Bluetooth).
 * Currently only communication through Internet is implemented.
 */
public class CommunicationTask {

    /**
     * Context of some activity which uses this class.
     */
    private Context mContext;
    /**
     * Flag to control if toast which says if message is sent or not shows or not.
     */
    private boolean show;

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
     * @param showSendResult
     *         flag to control if toast which says if message is sent or not shows or not.
     */
    public CommunicationTask(String destIP, String destPort, byte[] message, Context context, String sendMode,
                             boolean showSendResult) {
        mContext = context;
        show = showSendResult;
        switch (sendMode.toUpperCase()) {
            case "INTERNET":
                if (MyUtils.isNetworkAvailable(context)) {
                    (new SendToServerTask()).execute(destIP, destPort, new String(message, Charset.forName("UTF-8")));
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
                int destPort = Integer.parseInt(params[1]);
                String message = params[2];
                socket.connect(new InetSocketAddress(destIP, destPort), 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                out.write(message + "\r\n");
                out.flush();
                String returnMessage = in.readLine();
                if (returnMessage == null) {
                    throw new Exception("Null received msg");
                }
                //if return message is different then idle, it will be stored so it can be replied to
                if (!"idle".equals(returnMessage.toLowerCase())) {
                    String storeMsg = "Internet" + Constants.RECEIVED_MSG_DELIM   //send mode
                            + returnMessage.substring(0, 8) + Constants.RECEIVED_MSG_DELIM //message id
                            + destIP + Constants.RECEIVED_MSG_DELIM // server IP
                            + destPort + Constants.RECEIVED_MSG_DELIM //server port
                            + returnMessage.substring(8, 16) + Constants.RECEIVED_MSG_DELIM   //destination id
                            + returnMessage.substring(16, 24) + Constants.RECEIVED_MSG_DELIM   //my id
                            + returnMessage.substring(32); //JSON data
                    StoringUtils.addReceivedMessage(mContext, storeMsg);
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
            if (show) {
                if (Constants.STRING_OK.equals(s)) {
                    Toast toast = Toast.makeText(mContext, mContext.getResources().getString(R.string.success), Toast.LENGTH_SHORT);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    v.setTextColor(Color.GREEN);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(mContext, mContext.getResources().getString(R.string.error), Toast.LENGTH_SHORT);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    v.setTextColor(Color.RED);
                    toast.show();
                }
            }
        }
    }
}
