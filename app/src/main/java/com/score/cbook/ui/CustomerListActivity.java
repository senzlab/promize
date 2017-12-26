package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.utils.ActivityUtils;
import com.score.cbook.utils.NetworkUtil;
import com.score.cbook.utils.PhoneBookUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;


public class CustomerListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ArrayList<ChequeUser> customerList;
    private FriendListAdapter customerListAdapter;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                if (needToRefreshList(senz)) {
                    refreshList();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_list_layout);

        initPrefs();
        initToolbar();
        initActionBar();
        initListView();
        initNewButton();
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindToService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // unbind from service
        if (isServiceBound) {
            unbindService(senzServiceConnection);

            isServiceBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senzReceiver != null) unregisterReceiver(senzReceiver);
    }

    private void initPrefs() {
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.customer_list_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);

        // back button
        ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void initNewButton() {
        // new
        FloatingActionButton newCustomer = (FloatingActionButton) findViewById(R.id.new_customer);
        if (customerList.size() > 0) newCustomer.setVisibility(View.GONE);
        else newCustomer.setVisibility(View.VISIBLE);

        newCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to contact list
                Intent intent = new Intent(CustomerListActivity.this, ContactListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initListView() {
        ListView friendListView = (ListView) findViewById(R.id.customer_list_view);
        friendListView.setOnItemClickListener(this);

        customerList = UserSource.getAllUsers(this);
        if (customerList.size() > 0) {
            customerListAdapter = new FriendListAdapter(this, customerList);
            customerListAdapter.notifyDataSetChanged();
            friendListView.setAdapter(customerListAdapter);
        } else {
            customerListAdapter = new FriendListAdapter(this, customerList);
            friendListView.setAdapter(customerListAdapter);
        }
    }

    private void refreshList() {
        customerList.clear();
        customerList.addAll(UserSource.getAllUsers(this));
        customerListAdapter.notifyDataSetChanged();
    }

    private boolean needToRefreshList(Senz senz) {
        return senz.getSenzType() == SenzTypeEnum.SHARE ||
                senz.getSenzType() == SenzTypeEnum.DATA && (senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("USER_SHARED"));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ChequeUser chequeUser = customerList.get(position);

        if (chequeUser.isActive()) {
            Intent intent = new Intent(CustomerListActivity.this, NewChequeActivity.class);
            intent.putExtra("USER", chequeUser);
            startActivity(intent);
        } else {
            if (chequeUser.isSMSRequester()) {
                String contactName = PhoneBookUtil.getContactName(CustomerListActivity.this, chequeUser.getPhone());
                ActivityUtils.displayConfirmationMessageDialog("Confirm", "Would you like to resend request to " + contactName + "?", CustomerListActivity.this, typeface, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // start sharing again
                        // broadcast
                        Intent intent = new Intent(IntentProvider.ACTION_SMS_REQUEST_CONFIRM);
                        intent.putExtra("USERNAME", chequeUser.getUsername());
                        intent.putExtra("PHONE", chequeUser.getPhone());
                        sendBroadcast(intent);
                        ActivityUtils.showCustomToast("Request sent", CustomerListActivity.this);
                    }
                });
            } else {
                String contactName = PhoneBookUtil.getContactName(CustomerListActivity.this, chequeUser.getPhone());
                ActivityUtils.displayConfirmationMessageDialog("Confirm", "Would you like to accept the request from " + contactName + "?", CustomerListActivity.this, typeface, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // start getting public key and sending confirmation sms
                        // broadcast
                        if (NetworkUtil.isAvailableNetwork(CustomerListActivity.this)) {
                            Intent intent = new Intent(IntentProvider.ACTION_SMS_REQUEST_ACCEPT);
                            intent.putExtra("USERNAME", chequeUser.getUsername());
                            intent.putExtra("PHONE", chequeUser.getPhone());
                            sendBroadcast(intent);
                            ActivityUtils.showCustomToast("Confirmation sent", CustomerListActivity.this);
                        } else {
                            ActivityUtils.showCustomToastShort("No network connection", CustomerListActivity.this);
                        }
                    }
                });
            }
        }

    }

}
