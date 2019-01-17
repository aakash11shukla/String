package com.parse.starter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.starter.Database.StringContract;
import com.parse.starter.Database.StringDbHelper;

public class UserChatsActivity extends AppCompatActivity {

    private static SQLiteDatabase db;

    private static boolean connected = false;
    private static String currentUserId;
    RecyclerView recyclerView;
    UserChatsAdapter adapter;
    TextView no_data;
    private boolean disconnectedSnakbar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chats);
        setTitle("Chats");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                connectionStatus();
                if (!connected && !disconnectedSnakbar) {
                    showSnakBar(recyclerView);
                    disconnectedSnakbar = true;
                } else if (connected) {
                    disconnectedSnakbar = false;
                }
                handler.postDelayed(this, 1000);
            }

        };

        handler.post(runnable);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(UserChatsActivity.this, ContactsActivity.class));

            }
        });

        no_data = (TextView) findViewById(R.id.no_data);
        recyclerView = (RecyclerView) findViewById(R.id.list_names);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        StringDbHelper dbHelper = new StringDbHelper(this);
        db = dbHelper.getWritableDatabase();

        final Cursor userList = getAllUser();
        final Cursor userChats = getLastChat();

        if (userList == null || userChats == null || userChats.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            adapter = new UserChatsAdapter(userList, userChats, new UserChatsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View item, int position) {
                    userList.moveToPosition(position);
                    final Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("user_id", currentUserId);
                    intent.putExtra("chatuser_id", userList.getString(userList.getColumnIndex(StringContract.User.COLUMN_OBJECTID)));
                    intent.putExtra("username", userList.getString(userList.getColumnIndex(StringContract.User.COLUMN_NAME)));
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            if(connected) {
                ParseUser.logOut();
                this.deleteDatabase("string.db");
                startActivity(new Intent(UserChatsActivity.this, MainActivity.class));
                finish();
            }else {
                showSnakBar(recyclerView);
            }
        }

        return true;
    }

    public void connectionStatus() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        assert connectivityManager != null;
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).
                getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).
                        getState() == NetworkInfo.State.CONNECTED;

    }

    public void showSnakBar(View view) {
        Snackbar.make(view, "No Internet Connection!!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public Cursor getCurrentUser() {
        return db.query(StringContract.User.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                StringContract.User.COLUMN_CREATED_AT + " ASC ",
                "1");
    }

    public Cursor getAllUser() {
        Cursor currentUser = getCurrentUser();

        if (currentUser.moveToFirst()) {
            currentUserId = currentUser.getString(currentUser.getColumnIndex(StringContract.User.COLUMN_OBJECTID));
            currentUser.close();

            return db.rawQuery("SELECT * FROM " +
                            StringContract.User.TABLE_NAME + " WHERE " + StringContract.User.COLUMN_OBJECTID +
                            " IN (SELECT DISTINCT " + StringContract.Message.COLUMN_RECIPIENT + " FROM " +
                            StringContract.Message.TABLE_NAME + " WHERE " +
                            StringContract.Message.COLUMN_SENDER + " = ? UNION SELECT DISTINCT " +
                            StringContract.Message.COLUMN_SENDER + " FROM " +
                            StringContract.Message.TABLE_NAME + " WHERE " + StringContract.Message.COLUMN_RECIPIENT + " = ? )" + " ORDER BY " +
                            StringContract.Message.COLUMN_CREATED_AT + " DESC;",
                    new String[]{currentUserId, currentUserId});
        }

        return null;

    }

    public Cursor getLastChat() {

        Cursor currentUser = getCurrentUser();

        String currentUserId;
        if (currentUser.moveToFirst()) {
            currentUserId = currentUser.getString(currentUser.getColumnIndex(StringContract.User.COLUMN_OBJECTID));
            currentUser.close();
            return db.rawQuery("SELECT " + StringContract.Message.COLUMN_MESSAGE +
                            ", max(" + StringContract.Message.COLUMN_CREATED_AT +
                            ") FROM " + StringContract.Message.TABLE_NAME +
                            " GROUP BY " + StringContract.Message.COLUMN_RECIPIENT +
                            " HAVING " + StringContract.Message.COLUMN_SENDER +
                            " = ? UNION SELECT " + StringContract.Message.COLUMN_MESSAGE +
                            ", max(" + StringContract.Message.COLUMN_CREATED_AT +
                            ") FROM " + StringContract.Message.TABLE_NAME +
                            " GROUP BY " + StringContract.Message.COLUMN_SENDER +
                            " HAVING " + StringContract.Message.COLUMN_RECIPIENT + " = ?;"
                    , new String[]{currentUserId, currentUserId});

        }
        return null;
    }
}
