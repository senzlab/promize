package com.score.cbook.async;

import android.os.AsyncTask;
import android.util.Log;

import com.score.cbook.interfaces.IPostTaskListener;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.JsonUtil;
import com.score.senzc.pojos.Senz;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class PostTask extends AsyncTask<String, String, String> {

    private static final String TAG = PostTask.class.getName();

    private static final String CONTRACTZ_API = "https://uatweb.sampath.lk/igift/v1/contractz";

    private IPostTaskListener listener;
    private SenzMsg senzMsg;

    public PostTask(SenzMsg senzMsg, IPostTaskListener listener) {
        this.listener = listener;
        this.senzMsg = senzMsg;
    }

    @Override
    protected String doInBackground(String... params) {
        return doPost(senzMsg);
    }

    private String doPost(SenzMsg senzMsg) {
        try {
            // ssl config
            URL url = new URL(CONTRACTZ_API);
            SSLContext sslcontext = SSLContext.getInstance("TLSv1.2");
            sslcontext.init(null, null, null);
            SSLSocketFactory NoSSLv3Factory = new NoSSLv3Factory(sslcontext.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // connection
            //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // post
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(JsonUtil.toJson(senzMsg));
            os.flush();
            os.close();

            // read response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.i(TAG, "StatusCode: " + conn.getResponseCode());
            Log.i(TAG, "Response: " + response.toString());

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String msg) {
        super.onPostExecute(msg);

        // parse and find result, status back
        try {
            Senz senz = JsonUtil.toSenz(msg);
            listener.onFinishTask(senz.getAttributes().get("status"));
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onFinishTask("400");
        }
    }
}
