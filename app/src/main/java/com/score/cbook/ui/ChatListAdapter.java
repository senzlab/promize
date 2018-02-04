package com.score.cbook.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.score.cbook.R;
import com.score.cbook.db.UserSource;
import com.score.cbook.enums.BlobType;
import com.score.cbook.enums.DeliveryState;
import com.score.cbook.pojo.ChequeUser;
import com.score.cbook.pojo.Secret;
import com.score.cbook.util.LimitedList;
import com.score.cbook.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by eranga on 9/28/16
 */
class ChatListAdapter extends BaseAdapter {

    private Context context;
    private ChequeUser chequeUser;
    private LimitedList<Secret> secretList;

    private Typeface typeface;

    private static final int MY_CHAT_ITEM = 0;
    private static final int FRIEND_CHAT_ITEM = 1;
    private static final int MAX_TYPE_COUNT = 2;

    ChatListAdapter(Context context, ChequeUser user, LimitedList<Secret> secretList) {
        this.context = context;
        this.chequeUser = user;
        this.secretList = secretList;

        // reset users unread secret count
        UserSource.resetUnreadSecretCount(context, chequeUser.getUsername());

        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/GeosansLight.ttf");
    }

    @Override
    public int getCount() {
        return secretList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ((Secret) getItem(position)).isMySecret() ? MY_CHAT_ITEM : FRIEND_CHAT_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return MAX_TYPE_COUNT;
    }

    @Override
    public Object getItem(int position) {
        return secretList.get(position);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        // reset users unread secret count
        UserSource.resetUnreadSecretCount(context, chequeUser.getUsername());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        final Secret secret = secretList.get(position);
        final int type = getItemViewType(position);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();

            switch (type) {
                case MY_CHAT_ITEM:
                    view = layoutInflater.inflate(R.layout.my_chat_view_row_layout, parent, false);
                    holder.chatCamHolder = (RelativeLayout) view.findViewById(R.id.chat_cam_holder);
                    holder.chatMisHolder = (RelativeLayout) view.findViewById(R.id.chat_mis_holder);
                    holder.chatMsgHolder = (LinearLayout) view.findViewById(R.id.chat_msg_holder);

                    holder.chatCam = (ImageView) view.findViewById(R.id.chat_cam);
                    holder.chatMsg = (TextView) view.findViewById(R.id.chat_msg);
                    holder.chatMis = (RelativeLayout) view.findViewById(R.id.chat_mis);
                    holder.missedSecret = (TextView) view.findViewById(R.id.missed_secret);

                    holder.chatStatus = (FrameLayout) view.findViewById(R.id.chat_status);
                    holder.chatTime = (TextView) view.findViewById(R.id.chat_time);
                    holder.chatDelivered = (ImageView) view.findViewById(R.id.chat_delivered);
                    holder.chatPending = (ImageView) view.findViewById(R.id.chat_pending);

                    break;
                case FRIEND_CHAT_ITEM:
                    view = layoutInflater.inflate(R.layout.friend_chat_view_row_layout, parent, false);
                    holder.chatCamHolder = (RelativeLayout) view.findViewById(R.id.chat_cam_holder);
                    holder.chatMisHolder = (RelativeLayout) view.findViewById(R.id.chat_mis_holder);
                    holder.chatMsgHolder = (LinearLayout) view.findViewById(R.id.chat_msg_holder);

                    holder.chatCam = (ImageView) view.findViewById(R.id.chat_cam);
                    holder.chatMsg = (TextView) view.findViewById(R.id.chat_msg);
                    holder.chatMis = (RelativeLayout) view.findViewById(R.id.chat_mis);
                    holder.missedSecret = (TextView) view.findViewById(R.id.missed_secret);

                    holder.chatStatus = (FrameLayout) view.findViewById(R.id.chat_status);
                    holder.chatTime = (TextView) view.findViewById(R.id.chat_time);
                    holder.chatDelivered = (ImageView) view.findViewById(R.id.chat_delivered);
                    holder.chatPending = (ImageView) view.findViewById(R.id.chat_pending);

                    break;
            }

            holder.chatMsg.setTypeface(typeface, Typeface.BOLD);
            holder.chatTime.setTypeface(typeface);
            holder.missedSecret.setTypeface(typeface, Typeface.BOLD);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        setupRow(secret, holder);
        return view;
    }

    private void setupRow(final Secret secret, ViewHolder holder) {
        if (secret.getBlobType() == BlobType.TEXT) {
            holder.chatCamHolder.setVisibility(View.GONE);
            holder.chatMisHolder.setVisibility(View.GONE);
            holder.chatMsgHolder.setVisibility(View.VISIBLE);

            holder.chatMsg.setText(secret.getBlob());
        } else if (secret.getBlobType() == BlobType.IMAGE) {
            holder.chatCamHolder.setVisibility(View.VISIBLE);
            holder.chatMisHolder.setVisibility(View.GONE);
            holder.chatMsgHolder.setVisibility(View.GONE);

            // load thumbnail
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Rahasak/" + secret.getId() + ".jpg");
            Picasso.with(context)
                    .load(file)
                    .resize(150, 150)
                    .centerCrop()
                    .into(holder.chatCam);
        } else if (secret.getBlobType() == BlobType.MISSED_SELFIE) {
            // mis selfie
            holder.chatCamHolder.setVisibility(View.GONE);
            holder.chatMisHolder.setVisibility(View.VISIBLE);
            holder.chatMsgHolder.setVisibility(View.GONE);

            //holder.chatMis.setBackgroundResource(R.drawable.mis_selfie_bg);
            holder.missedSecret.setText("missed selfie");
        } else if (secret.getBlobType() == BlobType.MISSED_CALL) {
            // mis call
            holder.chatCamHolder.setVisibility(View.GONE);
            holder.chatMisHolder.setVisibility(View.VISIBLE);
            holder.chatMsgHolder.setVisibility(View.GONE);

            //holder.chatMis.setBackgroundResource(R.drawable.mis_call_bg);
            holder.missedSecret.setText("missed call");
        }

        if (secret.isMySecret()) {
            if (secret.getDeliveryState() == DeliveryState.DELIVERED) {
                holder.chatDelivered.setVisibility(View.VISIBLE);
                holder.chatPending.setVisibility(View.GONE);
                holder.chatTime.setVisibility(View.VISIBLE);
            } else if (secret.getDeliveryState() == DeliveryState.RECEIVED) {
                holder.chatDelivered.setVisibility(View.GONE);
                holder.chatPending.setVisibility(View.VISIBLE);
                holder.chatTime.setVisibility(View.VISIBLE);
            } else {
                holder.chatDelivered.setVisibility(View.GONE);
                holder.chatPending.setVisibility(View.GONE);
                holder.chatTime.setVisibility(View.VISIBLE);
            }
        } else {
            holder.chatDelivered.setVisibility(View.GONE);
            holder.chatPending.setVisibility(View.GONE);
        }

        if (secret.getTimeStamp() != null) {
            holder.chatTime.setText(TimeUtil.getTimeInWords(secret.getTimeStamp()));
        }

        // disable status panel if secret time is aligned(in range of 5 mins) with previous secret
        if (secret.isInOrder()) {
            holder.chatStatus.setVisibility(View.GONE);
        } else {
            holder.chatStatus.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    private static class ViewHolder {
        RelativeLayout chatCamHolder;
        RelativeLayout chatMisHolder;
        LinearLayout chatMsgHolder;

        ImageView chatCam;
        TextView chatMsg;
        RelativeLayout chatMis;
        TextView missedSecret;

        FrameLayout chatStatus;
        TextView chatTime;
        ImageView chatDelivered;
        ImageView chatPending;
    }

}
