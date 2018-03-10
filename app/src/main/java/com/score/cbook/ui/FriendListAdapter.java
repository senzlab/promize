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

import java.util.ArrayList;


class FriendListAdapter extends ArrayAdapter<ChequeUser> {
    Context context;
    private Typeface typeface;

    FriendListAdapter(Context _context, ArrayList<ChequeUser> userList) {
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
            holder.phoneBookNameView = (TextView) view.findViewById(R.id.user_name_from_contacts);
            holder.userLocationPermView = (ImageView) view.findViewById(R.id.perm_locations);
            holder.userCameraPermView = (ImageView) view.findViewById(R.id.perm_camera);

            view.setTag(holder);
        } else {
            //get view holder back_icon
            holder = (ViewHolder) view.getTag();
        }

        setUpRow(i, chequeUser, view, holder);

        return view;
    }

    private void setUpRow(int i, ChequeUser chequeUser, View view, ViewHolder viewHolder) {
        viewHolder.usernameView.setTypeface(typeface, Typeface.NORMAL);
        viewHolder.phoneBookNameView.setTypeface(typeface, Typeface.NORMAL);

        // load contact image
        Picasso.with(context)
                .load(R.drawable.df_user)
                .placeholder(R.drawable.df_user)
                .into(viewHolder.userImageView);

        // request text
        if (chequeUser.isActive()) {
            viewHolder.usernameView.setText(PhoneBookUtil.getContactName(context, chequeUser.getPhone()));
            viewHolder.phoneBookNameView.setVisibility(View.GONE);
        } else {
            viewHolder.usernameView.setText(PhoneBookUtil.getContactName(context, chequeUser.getPhone()));
            viewHolder.phoneBookNameView.setText(chequeUser.isSMSRequester() ? "Sent request" : "Received request");
            viewHolder.phoneBookNameView.setVisibility(View.VISIBLE);
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
        TextView phoneBookNameView;
        ImageView userCameraPermView;
        ImageView userLocationPermView;
    }

}
