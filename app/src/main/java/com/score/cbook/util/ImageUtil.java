package com.score.cbook.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {

    public static byte[] compressImage(byte[] data, boolean rotate, boolean mirror) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;

        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        // max Height and width values of the compressed image is taken as 816x612
        float maxHeight = 1155.0f;
        float maxWidth = 866.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        // setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        // inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        // this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        // rotate
        if (rotate) {
            Matrix matrix = new Matrix();
            int orientation = Exif.getOrientation(data);
            if (orientation == 0) {
                // mirror
                if (mirror) {
                    // rotate based on camera id(front/back)
                    matrix.preScale(-1.0f, 1.0f);
                    matrix.postRotate(90);
                } else {
                    matrix.postRotate(-90);
                }
            } else {
                matrix.postRotate(orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);

        return out.toByteArray();
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static String encodeBitmap(byte[] bitmapData) {
        return Base64.encodeToString(bitmapData, Base64.DEFAULT);
    }

    public static Bitmap decodeBitmap(String encodedImage) {
        byte data[] = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static Bitmap blur(Bitmap image, float blurRadius, Context con) {
        if (null == image) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(con);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(blurRadius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    public static void saveImg(String name, String image) {
        byte data[] = Base64.decode(image, Base64.DEFAULT);
        saveImg(name, data);
    }

    public static String saveImg(String name, byte[] image) {
        // create root
        File rahasakRootDir = new File(Environment.getExternalStorageDirectory().getPath() + "/ChequeBook");
        if (!rahasakRootDir.exists()) {
            rahasakRootDir.mkdirs();
        }

        // save selfi
        File selfi = new File(rahasakRootDir, name);
        try {
            FileOutputStream fos = new FileOutputStream(selfi, false);
            fos.write(image);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return selfi.getAbsolutePath();
    }

    public static void deleteImg(String name) {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/ChequeBook/" + name);
        if (file.exists()) {
            file.delete();
        }
    }

    public static String[] splitImg(String src, int len) {
        String[] result = new String[(int) Math.ceil((double) src.length() / (double) len)];
        for (int i = 0; i < result.length; i++)
            result[i] = src.substring(i * len, Math.min(src.length(), (i + 1) * len));
        return result;
    }

    public static byte[] bmpToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap bytesToBmp(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap loadImg(Context context, String imgName) {
        Bitmap bit = null;
        try {
            InputStream bitmap = context.getAssets().open(imgName);
            bit = BitmapFactory.decodeStream(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bit;
    }

    public static Bitmap addSign(Bitmap chq, Bitmap sig) {
        Bitmap rSig = Bitmap.createScaledBitmap(sig, chq.getWidth(), chq.getHeight(), false);

        Bitmap sChq = Bitmap.createBitmap(chq.getWidth(), chq.getHeight(), chq.getConfig());
        Canvas canvas = new Canvas(sChq);
        canvas.drawBitmap(chq, 0, 0, null);
        canvas.drawBitmap(rSig, 0, 0, null);

        return sChq;
    }

    public static Bitmap addText(Bitmap chqImg, int amount, String account, String date) {
        Bitmap sChq = Bitmap.createBitmap(chqImg.getWidth(), chqImg.getHeight(), chqImg.getConfig());
        Canvas canvas = new Canvas(sChq);
        canvas.drawBitmap(chqImg, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(32);
        canvas.drawText(account, 130, 450, paint);
        canvas.drawText(date, 1010, 150, paint);
        canvas.drawText(amount + ".00", 1000, 250, paint);
        canvas.drawText(NumberUtil.convert(amount), 160, 250, paint);

        return sChq;
    }

}
