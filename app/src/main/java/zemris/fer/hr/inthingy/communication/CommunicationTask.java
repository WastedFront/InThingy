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

import zemris.fer.hr.inthingy.utils.Constants;


public class CommunicationTask {

    private Context mContext;

    public CommunicationTask(final String destIP, final String destPort, final byte[] message, Context context) {
        mContext = context;
        (new SendToServerTask()).execute(destIP, destPort, new String(message));
    }

    /**
     * Async task for sending given message to server.
     */
    public class SendToServerTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String destIP = params[0];
            int destPort = Integer.valueOf(params[1]);
            String message = params[2];
            BufferedWriter out = null;
            BufferedReader in = null;
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(destIP, destPort), 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.write(message + "\r\n");
                out.flush();
                String msg = in.readLine();
                Log.e("COMM got message", msg);

            } catch (Exception e) {
                Log.e("COMM exception", e.getMessage());
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
