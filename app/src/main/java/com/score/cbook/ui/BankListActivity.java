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
import com.score.cbook.enums.ChequeState;
import com.score.cbook.enums.DeliveryState;
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
        bankList.add(new Bank("7056", "Commercial Bank PLC"));
        bankList.add(new Bank("7074", "Habib Bank Ltd"));
        bankList.add(new Bank("7083", "Hatton National Bank PLC"));
        bankList.add(new Bank("7302", "Hongkong   Shanghai Bank"));
        bankList.add(new Bank("7311", "Pan Asia Banking Corporation PLC"));
        bankList.add(new Bank("7384", "ICICI Bank Ltd"));
        bankList.add(new Bank("7454", "DFCC Bank PLC"));
        bankList.add(new Bank("7463", "Amana Bank PLC"));
        bankList.add(new Bank("7472", "Axis Bank"));
        bankList.add(new Bank("7481", "Cargills Bank Limited"));
        bankList.add(new Bank("7719", "National Savings Bank"));
        bankList.add(new Bank("7728", "Sanasa Development Bank"));
        bankList.add(new Bank("7737", "HDFC Bank"));
        bankList.add(new Bank("7746", "Citizen Development Business Finance PLC"));
        bankList.add(new Bank("7755", "Regional Development Bank"));
        bankList.add(new Bank("7764", "State Mortgage&Investment Bank"));
        bankList.add(new Bank("7773", "LB Finance PLC"));
        bankList.add(new Bank("7782", "Senkadagala Finance PLC"));
        bankList.add(new Bank("7807", "Commercial Leasing and Finance"));
        bankList.add(new Bank("7816", "Vallibel Finance PLC"));
        bankList.add(new Bank("7834", "Kanrich Finance Limited"));
        bankList.add(new Bank("7852", "Alliance Finance Company PLC"));
        bankList.add(new Bank("7861", "LOLC Finance PLC"));
        bankList.add(new Bank("7870", "Commercial Credit&Finance PLC"));
        bankList.add(new Bank("7898", "Merchant Bank of Sri Lanka&Finance PLC"));
        bankList.add(new Bank("7904", "HNB Grameen Finance Limited"));
        bankList.add(new Bank("7913", "Mercantile Investment and Finance PLC"));
        bankList.add(new Bank("7922", "People's Leasing & Finance PLC"));
        bankList.add(new Bank("8004", "Central Bank of Sri Lanka"));

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
        else {
            this.cheque = new Cheque();
            cheque.setAccount("231234323489");
            cheque.setAmount("3400");
            cheque.setTimestamp(34232323L);
            cheque.setUid("32342212121");
            cheque.setDeliveryState(DeliveryState.DELIVERED);
            cheque.setChequeState(ChequeState.DEPOSIT);
        }
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
                intent.putExtra("ACCOUNT_BANK", bank);
                intent.putExtra("CHEQUE", cheque);
                startActivity(intent);
                BankListActivity.this.finish();
            }
        });
    }

}
