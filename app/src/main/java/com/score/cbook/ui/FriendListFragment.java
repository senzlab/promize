package com.score.cbook.ui;


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

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.utils.ActivityUtils;
import com.score.cbook.utils.NetworkUtil;
import com.score.cbook.utils.PhoneBookUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;


public class FriendListFragment extends ListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = FriendListFragment.class.getName();

    private ActionBar actionBar;
    private ImageView actionBarDelete;

    private ArrayList<ChequeUser> friendsList;
    private FriendListAdapter adapter;

    private Typeface typeface;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got new user from Senz service");

            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                if (needToRefreshList(senz)) {
                    refreshList();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.friend_list_fragment_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initUi();
        initActionBar();
        initList();
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

    private void initUi() {
        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/GeosansLight.ttf");
        ((TextView) getActivity().findViewById(R.id.empty_view_friend)).setTypeface(typeface);
    }

    private void initActionBar() {
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBarDelete = (ImageView) actionBar.getCustomView().findViewById(R.id.delete);
        actionBarDelete.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ChequeUser chequeUser = friendsList.get(position);
        actionBarDelete.setVisibility(View.GONE);

        if (chequeUser.isSelected()) {
            chequeUser.setSelected(false);
            adapter.notifyDataSetChanged();
            actionBarDelete.setVisibility(View.GONE);
        } else {
            if (chequeUser.isActive()) {
                Intent intent = new Intent(this.getActivity(), NewChequeActivity.class);
                intent.putExtra("USER", chequeUser);
                startActivity(intent);
            } else {
                if (chequeUser.isSMSRequester()) {
                    String contactName = PhoneBookUtil.getContactName(getActivity(), chequeUser.getPhone());
                    ActivityUtils.displayConfirmationMessageDialog("Confirm", "Would you like to resend request to " + contactName + "?", getActivity(), typeface, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // start sharing again
                            // broadcast
                            Intent intent = new Intent(IntentProvider.ACTION_SMS_REQUEST_CONFIRM);
                            intent.putExtra("USERNAME", chequeUser.getUsername());
                            intent.putExtra("PHONE", chequeUser.getPhone());
                            getActivity().sendBroadcast(intent);
                            ActivityUtils.showCustomToast("Request sent", getActivity());
                        }
                    });
                } else {
                    String contactName = PhoneBookUtil.getContactName(getActivity(), chequeUser.getPhone());
                    ActivityUtils.displayConfirmationMessageDialog("Confirm", "Would you like to accept the request from " + contactName + "?", getActivity(), typeface, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // start getting public key and sending confirmation sms
                            // broadcast
                            if (NetworkUtil.isAvailableNetwork(getActivity())) {
                                Intent intent = new Intent(IntentProvider.ACTION_SMS_REQUEST_ACCEPT);
                                intent.putExtra("USERNAME", chequeUser.getUsername());
                                intent.putExtra("PHONE", chequeUser.getPhone());
                                getActivity().sendBroadcast(intent);
                                ActivityUtils.showCustomToast("Confirmation sent", getActivity());
                            } else {
                                ActivityUtils.showCustomToastShort("No network connection", getActivity());
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final ChequeUser chequeUser = friendsList.get(position);

        actionBarDelete.setVisibility(View.VISIBLE);
        chequeUser.setSelected(true);
        adapter.notifyDataSetChanged();

        actionBarDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete item
                displayConfirmationMessageDialog("Are you sure your want to remove the user", position, chequeUser);
            }
        });

        return true;
    }

    /**
     * Display sensor list
     * Basically setup list adapter if have items to display otherwise display empty view
     */
    private void initList() {
        // get User from db
        friendsList = UserSource.getAllUsers(this.getContext());
        // construct list adapter
        if (friendsList.size() > 0) {
            adapter = new FriendListAdapter(getContext(), friendsList);
            adapter.notifyDataSetChanged();
            getListView().setAdapter(adapter);
        } else {
            adapter = new FriendListAdapter(getContext(), friendsList);
            getListView().setAdapter(adapter);
        }
    }

    private void refreshList() {
        friendsList.clear();
        friendsList.addAll(UserSource.getAllUsers(this.getContext()));
        adapter.notifyDataSetChanged();
    }

    private boolean needToRefreshList(Senz senz) {
        return senz.getSenzType() == SenzTypeEnum.SHARE ||
                senz.getSenzType() == SenzTypeEnum.DATA && (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("USER_SHARED"));
    }

    public void displayConfirmationMessageDialog(String message, final int index, final ChequeUser chequeUser) {
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
        messageHeaderTextView.setText("Confirm remove");
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
                friendsList.remove(index);
                adapter.notifyDataSetChanged();

                // delete from db
                UserSource.deleteUser(FriendListFragment.this.getContext(), chequeUser.getUsername());
                ChequeSource.deleteChequesOfUser(FriendListFragment.this.getContext(), chequeUser.getUsername());
                SecretSource.deleteSecretsOfUser(FriendListFragment.this.getContext(), chequeUser.getUsername());

                actionBarDelete.setVisibility(View.GONE);

                // TODO send unshare message
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
