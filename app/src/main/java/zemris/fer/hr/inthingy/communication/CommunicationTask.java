package zemris.fer.hr.inthingy.communication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


public class CommunicationTask {

    public CommunicationTask(String destIP, String destPort, byte[] message) {
        Thread clientThread = new Thread(new SendMessageThread(destIP, destPort, message));
        clientThread.start();
    }

    /**
     * Thread for sending message.
     */
    private class SendMessageThread extends Thread {

        private Socket clientSocket;
        private byte[] message;

        private SendMessageThread(String destIP, String destPort, byte[] message) {
            this.message = message;
            try {
                clientSocket = new Socket(destIP, Integer.valueOf(destPort));
            } catch (Exception e1) {
                Log.e("SendMessageThread", e1.getMessage());
            }
        }

        @Override
        public void run() {
            OutputStream outputStream;
            try {
                outputStream = clientSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(new String(message));
                printStream.close();
                Thread socketServerThread = new Thread(new RecieveMessageThread(clientSocket.getLocalPort()));
                socketServerThread.start();
            } catch (IOException e) {
                Log.e("SendMessageThread", e.toString());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    //do nothing
                }
            }

        }

    }

    /**
     * Recieve message thread
     */
    private class RecieveMessageThread extends Thread {

        private ServerSocket serverSocket;

        private RecieveMessageThread(int serverPort) {
            try {
                serverSocket = new ServerSocket(serverPort);
            } catch (IOException e) {
                //do nothing
            }
        }

        @Override
        public void run() {
            try {
                Socket socket = serverSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (!Thread.currentThread().isInterrupted()) {
                    String read = input.readLine();
                    if ("idle".equals(read)) {
                        break;
                    } else {
                        Log.d("RECIEVED", read);
                    }
                }
            } catch (IOException e) {
                Log.e("CommServiceServerThread", e.getMessage());
            }
        }

    }


}
