package com.score.cbook.async;

import android.os.AsyncTask;
import android.util.Log;

import com.score.cbook.interfaces.IPostTaskListener;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.JsonUtil;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchTask extends AsyncTask<SenzMsg, String, Integer> {

    private static final String TAG = FetchTask.class.getName();

    public static final String PROMIZE_API = "http://10.2.2.9:7171/promizes";

    private IPostTaskListener listener;
    private String api;

    public FetchTask(IPostTaskListener listener, String api) {
        this.listener = listener;
        this.api = api;
    }

    @Override
    protected Integer doInBackground(SenzMsg... params) {
        return doPost(params[0]);
    }

    @Override
    protected void onPostExecute(Integer status) {
        super.onPostExecute(status);

        listener.onFinishTask(status);
    }

    private int doPost(SenzMsg senzMsg) {
        try {
            URL url = new URL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
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
