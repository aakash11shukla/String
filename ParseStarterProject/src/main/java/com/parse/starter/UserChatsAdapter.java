package com.parse.starter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.starter.Database.StringContract;
import com.parse.starter.Utils.Bitmap_Byte;

public class UserChatsAdapter extends RecyclerView.Adapter<UserChatsAdapter.UserChatsViewHolder> {

    private final OnItemClickListener listener;
    private Cursor mCursor;
    private Cursor mChats;

    UserChatsAdapter(Cursor cursor, Cursor chats, OnItemClickListener listener) {
        mCursor = cursor;
        mChats = chats;
        this.listener = listener;
    }

    @Override
    public UserChatsAdapter.UserChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.user_list_item, parent, false);
        return new UserChatsViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(UserChatsAdapter.UserChatsViewHolder holder, int position) {

        holder.bind(holder.mLinearlayout, listener, position);

        ImageView imageView = (ImageView) holder.mLinearlayout.findViewById(R.id.profile_photo);
        TextView name = (TextView) holder.mLinearlayout.findViewById(R.id.user_name);
        TextView lastChat = (TextView) holder.mLinearlayout.findViewById(R.id.last_chat);
        if (mCursor.moveToPosition(position) & mChats.moveToPosition(position)) {

            byte[] outImage = mCursor.getBlob(mCursor.getColumnIndex(StringContract.User.COLUMN_PROFILE_PHOTO));
            imageView.setImageBitmap(Bitmap_Byte.getBitmap(outImage));

            name.setText(mCursor.getString(mCursor.getColumnIndex(StringContract.User.COLUMN_NAME)));
            String msg = mChats.getString(mChats.getColumnIndex(StringContract.Message.COLUMN_MESSAGE));
            if (msg != null) {
                lastChat.setText(msg);
            } else {
                lastChat.setText(R.string.photo);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public interface OnItemClickListener {
        void onItemClick(View item, int position);
    }

    static class UserChatsViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mLinearlayout;

        UserChatsViewHolder(LinearLayout itemView) {
            super(itemView);
            mLinearlayout = itemView;
        }

        void bind(final View item, final OnItemClickListener listener, final int position) {

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item, position);
                }
            });
        }

    }
}
