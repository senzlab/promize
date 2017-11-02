package com.score.cbook.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.score.cbook.interfaces.ICheckImageGeneratorListener;
import com.score.cbook.pojo.Cheque;
import com.score.cbook.utils.ImageUtils;

public class CheckImageGenerator extends AsyncTask<Cheque, String, String> {

    private Context context;
    private ICheckImageGeneratorListener listener;

    public CheckImageGenerator(Context context, ICheckImageGeneratorListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Cheque... params) {
        return generateImg(params[0]);
    }

    @Override
    protected void onPostExecute(String bitmap) {
        super.onPostExecute(bitmap);

        listener.onGenerate(bitmap);
    }

    private String generateImg(Cheque cheque) {
        // add text
        Bitmap chq = ImageUtils.loadImg(context, "echq.jpg");
        Bitmap stChq = ImageUtils.addText(chq, cheque.getAmount(), cheque.getUser().getUsername());

        // compress
        byte[] bytes = ImageUtils.bmpToBytes(stChq);
        byte[] compBytes = ImageUtils.compressImage(bytes, true);

        return ImageUtils.encodeBitmap(compBytes);
    }
}
