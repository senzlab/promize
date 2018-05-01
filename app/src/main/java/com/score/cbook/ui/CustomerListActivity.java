package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.CustomerActionType;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PhoneBookUtil;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.LinkedList;


public class CustomerListActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private LinkedList<ChequeUser> customerList;
    private CustomerListAdapter customerListAdapter;

    private CustomerActionType actionType = CustomerActionType.CUSTOMER_LIST;

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
        initSearchView();
        initEmptyView();
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
        refreshList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (senzReceiver != null) unregisterReceiver(senzReceiver);
    }

    private void initPrefs() {
        String action = getIntent().getStringExtra("ACTION");
        if (action != null && !action.isEmpty()) {
            actionType = CustomerActionType.valueOf(action);
        }
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.customer_list_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        if (UserSource.getAllUsers(this).size() == 0) {
            titleText.setText("Add contact");
        } else if (actionType == CustomerActionType.NEW_CHEQUE || actionType == CustomerActionType.NEW_MESSAGE) {
            titleText.setText("Choose contact");
        } else {
            titleText.setText("iGift contacts");
        }

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
        newCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to contact list
                Intent intent = new Intent(CustomerListActivity.this, ContactListActivity.class);
                startActivity(intent);
            }
        });

        if (actionType == CustomerActionType.CUSTOMER_LIST) {
            newCustomer.setVisibility(View.VISIBLE);
        } else {
            if (customerList.size() > 0) newCustomer.setVisibility(View.GONE);
            else newCustomer.setVisibility(View.VISIBLE);
        }
    }

    private void initSearchView() {
        LinearLayout search = (LinearLayout) findViewById(R.id.search_layout);
        EditText searchView = (EditText) findViewById(R.id.inputSearch);
        if (actionType == CustomerActionType.CUSTOMER_LIST || customerList.size() == 0) {
            search.setVisibility(View.GONE);
        } else {
            search.setVisibility(View.VISIBLE);
            searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    customerListAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private void initEmptyView() {
        TextView emptyText = (TextView) findViewById(R.id.empty_view_text);
        emptyText.setTypeface(typeface, Typeface.NORMAL);
    }

    private void initListView() {
        ListView friendListView = (ListView) findViewById(R.id.customer_list_view);
        friendListView.setOnItemClickListener(this);
        friendListView.setOnItemLongClickListener(this);

        RelativeLayout emptyView = (RelativeLayout) findViewById(R.id.empty_view);
        if (UserSource.getAllUsers(this).size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            friendListView.setEmptyView(emptyView);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        customerList = UserSource.getAllUsers(this);
        if (customerList.size() > 0) {
            customerListAdapter = new CustomerListAdapter(this, customerList);
            customerListAdapter.notifyDataSetChanged();
            friendListView.setAdapter(customerListAdapter);
        } else {
            customerListAdapter = new CustomerListAdapter(this, customerList);
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
            if (actionType == CustomerActionType.NEW_CHEQUE) {
                Intent intent = new Intent(CustomerListActivity.this, NewPromizeActivity.class);
                intent.putExtra("USER", chequeUser);
                startActivity(intent);
            } else if (actionType == CustomerActionType.NEW_MESSAGE) {
                Intent intent = new Intent(CustomerListActivity.this, ChatActivity.class);
                intent.putExtra("SENDER", chequeUser.getUsername());
                startActivity(intent);
            } else if (actionType == CustomerActionType.CUSTOMER_LIST) {
                Intent intent = new Intent(CustomerListActivity.this, UserProfileActivity.class);
                intent.putExtra("SECRET_USER", chequeUser);
                startActivity(intent);
            }
        } else {
            if (chequeUser.isSMSRequester()) {
                String contactName = PhoneBookUtil.getContactName(CustomerListActivity.this, chequeUser.getPhone());
                displayConfirmationMessageDialog("Would you like to resend request to " + contactName + "?", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // start sharing again
                        // broadcast
                        Intent intent = new Intent(IntentProvider.ACTION_SMS_REQUEST_CONFIRM);
                        intent.putExtra("USERNAME", chequeUser.getUsername());
                        intent.putExtra("PHONE", chequeUser.getPhone());
                        sendBroadcast(intent);
                        Toast.makeText(CustomerListActivity.this, "Request sent", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                String contactName = PhoneBookUtil.getContactName(CustomerListActivity.this, chequeUser.getPhone());
                displayConfirmationMessageDialog("Would you like to accept the request from " + contactName + "?", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // start getting public key and sending confirmation sms
                        // broadcast
                        if (NetworkUtil.isAvailableNetwork(CustomerListActivity.this)) {
                            Intent intent = new Intent(IntentProvider.ACTION_SMS_REQUEST_ACCEPT);
                            intent.putExtra("USERNAME", chequeUser.getUsername());
                            intent.putExtra("PHONE", chequeUser.getPhone());
                            sendBroadcast(intent);
                            Toast.makeText(CustomerListActivity.this, "Confirmation sent", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CustomerListActivity.this, "No network connection", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final ChequeUser chequeUser = customerList.get(position);

        if (ChequeSource.hasChequesToRedeem(CustomerListActivity.this, chequeUser.getUsername())) {
            displayConfirmationMessageDialog("Are you sure your want to remove the user", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // delete item
                    customerList.remove(position);
                    customerListAdapter.notifyDataSetChanged();

                    // delete from db
                    UserSource.deleteUser(CustomerListActivity.this, chequeUser.getUsername());
                    ChequeSource.deleteChequesOfUser(CustomerListActivity.this, chequeUser.getUsername());
                    SecretSource.deleteSecretsOfUser(CustomerListActivity.this, chequeUser.getUsername());
                }
            });
        }

        return true;
    }


}
