package com.score.cbook.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.pojo.ChequeUser;

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

        findViewById(R.id.write_cheque_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to new cheque
                Intent intent = new Intent(DashBoardActivity.this, NewChequeActivity.class);
                intent.putExtra("USER", new ChequeUser("322", "eranga"));
                startActivity(intent);
            }
        });
    }

    private void navigateRegistration() {
        // no user, so move to registration
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
        finish();
    }

}
