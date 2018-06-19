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
import java.util.Collections;
import java.util.Comparator;

public class BankListActivity extends BaseActivity {

    private EditText searchView;
    private ListView bankListView;
    private BankListAdapter adapter;
    private Cheque cheque;
    private static ArrayList<Bank> bankList;

    static {
        bankList = new ArrayList<>();
        bankList.add(new Bank("7010", "Bank of Ceylon"));
        bankList.add(new Bank("7038", "Standard Chartered Bank"));
        bankList.add(new Bank("7047", "Citi Bank"));
        bankList.add(new Bank("7056", "Commercial Bank"));
        bankList.add(new Bank("7074", "Habib Bank"));
        bankList.add(new Bank("7083", "HNB - Hatton National Bank"));
        bankList.add(new Bank("7302", "HSBC - Hongkong Shanghai Bank"));
        bankList.add(new Bank("7108", "Indea Bank"));
        bankList.add(new Bank("7384", "ICICI Bank Ltd"));
        bankList.add(new Bank("7117", "Indian Overseas Bank"));
        bankList.add(new Bank("7135", "Peoples Bank"));
        bankList.add(new Bank("7144", "State Bank of India"));
        bankList.add(new Bank("7162", "NTB - Nations Trust Bank"));
        bankList.add(new Bank("7205", "Deutsche Bank"));
        bankList.add(new Bank("7214", "NDB - National Development Bank"));
        bankList.add(new Bank("7269", "MCB Bank"));
        bankList.add(new Bank("7278", "Sampath Bank"));
        bankList.add(new Bank("7287", "Seylan Bank"));
        bankList.add(new Bank("7296", "Public Bank"));
        bankList.add(new Bank("7302", "Union Bank of Colombo"));
        bankList.add(new Bank("7311", "Pan Asia Banking Corporation"));
        bankList.add(new Bank("7384", "ICICI Bank"));
        bankList.add(new Bank("7454", "DFCC Bank"));
        bankList.add(new Bank("7463", "Amana Bank"));
        bankList.add(new Bank("7472", "Axis Bank"));
        bankList.add(new Bank("7481", "Cargills Bank"));
        bankList.add(new Bank("7719", "NSB - National Savings Bank"));
        bankList.add(new Bank("7728", "SDB - Sanasa Development Bank"));
        bankList.add(new Bank("7737", "HDFC Bank"));
        bankList.add(new Bank("7746", "CDB - Citizen Development Business Finance"));
        bankList.add(new Bank("7755", "RDB - Regional Development Bank"));
        bankList.add(new Bank("7764", "State Mortgage & Investment Bank"));
        bankList.add(new Bank("7773", "LB Finance"));
        bankList.add(new Bank("7782", "Senkadagala Finance"));
        bankList.add(new Bank("7807", "Commercial Leasing and Finance"));
        bankList.add(new Bank("7816", "Vallibel Finance"));
        bankList.add(new Bank("7825", "Central Finance"));
        bankList.add(new Bank("7834", "Kanrich Finance"));
        bankList.add(new Bank("7852", "Alliance Finance Company"));
        bankList.add(new Bank("7861", "LOLC Finance"));
        bankList.add(new Bank("7870", "Commercial Credit & Finance"));
        bankList.add(new Bank("7898", "Merchant Bank of Sri Lanka & Finance"));
        bankList.add(new Bank("7904", "HNB Grameen Finance Limited"));
        bankList.add(new Bank("7913", "Mercantile Investment and Finance"));
        bankList.add(new Bank("7922", "People's Leasing & Finance"));
        bankList.add(new Bank("8004", "Central Bank of Sri Lanka"));
        bankList.add(new Bank("6990", "Lank Pay Test"));

        Collections.sort(bankList, new Comparator<Bank>() {
            public int compare(Bank o1, Bank o2) {
                return o1.getBankName().compareTo(o2.getBankName());
            }
        });

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
                onSelectBank(bank);
            }
        });
    }

    private void onSelectBank(final Bank bank) {
        if (bank.getBankCode().equalsIgnoreCase("7278")) {
            navigateRedeem(bank);
        } else {
            String message = "When you redeem iGift for non sampath account, a charge of Rs 50.00 will be debit from your iGift as the commission";
            displayConfirmationMessageDialog("CONFIRM", message, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateRedeem(bank);
                }
            });
        }
    }

    private void navigateRedeem(final Bank bank) {
        Intent intent = new Intent(BankListActivity.this, RedeemActivity.class);
        intent.putExtra("ACCOUNT_BANK", bank);
        intent.putExtra("CHEQUE", cheque);
        startActivity(intent);
        BankListActivity.this.finish();
    }

}
