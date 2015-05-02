package com.ticket.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TimeUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.ticket.media.ImageDetailActivity;
import com.ticket.media.R;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter {

    public final List<Message> chatMessages;
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

    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {

        Intent intent = new Intent();
        intent.setClass(context, ImageDetailActivity.class);

        Message message = chatMessages.get(position);
        intent.putExtra("EXTRA_IMAGE", message.getPath());

        // the sample activity
        context.startActivity(intent);
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

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_message, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        boolean isOutgoing = chatMessage.isSender();
        setAlignment(holder, isOutgoing,type);

        switch (type) {
            case 1:
                holder.imgMessage.setVisibility(View.VISIBLE);
                Image image = new Image();
                Bitmap d = image.decodeSampledBitmapFromFile(chatMessage.getPath(),1000,1000);
                holder.imgMessage.setImageBitmap(d);
                holder.videoMessage.setVisibility(View.GONE);
                holder.txtMessage.setVisibility(View.GONE);
                break;
            case 2:
                holder.videoMessage.setVisibility(View.VISIBLE);
                holder.videoMessage.setVideoURI(chatMessage.getUri());
                holder.imgMessage.setVisibility(View.GONE);
                holder.txtMessage.setVisibility(View.GONE);
                break;
            default:
                holder.txtMessage.setVisibility(View.VISIBLE);
                holder.txtMessage.setText(chatMessage.getData());
                holder.videoMessage.setVisibility(View.GONE);
                holder.imgMessage.setVisibility(View.GONE);
                break;
        }

//        Fragment fragment = new MessageFragment();
//
//        Bundle args = new Bundle();
//        args.putInt(MessageFragment.TYPE, chatMessage.getType());
//        args.putString(MessageFragment.DATA, chatMessage.getData());
//        fragment.setArguments(args);
//
//
//        FragmentManager fragmentManager = context.getFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
//

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


//            layoutParams = (LinearLayout.LayoutParams) holder.content_frame.getLayoutParams();
//            layoutParams.gravity = Gravity.RIGHT;
//            holder.content_frame.setLayoutParams(layoutParams);


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

//            layoutParams = (LinearLayout.LayoutParams) holder.content_frame.getLayoutParams();
//            layoutParams.gravity = Gravity.LEFT;
//            holder.content_frame.setLayoutParams(layoutParams);


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
        holder.content_frame = (FrameLayout) v.findViewById(R.id.content_frame);
        return holder;
    }

    private String getTimeText(Message message) {
        return message.getDateSent().toString();
    }

    private static class ViewHolder {
        public FrameLayout content_frame;
        public TextView txtMessage;
        public ImageView imgMessage;
        public VideoView videoMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }



    @SuppressLint("ValidFragment")
    public static class MessageFragment extends Fragment {
        public static final String TYPE = "type";
        public static final String DATA = "data";

        Message message;
        @SuppressLint("ValidFragment")
        public MessageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //Log.i('Tag','onCreateView');
            View rootView = null;
            int type = getArguments().getInt(TYPE);

            switch (type){
                case 1:
                   rootView = inflater.inflate(R.layout.fragment_image, container, false);
                   ((ImageView) rootView.findViewById(R.id.imgMessage)).setImageURI(message.getUri());
                    break;

                default:
                    rootView = inflater.inflate(R.layout.fragment_text, container, false);
                    String data = getArguments().getString(DATA);

                    Log.d("tt",data);

                    ((TextView) rootView.findViewById(R.id.txtMessage)).setText(data);
                    break;
            }

            return rootView;
        }

    }


}
