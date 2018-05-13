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
import com.score.cbook.async.PostTask;
import com.score.cbook.db.UserSource;
import com.score.cbook.interfaces.IContactReaderListener;
import com.score.cbook.interfaces.IPostTaskListener;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Contact;
import com.score.cbook.pojo.SenzMsg;
import com.score.cbook.util.ActivityUtil;
import com.score.cbook.util.CryptoUtil;
import com.score.cbook.util.NetworkUtil;
import com.score.cbook.util.SenzParser;
import com.score.cbook.util.SenzUtil;
import com.score.senzc.pojos.Senz;

import java.security.PrivateKey;
import java.util.ArrayList;

public class ContactListActivity extends BaseActivity implements IContactReaderListener, IPostTaskListener {

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
            String confirmationMessage = "<font size=10>Are you sure you want to add </font> <font color=#F37920>" + "<b>" + contact.getName() + "</b>" + "</font> (" + contact.getPhoneNo() + ") as iGift contact?";
            displayConfirmationMessageDialog(confirmationMessage, new View.OnClickListener() {
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
            Toast.makeText(ContactListActivity.this, "This user already added in your iGift contact list", Toast.LENGTH_LONG).show();
        }
    }

    private void addContact(String phoneNo) {
        // create senz
        try {
            Senz senz = SenzUtil.connectSenz(this, phoneNo);
            PrivateKey privateKey = CryptoUtil.getPrivateKey(this);
            String senzPayload = SenzParser.compose(senz);
            String signature = CryptoUtil.getDigitalSignature(senzPayload, privateKey);

            // senz msg
            String uid = senz.getAttributes().get("uid");
            String message = SenzParser.senzMsg(senzPayload, signature);
            SenzMsg senzMsg = new SenzMsg(uid, message);

            PostTask task = new PostTask(this, PostTask.CONNECTION_API, senzMsg);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "POST");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostRead(ArrayList<Contact> contactList) {
        ActivityUtil.cancelProgressDialog();
        initContactList(contactList);
    }

    @Override
    public void onFinishTask(Integer status) {
        if (status == 200) {
            // save contact
            ChequeUser chequeUser = new ChequeUser(selectedContact.getPhoneNo());
            chequeUser.setPhone(selectedContact.getPhoneNo());
            chequeUser.setActive(false);
            chequeUser.setSMSRequester(true);
            UserSource.createUser(this, chequeUser);
            Toast.makeText(this, "Request has been sent", Toast.LENGTH_LONG).show();
        } else {
            ActivityUtil.cancelProgressDialog();
            displayInformationMessageDialog("ERROR", "Fail to add account");
        }
    }
}
