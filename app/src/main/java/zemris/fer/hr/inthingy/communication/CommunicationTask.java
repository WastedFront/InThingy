package zemris.fer.hr.inthingy.communication;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import zemris.fer.hr.inthingy.R;
import zemris.fer.hr.inthingy.utils.Constants;
import zemris.fer.hr.inthingy.utils.Message;
import zemris.fer.hr.inthingy.utils.MyDialogs;
import zemris.fer.hr.inthingy.utils.MyUtils;
import zemris.fer.hr.inthingy.utils.ReceivedMessage;
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
     * @param msg
     *         Message for sending
     * @param context
     *         context of some activity
     * @param showSendResult
     *         flag to control if toast which says if message is sent or not shows or not.
     */
    public CommunicationTask(Context context, Message msg, boolean showSendResult) {
        mContext = context;
        show = showSendResult;
        switch (msg.getSendMode().toUpperCase()) {
            case "INTERNET":
                if (MyUtils.isNetworkAvailable(context)) {
                    (new SendToServerTask()).execute(msg.getDestIP(), "" + msg.getDestPort(), msg.getComSendMessage());
                } else {
                    Toast.makeText(context, context.getResources().getText(R.string.error_no_internet_conn), Toast.LENGTH_LONG).show();
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
                //get parameters
                String destIP = params[0];
                int destPort = Integer.parseInt(params[1]);
                String message = params[2];
                //connect through TCP socket with timeout of 5s
                socket.connect(new InetSocketAddress(destIP, destPort), 5000);
                //find input and output
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                //write message
                out.write(message + "\r\n");
                out.flush();
                //get return message
                String rtnMsg = in.readLine();
                if (rtnMsg == null) {
                    throw new Exception("Null received msg");
                }
                //if return message is different then idle, it will be stored so it can be replied to
                while (!"idle".equals(rtnMsg.toLowerCase())) {
                    String[] encrypt = mContext.getResources().getStringArray(R.array.encryption_array);
                    ReceivedMessage msg = ReceivedMessage.parseReceivedMessage(rtnMsg, "INTERNET",
                            encrypt[rtnMsg.charAt(0) - '0'], destIP, "" + destPort);
                    //store received message
                    StoringUtils.addReceivedMessage(mContext, msg);
                    //get another message
                    rtnMsg = in.readLine();
                    if (rtnMsg == null) {
                        throw new Exception("Null received msg");
                    }
                }
            } catch (Exception e) {
                Log.e("MMCA", e.toString());
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
                    MyDialogs.makeGreenTextToast(mContext, mContext.getResources().getString(R.string.success));
                } else {
                    MyDialogs.makeRedTextToast(mContext, mContext.getResources().getString(R.string.error));
                }
            }
        }
    }
}
