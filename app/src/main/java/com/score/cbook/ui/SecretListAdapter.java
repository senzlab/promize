package com.score.cbook.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.pojo.Secret;
import com.score.cbook.util.PhoneBookUtil;
import com.score.cbook.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class SecretListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Secret> userSecretList;
    private Typeface typeface;

    SecretListAdapter(Context _context, ArrayList<Secret> secretList) {
        this.context = _context;
        this.userSecretList = secretList;

        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
    }

    @Override
    public int getCount() {
        return userSecretList.size();
    }

    @Override
    public Object getItem(int position) {
        return userSecretList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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
        final ViewHolder holder;
        final Secret secret = (Secret) getItem(i);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.secret_list_row_layout, viewGroup, false);

            holder = new ViewHolder();
            holder.message = (TextView) view.findViewById(R.id.message);
            holder.sender = (TextView) view.findViewById(R.id.sender);
            holder.sentTime = (TextView) view.findViewById(R.id.sent_time);
            holder.userImage = (ImageView) view.findViewById(R.id.user_image);
            holder.selected = (ImageView) view.findViewById(R.id.selected);
            holder.unreadCount = (FrameLayout) view.findViewById(R.id.unread_msg_count);
            holder.unreadText = (TextView) view.findViewById(R.id.unread_msg_text);

            holder.sender.setTypeface(typeface, Typeface.NORMAL);
            holder.message.setTypeface(typeface, Typeface.NORMAL);
            holder.sentTime.setTypeface(typeface, Typeface.NORMAL);
            holder.unreadText.setTypeface(typeface, Typeface.BOLD);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        setUpRow(secret, holder);
        return view;
    }

    private void setUpRow(Secret secret, ViewHolder viewHolder) {
        if (secret.getUser().getPhone() != null && !secret.getUser().getPhone().isEmpty()) {
            viewHolder.sender.setText(PhoneBookUtil.getContactName(context, secret.getUser().getPhone()));
        } else {
            viewHolder.sender.setText("@" + secret.getUser().getUsername());
        }
        viewHolder.message.setText("Message");

        if (secret.getTimeStamp() != null) {
            viewHolder.sentTime.setText(TimeUtil.getTimeInWords(secret.getTimeStamp()));
        }

        // user image
        Picasso.with(context)
                .load(R.drawable.df_user)
                .placeholder(R.drawable.df_user)
                .into(viewHolder.userImage);

        // selected state
        if (secret.isSelected()) {
            viewHolder.selected.setVisibility(View.VISIBLE);
        } else {
            viewHolder.selected.setVisibility(View.GONE);
        }

        // unread secret count
        if (secret.getUser().getUnreadSecretCount() > 0) {
            viewHolder.sentTime.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            viewHolder.sentTime.setTypeface(typeface, Typeface.BOLD);
            viewHolder.unreadCount.setVisibility(View.VISIBLE);
            viewHolder.unreadText.setText(secret.getUser().getUnreadSecretCount() + "");
        } else {
            viewHolder.sentTime.setTextColor(context.getResources().getColor(R.color.android_grey));
            viewHolder.sentTime.setTypeface(typeface, Typeface.NORMAL);
            viewHolder.unreadCount.setVisibility(View.GONE);
        }
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    private static class ViewHolder {
        TextView message;
        TextView sender;
        TextView sentTime;
        ImageView userImage;
        ImageView selected;
        FrameLayout unreadCount;
        TextView unreadText;
    }
}
