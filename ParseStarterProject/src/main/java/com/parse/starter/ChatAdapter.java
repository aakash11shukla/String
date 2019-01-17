package com.parse.starter;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.starter.Database.StringContract;
import com.parse.starter.Utils.Bitmap_Byte;
import com.parse.starter.Utils.UserMessage;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Cursor mChats;
    private String currentUserId;
    private final OnItemClickListener listener;

    ChatAdapter(Cursor chats, String id, OnItemClickListener listener) {
        mChats = chats;
        currentUserId = id;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View item);
    }

    @Override
    public ChatAdapter.ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.chats_list_item, parent, false);
        return new ChatViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {

        holder.bind(holder.linearLayout, listener);

        if (mChats != null) {
            if (mChats.moveToPosition(position)) {
                TextView textView = (TextView) holder.linearLayout.findViewById(R.id.user_chat);
                ImageView imageView = (ImageView) holder.linearLayout.findViewById(R.id.photo_msg);
                CardView cardView = (CardView) holder.linearLayout.findViewById(R.id.photo_card);
                if (mChats.getString(mChats.getColumnIndex(StringContract.Message.COLUMN_MESSAGE)) == null) {
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    cardView.setVisibility(View.VISIBLE);
                    byte[] array = mChats.getBlob(mChats.getColumnIndex(StringContract.Message.COLUMN_PHOTO));
                    Bitmap bitmap = Bitmap_Byte.getBitmap(array);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        cardView.setVisibility(View.GONE);
                        imageView.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(R.string.error_msg_image);
                        textView.setBackgroundResource(android.R.drawable.screen_background_light_transparent);
                    }
                } else {
                    textView.setText(mChats.getString(mChats.getColumnIndex(StringContract.Message.COLUMN_MESSAGE)));
                }
                if (currentUserId.equals(mChats.getString(mChats.getColumnIndex(StringContract.Message.COLUMN_SENDER)))) {
                    holder.linearLayout.setGravity(Gravity.END);
                } else {
                    holder.linearLayout.setGravity(Gravity.START);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        if (null == mChats || mChats.getCount() == 0)
            return 0;
        return mChats.getCount();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;

        ChatViewHolder(LinearLayout itemView) {
            super(itemView);
            linearLayout = itemView;
        }

        void bind(final View item, final OnItemClickListener listener) {

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
