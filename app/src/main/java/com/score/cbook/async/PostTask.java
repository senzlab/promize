package com.score.cbook.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.score.cbook.interfaces.IPostTaskListener;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.JsonUtil;
import com.score.cbook.util.PreferenceUtil;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

public class PostTask extends AsyncTask<String, String, Integer> {

    private static final String TAG = PostTask.class.getName();

    public static final String UZER_API = "https://uatweb.sampath.lk/uzers";
    public static final String CONNECTION_API = "https://uatweb.sampath.lk:443/connections";
    public static final String PROMIZE_API = "https://uatweb.sampath.lk:443/promizes";

    private IPostTaskListener listener;
    private String api;
    private SenzMsg senzMsg;
    private Context context;

    public PostTask(Context context, IPostTaskListener listener, String api, SenzMsg senzMsg) {
        this.context = context;
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
            // load certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = new BufferedInputStream(context.getAssets().open("sampath.cer"));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, tmf.getTrustManagers(), null);

            URL url = new URL(PostTask.UZER_API);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(JsonUtil.toJson(senzMsg));
            os.flush();
            os.close();

            conn.connect();
            InputStream in = conn.getInputStream();


            int statusCode = conn.getResponseCode();
            Log.i(TAG, "StatusCode: " + statusCode);

            return statusCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpURLConnection.HTTP_BAD_REQUEST;
    }

    private void copyInOut(InputStream in) {

    }
}
