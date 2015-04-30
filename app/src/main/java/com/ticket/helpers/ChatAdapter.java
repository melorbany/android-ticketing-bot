package com.ticket.helpers;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.TimeUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.ticket.media.R;

import java.util.Date;
import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private final List<Message> chatMessages;
    private Activity context;

    public ChatAdapter(Activity context, List<Message> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public Message getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Message chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int type  = chatMessage.getType();

        switch (type) {
            case 1:
                convertView = vi.inflate(R.layout.list_item_image, null);
                break;
            case 2:
                convertView = vi.inflate(R.layout.list_item_video, null);
                break;
            default:
                convertView = vi.inflate(R.layout.list_item_message, null);
        }

        holder = createViewHolder(convertView);
        convertView.setTag(holder);

        boolean isOutgoing = chatMessage.isSender();
        setAlignment(holder, isOutgoing,type);

        switch (type) {
            case 1:
                holder.imgMessage.setImageURI(chatMessage.getUri());
                break;
            case 2:
                holder.videoMessage.setVideoURI(chatMessage.getUri());
                break;
            default:
                holder.txtMessage.setText(chatMessage.getData());
        }



//        if (chatMessage.isSender()) {
//            holder.txtInfo.setText("["+getTimeText(chatMessage)+"] You:");
//        } else {
//            holder.txtInfo.setText("Server Response");
//        }

        return convertView;
    }

    public void add(Message message) {
        chatMessages.add(message);
    }

    public void add(List<Message> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isOutgoing,int type) {
        if (isOutgoing) {
            holder.contentWithBG.setBackgroundResource(R.drawable.incoming_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);

            switch (type) {
                case 1:
                    layoutParams = (LinearLayout.LayoutParams) holder.imgMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.RIGHT;
                    holder.imgMessage.setLayoutParams(layoutParams);
                    break;
                case 2:
                    layoutParams = (LinearLayout.LayoutParams) holder.videoMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.RIGHT;
                    holder.videoMessage.setLayoutParams(layoutParams);
                    break;
                default:
                    layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.RIGHT;
                    holder.txtMessage.setLayoutParams(layoutParams);
            }


            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.outgoing_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);

            switch (type) {
                case 1:
                    layoutParams = (LinearLayout.LayoutParams) holder.imgMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT;
                    holder.imgMessage.setLayoutParams(layoutParams);
                    break;
                case 2:
                    layoutParams = (LinearLayout.LayoutParams) holder.videoMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT;
                    holder.videoMessage.setLayoutParams(layoutParams);
                    break;
                default:
                    layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT;
                    holder.txtMessage.setLayoutParams(layoutParams);
            }

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.imgMessage = (ImageView) v.findViewById(R.id.imgMessage);
        holder.videoMessage = (VideoView) v.findViewById(R.id.videoMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }

    private String getTimeText(Message message) {
        return message.getDateSent().toString();
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public ImageView imgMessage;
        public VideoView videoMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}
