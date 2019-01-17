package com.parse.starter;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.starter.Database.StringContract;
import com.parse.starter.Database.StringDbHelper;
import com.parse.starter.Utils.Bitmap_Byte;
import com.parse.starter.Utils.UserMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener, LoaderManager.LoaderCallbacks<Cursor> {

    static SQLiteDatabase db;
    static String userId;
    static String chatUser_id;
    EditText message;
    Button send;
    ImageView photo;
    RecyclerView recyclerView;
    ChatAdapter adapter;
    Cursor chats;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        chatUser_id = intent.getStringExtra("chatuser_id");
        setTitle(intent.getStringExtra("username"));

        StringDbHelper dbHelper = new StringDbHelper(this);
        db = dbHelper.getWritableDatabase();

        message = (EditText) findViewById(R.id.message);
        send = (Button) findViewById(R.id.send);
        photo = (ImageView) findViewById(R.id.photo);
        send.setOnClickListener(this);
        message.setOnKeyListener(this);
        photo.setOnClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.chats);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        initializeChats();

//        final Handler handler = new Handler();
//        final Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                initLoader();
//                handler.postDelayed(this, 1000);
//            }
//        };
//        handler.post(runnable);
    }

    public void initLoader() {
        Bundle args = new Bundle();
        args.putString("chatid", chatUser_id);
        args.putString("currentid", userId);
        getSupportLoaderManager().initLoader(1, args, ChatActivity.this);
    }

    public void send(View view) {

        String sendMessage = message.getText().toString();

        if (sendMessage.isEmpty() || sendMessage.matches("[ ]"))
            return;
        message.setText("");

        long time = new Date().getTime();
        ParseObject message = new ParseObject("Message");
        message.put("sender", userId);
        message.put("recipient", chatUser_id);
        message.put("message", sendMessage);
        message.put("created", time);
        message.saveEventually();

        ContentValues cv = new ContentValues();
        cv.put(StringContract.Message.COLUMN_SENDER, userId);
        cv.put(StringContract.Message.COLUMN_RECIPIENT, chatUser_id);
        cv.put(StringContract.Message.COLUMN_MESSAGE, sendMessage);
        cv.put(StringContract.Message.COLUMN_CREATED_AT, time);

        db.insert(StringContract.Message.TABLE_NAME, null, cv);
        initializeChats();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.send) {
            send(view);
        } else if (id == R.id.photo) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 1);
        } else if (id == R.id.chats || id == R.id.chat_layout) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                final ParseFile file = Bitmap_Byte.getFile(bitmap);

                long time = new Date().getTime();
                final ParseObject photo = new ParseObject("Message");
                photo.put("sender", userId);
                photo.put("recipient", chatUser_id);
                photo.put("created", time);

                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        photo.put("photo", file);
                        photo.saveInBackground();
                    }
                });

                ContentValues cv = new ContentValues();
                cv.put(StringContract.Message.COLUMN_SENDER, userId);
                cv.put(StringContract.Message.COLUMN_RECIPIENT, chatUser_id);
                cv.put(StringContract.Message.COLUMN_PHOTO, Bitmap_Byte.getBytearray(bitmap));
                cv.put(StringContract.Message.COLUMN_CREATED_AT, time);

                db.insert(StringContract.Message.TABLE_NAME, null, cv);

                initializeChats();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initializeChats() {
        chats = db.rawQuery("SELECT * FROM " + StringContract.Message.TABLE_NAME +
                " WHERE " + StringContract.Message.COLUMN_SENDER + " = ? AND " +
                StringContract.Message.COLUMN_RECIPIENT + " = ? UNION SELECT * FROM " +
                StringContract.Message.TABLE_NAME + " WHERE " + StringContract.Message.COLUMN_RECIPIENT + " = ? AND " +
                StringContract.Message.COLUMN_SENDER + " = ? " +
                " ORDER BY " + StringContract.Message.COLUMN_CREATED_AT + ";", new String[]{userId, chatUser_id, userId, chatUser_id});

        setUpRecyclerAdapter();
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            send(view);
        }
        return false;
    }

    public void setUpRecyclerAdapter() {
        adapter = new ChatAdapter(chats, userId, new ChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View item) {
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollToPosition(chats.getCount());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ChatLoader(this, args.getString("chatid"), args.getString("currentid"));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            chats = data;
            setUpRecyclerAdapter();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        chats = null;
    }


    public static class ChatLoader extends AsyncTaskLoader<Cursor> {

        String chatUser_id;
        String currentUserId;
        private Cursor cursor;

        ChatLoader(Context context, String chatid, String currenid) {
            super(context);
            chatUser_id = chatid;
            currentUserId = currenid;
        }

        @Override
        protected void onStartLoading() {
            if (cursor != null) {
                deliverResult(cursor);
            } else {
                forceLoad();
            }
        }


        @Override
        public void deliverResult(Cursor data) {
            cursor = data;
            super.deliverResult(data);
        }

        @Override
        public Cursor loadInBackground() {

            return db.rawQuery("SELECT * FROM " + StringContract.Message.TABLE_NAME +
                    " WHERE " + StringContract.Message.COLUMN_SENDER + " = ? AND " +
                    StringContract.Message.COLUMN_RECIPIENT + " = ? UNION SELECT * FROM " +
                    StringContract.Message.TABLE_NAME + " WHERE " + StringContract.Message.COLUMN_RECIPIENT + " = ? AND " +
                    StringContract.Message.COLUMN_SENDER + " = ? " + chatUser_id +
                    " ORDER BY " + StringContract.Message.COLUMN_CREATED_AT + ";", new String[]{userId, chatUser_id, userId, chatUser_id});

        }

    }

}
