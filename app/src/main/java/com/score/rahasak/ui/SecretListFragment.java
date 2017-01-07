package com.score.rahasak.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.rahasak.R;
import com.score.rahasak.application.IntentProvider;
import com.score.rahasak.db.SenzorsDbSource;
import com.score.rahasak.enums.BlobType;
import com.score.rahasak.enums.IntentType;
import com.score.rahasak.pojo.Secret;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;


public class SecretListFragment extends ListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = SecretListFragment.class.getName();

    private ActionBar actionBar;
    private ImageView actionBarDelete;
    //private TextView actionBarName;

    private ArrayList<Secret> allSecretsList;
    private SecretListAdapter adapter;
    private SenzorsDbSource dbSource;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got new user from Senz service");

            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                if (senz.getSenzType() == SenzTypeEnum.DATA || senz.getSenzType() == SenzTypeEnum.STREAM) {
                    refreshList();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.last_item_chat_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbSource = new SenzorsDbSource(getContext());
        setupEmptyTextFont();
        initActionBar();
        displayList();
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();

        getActivity().registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(senzReceiver);
    }

    private void setupEmptyTextFont() {
        ((TextView) getActivity().findViewById(R.id.empty_view_chat)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/GeosansLight.ttf"));
    }

    private void initActionBar() {
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBarDelete = (ImageView) actionBar.getCustomView().findViewById(R.id.delete);
        //actionBarName = (TextView) actionBar.getCustomView().findViewById(R.id.user_name);
    }

    /**
     * Display sensor list
     * Basically setup list adapter if have items to display otherwise display empty view
     */
    private void displayList() {
        allSecretsList = dbSource.getRecentSecretList();
        adapter = new SecretListAdapter(getContext(), allSecretsList);
        getListView().setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void refreshList() {
        allSecretsList.clear();
        allSecretsList.addAll(dbSource.getRecentSecretList());
        adapter.notifyDataSetChanged();
    }

    private void updateList(Senz senz) {
        for (Secret secret : allSecretsList) {
            if (senz.getSender().getUsername().equalsIgnoreCase(secret.getUser().getUsername())) {
                if (senz.getAttributes().containsKey("cam")) {
                    secret.setBlob(senz.getAttributes().get("cam"));
                    secret.setBlobType(BlobType.IMAGE);
                    secret.setTimeStamp((System.currentTimeMillis() / 1000));
                    adapter.notifyDataSetChanged();
                } else if (senz.getAttributes().containsKey("mic")) {
                    secret.setBlob(senz.getAttributes().get("mic"));
                    secret.setBlobType(BlobType.SOUND);
                    secret.setTimeStamp((System.currentTimeMillis() / 1000));
                    adapter.notifyDataSetChanged();
                } else if (senz.getAttributes().containsKey("msg")) {
                    secret.setBlob(senz.getAttributes().get("msg"));
                    secret.setBlobType(BlobType.TEXT);
                    secret.setTimeStamp((System.currentTimeMillis() / 1000));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Secret secret = allSecretsList.get(position);
        if (secret.isViewed()) {
            secret.setViewed(false);
            adapter.notifyDataSetChanged();
            actionBarDelete.setVisibility(View.GONE);
            //actionBarName.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent(this.getActivity(), ChatActivity.class);
            intent.putExtra("SENDER", allSecretsList.get(position).getUser().getUsername());
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final Secret secret = allSecretsList.get(position);
        secret.setViewed(true);
        adapter.notifyDataSetChanged();

        actionBarDelete.setVisibility(View.VISIBLE);
        //actionBarName.setVisibility(View.GONE);
        actionBarDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete item
                displayConfirmationMessageDialog("Are you sure your want to delete the secret", position, secret);
            }
        });

        return true;
    }

    /**
     * Generic display confirmation pop up
     *
     * @param message - Message to ask
     */
    public void displayConfirmationMessageDialog(String message, final int index, final Secret secret) {
        final Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/GeosansLight.ttf");
        final Dialog dialog = new Dialog(this.getActivity());

        //set layout for dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_confirm_message_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        // set dialog texts
        TextView messageHeaderTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_header_text);
        TextView messageTextView = (TextView) dialog.findViewById(R.id.information_message_dialog_layout_message_text);
        messageHeaderTextView.setText("Confirm delete");
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
                dialog.cancel();

                // delete item
                allSecretsList.remove(index);
                adapter.notifyDataSetChanged();

                // delete from db
                new SenzorsDbSource(getActivity()).deleteAllSecretsThatBelongToUser(secret.getUser().getUsername());

                actionBarDelete.setVisibility(View.GONE);
                //actionBarName.setVisibility(View.VISIBLE);
            }
        });

        // cancel button
        Button cancelButton = (Button) dialog.findViewById(R.id.information_message_dialog_layout_cancel_button);
        cancelButton.setTypeface(typeface, Typeface.BOLD);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
                actionBarDelete.setVisibility(View.GONE);
                //actionBarName.setVisibility(View.VISIBLE);
            }
        });

        dialog.show();
    }

}