package com.score.cbook.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.senz.ISenzService;
import com.score.senzc.pojos.Senz;

import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();

    // service interface
    protected ISenzService senzService = null;
    protected boolean isServiceBound = false;

    protected Typeface typeface;

    // service connection
    protected ServiceConnection senzServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "Connected with senz service");
            senzService = ISenzService.Stub.asInterface(service);
            isServiceBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "Disconnected from senz service");
            senzService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/GeosansLight.ttf");
    }

    protected void bindToService() {
        Intent intent = new Intent("com.score.cbook.remote.SenzService");
        intent.setPackage(this.getPackageName());
        bindService(intent, senzServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void unbindService() {
        // unbind from service
        if (isServiceBound) {
            Log.d(TAG, "Unbind to senz service");
            unbindService(senzServiceConnection);

            isServiceBound = false;
        }
    }

    public void displayInformationMessageDialog(String title, String message) {
        final Dialog dialog = new Dialog(this);

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.information_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText(title);
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        messageHeaderTextView.setTypeface(typeface, Typeface.BOLD);
        messageTextView.setTypeface(typeface);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(typeface, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void displayConfirmationMessageDialog(String message, final View.OnClickListener okClicked) {
        final Dialog dialog = new Dialog(this);

        // set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_confirm_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText("Confirm");
        messageTextView.setText(Html.fromHtml(message));

        // set custom font
        messageHeaderTextView.setTypeface(typeface, Typeface.BOLD);
        messageTextView.setTypeface(typeface);

        //set ok button
        Button okButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_ok_button);
        okButton.setTypeface(typeface, Typeface.BOLD);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okClicked.onClick(v);
                dialog.cancel();
            }
        });

        // cancel button
        Button cancelButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_cancel_button);
        cancelButton.setTypeface(typeface, Typeface.BOLD);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void sendSenz(Senz senz) {
        if (NetworkUtil.isAvailableNetwork(this)) {
            try {
                if (isServiceBound) {
                    senzService.sendSenz(senz);
                } else {
                    ActivityUtil.showCustomToast("Failed to connected to service.", this);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            ActivityUtil.showCustomToast(this.getResources().getString(R.string.no_internet), this);
        }
    }

    public void sendSenzes(List<Senz> senzList) {
        if (NetworkUtil.isAvailableNetwork(this)) {
            try {
                if (isServiceBound) {
                    senzService.sendSenzes(senzList);
                } else {
                    ActivityUtil.showCustomToast("Failed to connected to service.", this);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            ActivityUtil.showCustomToast(this.getResources().getString(R.string.no_internet), this);
        }
    }
}
