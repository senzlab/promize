package com.score.cbook.async;

import android.os.AsyncTask;
import android.util.Log;

import com.score.cbook.interfaces.IPostTaskListener;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.JsonUtil;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostTask extends AsyncTask<String, String, Integer> {

    private static final String TAG = PostTask.class.getName();

    public static final String UZER_API = "http://10.2.2.9:7171/uzers";
    public static final String CONNECTION_API = "http://10.2.2.9:7171/connections";
    public static final String PROMIZE_API = "http://10.2.2.9:7171/promizes";

    private IPostTaskListener listener;
    private String api;
    private SenzMsg senzMsg;

    public PostTask(IPostTaskListener listener, String api, SenzMsg senzMsg) {
        this.listener = listener;
        this.api = api;
        this.senzMsg = senzMsg;
    }

    @Override
    protected Integer doInBackground(String... params) {
        return doPost(params[0], senzMsg);
    }

    @Override
    protected void onPostExecute(Integer status) {
        super.onPostExecute(status);

        listener.onFinishTask(status);
    }

    private int doPost(String httpMethod, SenzMsg senzMsg) {
        try {
            URL url = new URL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(httpMethod);
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(JsonUtil.toJson(senzMsg));
            os.flush();
            os.close();

            int statusCode = conn.getResponseCode();
            String response = conn.getResponseMessage();
            Log.i(TAG, "StatusCode: " + statusCode);
            Log.i(TAG, "Response: " + response);

            return statusCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpURLConnection.HTTP_BAD_REQUEST;
    }
}
