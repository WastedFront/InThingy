package zemris.fer.hr.inthingy.communication;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import zemris.fer.hr.inthingy.utils.Constants;

/**
 * Async task for sending given message to server.
 * Parameters need to be in following order: destination IP, message.
 */
public class SendToServerTask extends AsyncTask<String, String, String> {

    /** Constant for port which will be used. */
    private static final int PORT = 25000;
    /** Application context. */
    private Context mContext;
    String response = "";

    /**
     * Constructor for getting context of activity or application.
     */
    public SendToServerTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String destinationIP = params[0];
        String message = params[1];

        Socket socket = null;

        try {
            socket = new Socket(destinationIP, PORT);

            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(message);

            ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

    /*
     * notice:
     * inputStream.read() will block if no data return
     */
            Log.e("SERVER", "before waiting loop");
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }
            Log.e("SERVER", "after waiting loop");

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return Constants.STRING_OK;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e("SERVER", response);
        if (Constants.STRING_OK.equals(s)) {
            Toast.makeText(mContext, "Message sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Message error", Toast.LENGTH_SHORT).show();
        }
    }

}
