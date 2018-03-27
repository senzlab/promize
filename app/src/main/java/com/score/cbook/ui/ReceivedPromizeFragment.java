package com.score.cbook.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.application.IntentProvider;
import com.score.cbook.db.ChequeSource;
import com.score.cbook.enums.IntentType;
import com.score.cbook.pojo.Cheque;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;

public class ReceivedPromizeFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cheque_list_layout, container, false);
        initEmptyView(view);
        initListView(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
        refreshList();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (senzReceiver != null) getActivity().unregisterReceiver(senzReceiver);
    }

    private void initEmptyView(View view) {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/GeosansLight.ttf");
        TextView emptyText = (TextView) view.findViewById(R.id.empty_view_text);
        emptyText.setTypeface(typeface, Typeface.NORMAL);
        emptyText.setText("There aren't any received iGifts yet");
    }

    private void initListView(View view) {
        ListView listView = (ListView) view.findViewById(R.id.cheque_list_view);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        chequeList = ChequeSource.getCheques(getActivity(), showMyCheques);

        RelativeLayout emptyView = (RelativeLayout) view.findViewById(R.id.empty_view);
        if (chequeList.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            listView.setEmptyView(emptyView);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        chequeListAdapter = new ChequeListAdapter(getActivity(), chequeList);
        chequeListAdapter.notifyDataSetChanged();
        listView.setAdapter(chequeListAdapter);
    }

    private void refreshList() {
        chequeList.clear();
        chequeList.addAll(ChequeSource.getCheques(getActivity(), showMyCheques));
        chequeListAdapter.notifyDataSetChanged();
    }

    private boolean needToRefreshList(Senz senz) {
        return senz.getSenzType() == SenzTypeEnum.SHARE && senz.getAttributes().containsKey("cimg");
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cheque cheque = chequeList.get(position);

        // open cheque
        Intent intent = new Intent(getActivity(), ChequePActivity.class);
        intent.putExtra("CHEQUE", cheque);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final Cheque cheque = chequeList.get(position);
//        displayConfirmationMessageDialog("Are you sure your want to remove the cheque", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // delete item
//            }
//        });

        return true;
    }
}
