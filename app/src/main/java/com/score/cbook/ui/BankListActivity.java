package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
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

import com.score.cbook.R;
import com.score.cbook.pojo.Bank;
import com.score.cbook.pojo.Cheque;

import java.util.ArrayList;

public class BankListActivity extends BaseActivity {

    private EditText searchView;

    private ListView bankListView;
    private BankListAdapter adapter;
    private Cheque cheque;
    private static ArrayList<Bank> bankList;

    static {
        bankList = new ArrayList<>();
        bankList.add(new Bank("3232", "Sampath bank"));
        bankList.add(new Bank("3232", "Commercial bank"));
        bankList.add(new Bank("3232", "Hatton national bank"));
        bankList.add(new Bank("3232", "BOC"));
        bankList.add(new Bank("3232", "Peoples bank"));
        bankList.add(new Bank("3232", "HSBC"));
        bankList.add(new Bank("3232", "ICIC"));
        bankList.add(new Bank("3232", "DFCC"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);

        setupToolbar();
        setupActionBar();
        setupSearchView();
        initPrefs();
        initList();
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
        titleText.setText("Choose your bank");

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

    private void initPrefs() {
        if (getIntent().hasExtra("CHEQUE"))
            this.cheque = getIntent().getParcelableExtra("CHEQUE");
    }

    private void initList() {
        bankListView = (ListView) findViewById(R.id.contacts_list);
        bankListView.setTextFilterEnabled(true);
        adapter = new BankListAdapter(this, bankList);
        bankListView.setAdapter(adapter);

        // click listener
        bankListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bank bank = (Bank) adapter.getItem(position);
                Intent intent = new Intent(BankListActivity.this, RedeemActivity.class);
                intent.putExtra("BANK", bank);
                intent.putExtra("CHEQUE", cheque);
                startActivity(intent);
                BankListActivity.this.finish();
            }
        });
    }

}
