package com.score.cbook.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.pojo.Bank;

import java.util.ArrayList;

class BankListAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private ListFilter listFilter;
    private ArrayList<Bank> bankList;
    private ArrayList<Bank> filteredList;

    private Typeface typeface;

    BankListAdapter(Context context, ArrayList<Bank> bankList) {
        this.context = context;
        this.bankList = bankList;
        this.filteredList = bankList;

        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;

        final Bank bank = (Bank) getItem(position);

        if (convertView == null) {
            //inflate sensor list row layout
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.bank_list_row_layout, parent, false);

            //create view holder to store reference to child views
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.bank_name);

            convertView.setTag(holder);
        } else {
            //get view holder back_icon
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(bank.getBankName());
        holder.name.setTypeface(typeface);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (listFilter == null) {
            listFilter = new ListFilter();
        }

        return listFilter;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView name;
    }

    /**
     * Custom filter for contact list
     * Filter content in contact list according to the search text
     */
    private class ListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Bank> tempList = new ArrayList<>();

                // search content in friend list
                for (Bank bank : bankList) {
                    if (bank.getBankName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(bank);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = bankList.size();
                filterResults.values = bankList;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         *
         * @param constraint text
         * @param results    filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<Bank>) results.values;
            notifyDataSetChanged();
        }
    }

}
