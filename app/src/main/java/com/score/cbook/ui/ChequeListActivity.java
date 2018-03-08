package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.Cheque;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;


public class ChequeListActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ArrayList<Cheque> chequeList;
    private ChequeListAdapter chequeListAdapter;

    private boolean showMyCheques = false;

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
        setContentView(R.layout.cheque_list_layout);

        initPrefs();
        initToolbar();
        initActionBar();
        initListView();
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
        showMyCheques = getIntent().getBooleanExtra("SHOW_MY_CHEQUES", false);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setCustomView(getLayoutInflater().inflate(R.layout.customer_list_header, null));
        getSupportActionBar().setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // title
        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setText(showMyCheques ? "Sent cheques" : "Received cheques");
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

    private void initListView() {
        ListView listView = (ListView) findViewById(R.id.cheque_list_view);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        chequeList = ChequeSource.getCheques(this, showMyCheques);
        chequeListAdapter = new ChequeListAdapter(this, chequeList);
        chequeListAdapter.notifyDataSetChanged();
        listView.setAdapter(chequeListAdapter);
    }

    private void refreshList() {
        chequeList.clear();
        chequeList.addAll(ChequeSource.getCheques(this, showMyCheques));
        chequeListAdapter.notifyDataSetChanged();
    }

    private boolean needToRefreshList(Senz senz) {
        return senz.getSenzType() == SenzTypeEnum.SHARE && senz.getAttributes().containsKey("cimg");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cheque cheque = chequeList.get(position);

        // open cheque
        //Intent intent = new Intent(ChequeListActivity.this, ViewChequeActivity.class);

        // cheque preview
        Intent intent = new Intent(this, ChequePActivity.class);
        intent.putExtra("UID", cheque.getUid());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final Cheque cheque = chequeList.get(position);
        displayConfirmationMessageDialog("Are you sure your want to remove the cheque", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete item
            }
        });

        return true;
    }

}
