package com.score.cbook.async;

import android.os.AsyncTask;

import com.score.cbook.interfaces.ISenzPublisherListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SenzPublisher extends AsyncTask<String, String, String> {

    //private static final String SENZ_HOST = "10.2.2.9";
    //private static final String SENZ_HOST = "34.226.3.46";
    //private static final String SENZ_HOST = "10.25.246.115";
    private static final String SENZ_HOST = "222.165.167.19";
    private static final int SENZ_PORT = 7171;

    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    private ISenzPublisherListener listener;

    public SenzPublisher(ISenzPublisherListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            socket = new Socket(InetAddress.getByName(SENZ_HOST), SENZ_PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            writeSenz(params[0]);
            return readSenz();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String senz) {
        super.onPostExecute(senz);
        listener.onFinish(senz);

        // close conn
        if (socket != null) try {
            socket.close();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSenz(String senz) throws IOException {
        outputStream.writeBytes(senz + ";");
        outputStream.flush();
    }

    private String readSenz() throws IOException {
        StringBuilder builder = new StringBuilder();
        int z;
        char c;
        while ((z = inputStream.read()) != -1) {
            c = (char) z;
            if (c == ';') {
                String senz = builder.toString();
                if (!senz.isEmpty()) {
                    return senz;
                }
            } else {
                builder.append(c);
            }
        }

        return null;
    }
}
