package com.score.cbook.async;

import android.os.AsyncTask;
import android.util.Log;

import com.score.cbook.interfaces.IFetchTaskListener;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.ImageUtil;
import com.score.cbook.util.JsonUtil;
import com.score.senzc.pojos.Senz;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchTask extends AsyncTask<SenzMsg, String, Integer> {

    private static final String TAG = FetchTask.class.getName();

    public static final String BLOB_API = "http://uatweb.sampath.lk/blobs";

    private IFetchTaskListener listener;
    private String api;

    public FetchTask(IFetchTaskListener listener, String api) {
        this.listener = listener;
        this.api = api;
    }

    @Override
    protected Integer doInBackground(SenzMsg... params) {
        return doFetch(params[0]);
    }

    @Override
    protected void onPostExecute(Integer status) {
        super.onPostExecute(status);

        listener.onFinishTask(status);
    }

    private int doFetch(SenzMsg senzMsg) {
        try {
            // get blob
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

            // extract blob
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer response = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // save blob
            int statusCode = conn.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) saveBlob(response.toString());

            Log.i(TAG, "StatusCode: " + statusCode);
            Log.i(TAG, "Response: " + response);

            return statusCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpURLConnection.HTTP_BAD_REQUEST;
    }

    private void saveBlob(String response) throws JSONException {
        Senz senz = JsonUtil.toSenz(response);
        String imgName = senz.getAttributes().get("uid") + ".jpg";
        ImageUtil.saveImg(imgName, senz.getAttributes().get("blob"));
    }
}
