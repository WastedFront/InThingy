package zemris.fer.hr.inthingy.communication;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import zemris.fer.hr.inthingy.utils.Constants;

/**
 * Async task for sending given message to server.
 * Parameters need to be in following order: destination IP, message.
 */
public class SendToServerTask extends AsyncTask<String, String, String> {

    /** Variable representing client. */
    private Socket client;
    /** Constant for prt which will be used. */
    private static final int PORT = 5200;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String destinationIP = params[0];
        String message = params[1];

        try {
            client = new Socket(destinationIP, PORT);// ip address is entered over here....
            PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);// getting the outputstream
            printwriter.write(message);// writing the message
            printwriter.flush();// flushing the printwriter
            printwriter.close();// closing printwriter
        } catch (IOException e) {
            return Constants.STRING_ERROR;
        }
        return Constants.STRING_OK;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (Constants.STRING_OK.equals(s)) {
            //say ok
        } else {
            //say error
        }
        if (client != null) {
            try {
                client.close();// closing client
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}