package com.score.cbook.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.score.cbook.R;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.util.PhoneBookUtil;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;


class CustomerListAdapter extends ArrayAdapter<ChequeUser> {
    Context context;
    private Typeface typeface;

    CustomerListAdapter(Context _context, LinkedList<ChequeUser> userList) {
        super(_context, R.layout.friend_list_row_layout, R.id.user_name, userList);
        context = _context;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
    }

    /**
     * Create list row view
     *
     * @param i         index
     * @param view      current list item view
     * @param viewGroup parent
     * @return view
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;

        final ChequeUser chequeUser = getItem(i);

        if (view == null) {
            //inflate sensor list row layout
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.friend_list_row_layout, viewGroup, false);

            //create view holder to store reference to child views
            holder = new ViewHolder();
            holder.userImageView = (CircularImageView) view.findViewById(R.id.user_image);
            holder.selected = (ImageView) view.findViewById(R.id.selected);
            holder.usernameView = (TextView) view.findViewById(R.id.user_name);
            holder.phoneNoView = (TextView) view.findViewById(R.id.phoneno);
            holder.statusView = (TextView) view.findViewById(R.id.status);

            view.setTag(holder);
        } else {
            //get view holder back_icon
            holder = (ViewHolder) view.getTag();
        }

        setUpRow(chequeUser, holder);

        return view;
    }

    private void setUpRow(ChequeUser chequeUser, ViewHolder viewHolder) {
        viewHolder.usernameView.setTypeface(typeface, Typeface.NORMAL);
        viewHolder.phoneNoView.setTypeface(typeface, Typeface.NORMAL);
        viewHolder.statusView.setTypeface(typeface, Typeface.BOLD);

        // load contact image
        Picasso.with(context)
                .load(R.drawable.df_user)
                .placeholder(R.drawable.df_user)
                .into(viewHolder.userImageView);

        // request text
        viewHolder.usernameView.setText(PhoneBookUtil.getContactName(context, chequeUser.getPhone()));
        viewHolder.phoneNoView.setText(chequeUser.getPhone());
        if (chequeUser.isActive()) {
            viewHolder.statusView.setVisibility(View.GONE);
        } else {
            viewHolder.statusView.setVisibility(View.VISIBLE);
            viewHolder.statusView.setText(chequeUser.isSMSRequester() ? "Sent Request" : "Received request");
        }

        // selected
        if (chequeUser.isSelected()) {
            viewHolder.selected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selected.setVisibility(View.GONE);
        }
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    private static class ViewHolder {
        CircularImageView userImageView;
        ImageView selected;
        TextView usernameView;
        TextView phoneNoView;
        TextView statusView;
    }

}

