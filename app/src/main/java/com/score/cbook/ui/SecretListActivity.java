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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.SecretSource;
import com.score.cbook.enums.CustomerActionType;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.Secret;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;


public class SecretListActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ArrayList<Secret> secretList;
    private SecretListAdapter secretListAdapter;

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
        setContentView(R.layout.secret_list_layout);

        initToolbar();
        initActionBar();
        initListView();
        initNewButton();
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

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.customer_list_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setText("Messages");
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
        newCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to contact list
                Intent intent = new Intent(SecretListActivity.this, CustomerListActivity.class);
                intent.putExtra("ACTION", CustomerActionType.NEW_MESSAGE.toString());
                startActivity(intent);
            }
        });
    }

    private void initEmptyView() {
        TextView emptyText = (TextView) findViewById(R.id.empty_view_text);
        emptyText.setTypeface(typeface, Typeface.NORMAL);
    }

    private void initListView() {
        ListView listView = (ListView) findViewById(R.id.customer_list_view);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        RelativeLayout emptyView = (RelativeLayout) findViewById(R.id.empty_view);
        if (SecretSource.getRecentSecrets(this).size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            listView.setEmptyView(emptyView);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        secretList = SecretSource.getRecentSecrets(this);
        secretListAdapter = new SecretListAdapter(this, secretList);
        listView.setAdapter(secretListAdapter);
        secretListAdapter.notifyDataSetChanged();
    }

    private void refreshList() {
        secretList.clear();
        secretList.addAll(SecretSource.getRecentSecrets(this));
        secretListAdapter.notifyDataSetChanged();
    }

    private boolean needToRefreshList(Senz senz) {
        return senz.getSenzType() == SenzTypeEnum.DATA &&
                ((senz.getAttributes().containsKey("msg") || senz.getAttributes().containsKey("$msg")));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Secret secret = secretList.get(position);
        Intent intent = new Intent(SecretListActivity.this, ChatActivity.class);
        intent.putExtra("SENDER", secret.getUser().getUsername());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final Secret secret = secretList.get(position);
        displayConfirmationMessageDialog("Confirm", "Are you sure your want to remove the message", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete item
                secretList.remove(position);
                secretListAdapter.notifyDataSetChanged();

                // delete from db
                SecretSource.deleteSecretsOfUser(SecretListActivity.this, secret.getUser().getUsername());
            }
        });

        return true;
    }


}
