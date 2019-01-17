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

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private Cursor mContacts;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View item, int position);
    }

    ContactsAdapter(Cursor contacts, OnItemClickListener listener){
        mContacts = contacts;
        this.listener = listener;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.user_list_item, parent, false);
        return new ContactsViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder holder, int position) {
        if(mContacts.moveToPosition(position)){
            ImageView imageView = (ImageView) holder.linearLayout.findViewById(R.id.profile_photo);
            imageView.setImageBitmap(Bitmap_Byte.getBitmap(mContacts.getBlob(mContacts.getColumnIndex(StringContract.User.COLUMN_PROFILE_PHOTO))));
            TextView name = (TextView) holder.linearLayout.findViewById(R.id.user_name);
            name.setText(mContacts.getString(mContacts.getColumnIndex(StringContract.User.COLUMN_NAME)));
            TextView number = (TextView) holder.linearLayout.findViewById(R.id.last_chat);
            number.setText(mContacts.getString(mContacts.getColumnIndex(StringContract.User.COLUMN_PHONE)));
        }
        holder.bind(holder.linearLayout, listener, position);
    }

    @Override
    public int getItemCount() {
        if(mContacts == null){
            return 0;
        }
        return mContacts.getCount();
    }

    static class ContactsViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;
        ContactsViewHolder(LinearLayout v) {
            super(v);
            linearLayout = v;
        }

        void bind(final View item, final ContactsAdapter.OnItemClickListener listener, final int position) {

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item, position);
                }
            });
        }
    }
}
