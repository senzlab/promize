package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
import com.score.cbook.async.SenzPublisher;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.CustomerActionType;
import com.score.cbook.interfaces.ISenzPublisherListener;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.PhoneBookUtil;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;
import java.util.LinkedList;


public class CustomerListActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ISenzPublisherListener {

    private LinkedList<ChequeUser> customerList;
    private CustomerListAdapter customerListAdapter;

    private CustomerActionType actionType = CustomerActionType.CUSTOMER_LIST;
    private ChequeUser selectedUser;

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
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void initPrefs() {
        String action = getIntent().getStringExtra("ACTION");
        if (action != null && !action.isEmpty()) {
            actionType = CustomerActionType.valueOf(action);
        }
        customerList = UserSource.getAllUsers(this);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.customer_list_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setTypeface(typeface, Typeface.BOLD);
        if (customerList.size() == 0) {
            titleText.setText("Add contact");
        } else if (actionType == CustomerActionType.NEW_CHEQUE) {
            titleText.setText("Choose contact");
        } else {
            titleText.setText("iGift Contacts");
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
        if (customerList.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            friendListView.setEmptyView(emptyView);
        } else {
            emptyView.setVisibility(View.GONE);
        }

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ChequeUser chequeUser = customerList.get(position);

        if (chequeUser.isActive()) {
            if (PreferenceUtil.get(CustomerListActivity.this, PreferenceUtil.ACCOUNT_STATE).equalsIgnoreCase("VERIFIED") && actionType == CustomerActionType.NEW_CHEQUE) {
                Intent intent = new Intent(CustomerListActivity.this, NewPromizeActivity.class);
                intent.putExtra("USER", chequeUser);
                startActivity(intent);
            }
        } else {
            if (chequeUser.isSMSRequester()) {
                //String contactName = PhoneBookUtil.getContactName(CustomerListActivity.this, chequeUser.getPhone());
                //displayConfirmationMessageDialog("CONFIRM", "Would you like to resend request to " + contactName + "?", new View.OnClickListener() {
                //    @Override
                //    public void onClick(View v) {
                //
                //    }
                //});
            } else {
                String contactName = PhoneBookUtil.getContactName(CustomerListActivity.this, chequeUser.getPhone());
                displayConfirmationMessageDialog("CONFIRM", "Would you like to accept the request from " + contactName + "?", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // start getting public key and sending confirmation sms
                        // broadcast
                        if (NetworkUtil.isAvailableNetwork(CustomerListActivity.this)) {
                            selectedUser = chequeUser;
                            addContact(selectedUser.getPhone());
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

        if (!ChequeSource.hasChequesToRedeem(CustomerListActivity.this, chequeUser.getUsername())) {
            displayConfirmationMessageDialog("CONFIRM", "Are you sure your want to remove the user", new View.OnClickListener() {
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

    private void addContact(String phoneNo) {
        // create senz
        try {
            Senz senz = SenzUtil.connectSenz(this, phoneNo);
            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(senz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);
            String message = SenzParser.senzMsg(senzPayload, signature);

            ActivityUtil.showProgressDialog(CustomerListActivity.this, "Accepting...");
            SenzPublisher task = new SenzPublisher(this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinish(String senz) {
        ActivityUtil.cancelProgressDialog();
        if (senz == null) {
            displayInformationMessageDialog("ERROR", "Fail to add contact");
        } else {
            // activate user
            UserSource.activateUser(this, selectedUser.getUsername());
            refreshList();
            Toast.makeText(this, "Successfully added contact", Toast.LENGTH_LONG).show();
        }
    }
}
