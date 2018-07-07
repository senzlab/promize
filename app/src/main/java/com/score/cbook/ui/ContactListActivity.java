package com.score.cbook.ui;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.score.cbook.R;
import com.score.cbook.async.ContactReader;
import com.score.cbook.async.ContractExecutor;
import com.score.cbook.db.UserSource;
import com.score.cbook.interfaces.IContactReaderListener;
import com.score.cbook.interfaces.IContractExecutorListener;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Contact;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.cbook.util.SmsUtil;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends BaseActivity implements IContactReaderListener, IContractExecutorListener {

    private EditText searchView;

    private ListView contactListView;
    private ContactListAdapter adapter;
    private Contact selectedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        setupToolbar();
        setupActionBar();
        setupSearchView();
        fetchContacts();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        toolbar.setOverScrollMode(Toolbar.OVER_SCROLL_NEVER);
        setSupportActionBar(toolbar);
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.search_contacts_header, null));
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

    private void setupSearchView() {
        searchView = (EditText) findViewById(R.id.inputSearch);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initContactList(ArrayList<Contact> contactList) {
        contactListView = (ListView) findViewById(R.id.contacts_list);
        contactListView.setTextFilterEnabled(true);
        adapter = new ContactListAdapter(this, contactList);
        contactListView.setAdapter(adapter);

        // click listener
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Contact contact = (Contact) adapter.getItem(position);
                onContactItemClick(contact);
            }
        });
    }

    private void fetchContacts() {
        ActivityUtil.showProgressDialog(this, "Loading...");

        ContactReader contactReader = new ContactReader(this, this);
        contactReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void onContactItemClick(final Contact contact) {
        // check existing secret user with given phone no
        if (!UserSource.isExistingUserWithPhoneNo(this, contact.getPhoneNo())) {
            String confirmationMessage = "<font size=10>Are you sure you want to add </font> <font color=#F37920>" + "<b>" + contact.getName() + "</b>" + "</font> (" + contact.getPhoneNo() + ") as igift contact?";
            displayConfirmationMessageDialog("Confirm", confirmationMessage, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.isAvailableNetwork(ContactListActivity.this)) {
                        selectedContact = contact;
                        addContact(selectedContact.getPhoneNo());
                    } else {
                        Toast.makeText(ContactListActivity.this, "No network connection", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(ContactListActivity.this, "This user already added in your igift contact list", Toast.LENGTH_LONG).show();
        }
    }

    private void addContact(String phoneNo) {
        // create senz
        try {
            Senz senz = SenzUtil.connectSenz(this, phoneNo);
            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(senz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);
            String message = SenzParser.senzMsg(senzPayload, signature);

            ActivityUtil.showProgressDialog(ContactListActivity.this, "Requesting...");
            SenzMsg senzMsg = new SenzMsg(senz.getAttributes().get("uid"), message);
            ContractExecutor task = new ContractExecutor(senzMsg, ContactListActivity.this);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmSmsRequest() {
        String message = "<b><font color=#F37920>" + selectedContact.getName() + "</b></font>" + "<font size=10> is not using sampath igift app, would you like to send invitation via SMS?</font>";
        displayConfirmationMessageDialog("Invite", message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send sms
                SmsUtil.iGiftRequest(selectedContact.getPhoneNo());
                Toast.makeText(ContactListActivity.this, "Invitation has been sent via SMS", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPostRead(ArrayList<Contact> contactList) {
        ActivityUtil.cancelProgressDialog();
        initContactList(contactList);
    }

    @Override
    public void onFinishTask(List<Senz> senzes) {
        ActivityUtil.cancelProgressDialog();
        if (senzes.size() == 0) {
            displayInformationMessageDialog("Error", "Fail to send request");
        } else {
            Senz z = senzes.get(0);
            if (z.getAttributes().get("status").equalsIgnoreCase("200")) {
                // sent request
                ChequeUser chequeUser = new ChequeUser(selectedContact.getPhoneNo());
                chequeUser.setPhone(selectedContact.getPhoneNo());
                chequeUser.setActive(false);
                chequeUser.setSMSRequester(true);
                UserSource.createUser(this, chequeUser);
                Toast.makeText(this, "Request has been sent", Toast.LENGTH_LONG).show();
            } else if (z.getAttributes().get("status").equalsIgnoreCase("404")) {
                // user does not exists,
                // ask for sms
                confirmSmsRequest();
            }
        }
    }
}
