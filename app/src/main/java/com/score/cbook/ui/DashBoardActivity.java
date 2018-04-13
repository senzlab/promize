package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.db.SecretSource;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.BlobType;
import com.score.cbook.enums.CustomerActionType;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.Account;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Secret;
import com.score.cbook.util.PreferenceUtil;
import com.score.cbook.util.SenzUtil;

public class DashBoardActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_dash_board);
        initUi();
    }

    private void initUi() {
        ((TextView) findViewById(R.id.new_cheque_text)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.customer_text)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.inbox_text)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.messages_text)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.outbox_text)).setTypeface(typeface, Typeface.BOLD);
        ((TextView) findViewById(R.id.support_text)).setTypeface(typeface, Typeface.BOLD);

        onClickWriteCheque();
        onClickReceivedCheques();
        onClickSettings();
        onClickCustomers();
        onClickMessages();
        onClickSupport();
    }

    private void onClickWriteCheque() {
        findViewById(R.id.write_cheque_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = PreferenceUtil.getAccount(DashBoardActivity.this);
                if (account.getAccountNo().isEmpty()) {
                    // account verify
                    Intent intent = new Intent(DashBoardActivity.this, AccountSetupInfoActivity.class);
                    startActivity(intent);
                } else if (account.getState().equalsIgnoreCase("PENDING")) {
                    // salt confirm
                    Intent intent = new Intent(DashBoardActivity.this, SaltConfirmInfoActivity.class);
                    startActivity(intent);
                } else {
                    // navigate to new cheque
                    Intent intent = new Intent(DashBoardActivity.this, CustomerListActivity.class);
                    intent.putExtra("ACTION", CustomerActionType.NEW_CHEQUE.toString());
                    startActivity(intent);
                }
            }
        });
    }

    private void onClickReceivedCheques() {
        findViewById(R.id.inbox_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to new cheque
                Intent intent = new Intent(DashBoardActivity.this, ChequeListTabActivity.class);
                intent.putExtra("SHOW_MY_CHEQUES", false);
                startActivity(intent);
            }
        });
    }

    private void onClickSettings() {
        findViewById(R.id.outbox_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to new cheque
                Intent intent = new Intent(DashBoardActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onClickCustomers() {
        findViewById(R.id.customers_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to customer list
                Intent intent = new Intent(DashBoardActivity.this, CustomerListActivity.class);
                intent.putExtra("ACTION", CustomerActionType.CUSTOMER_LIST.toString());
                startActivity(intent);
            }
        });
    }

    private void onClickMessages() {
        findViewById(R.id.messages_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to customer list
                Intent intent = new Intent(DashBoardActivity.this, SecretListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onClickSupport() {
        findViewById(R.id.support_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChequeUser chequeUser = new ChequeUser(SenzUtil.SAMPATH_SUPPORT_SENZIE_NAME);

                if (!UserSource.isExistingUser(DashBoardActivity.this, SenzUtil.SAMPATH_SUPPORT_SENZIE_NAME)) {
                    // create admin sampath user and secret if not exists
                    chequeUser.setActive(true);
                    chequeUser.setAdmin(true);
                    UserSource.createUser(DashBoardActivity.this, chequeUser);

                    // crate secret with help text
                    Secret secret = new Secret();
                    secret.setMySecret(false);
                    secret.setBlobType(BlobType.TEXT);
                    secret.setBlob("How can we help you? Let us know your problem, we promise you to solve it :) ");
                    secret.setUser(chequeUser);
                    Long timestamp = System.currentTimeMillis() / 1000;
                    secret.setTimeStamp(timestamp);
                    secret.setId(SenzUtil.getUid(DashBoardActivity.this, timestamp.toString()));
                    secret.setDeliveryState(DeliveryState.NONE);

                    SecretSource.createSecret(DashBoardActivity.this, secret);
                }

                // navigate to chat activity
                Intent intent = new Intent(DashBoardActivity.this, ChatActivity.class);
                intent.putExtra("SENDER", chequeUser.getUsername());
                startActivity(intent);
            }
        });
    }

}
