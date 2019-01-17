package com.parse.starter;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.starter.Database.StringContract;
import com.parse.starter.Database.StringDbHelper;

import java.util.HashMap;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PERMISSION_REQUEST_CODE = 1;
    Activity activity = ContactsActivity.this;
    String wantPermission = Manifest.permission.READ_CONTACTS;
    static String name, phone;

    RecyclerView recyclerView;
    ContactsAdapter adapter;

    static boolean connected = false;
    static boolean disconnectedSnakbar = false;

    static Cursor contacts;
    static String currentUserId;

    static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        setTitle("Contacts");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        connectionStatus();

        StringDbHelper dbHelper = new StringDbHelper(this);
        db = dbHelper.getWritableDatabase();

        recyclerView = (RecyclerView) findViewById(R.id.contacts);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), ((LinearLayoutManager) layoutManager).getOrientation()));

        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            initializeContacts();
        }

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

    }

    public void initLoader() {
        getSupportLoaderManager().initLoader(1, null, ContactsActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh) {
            Toast.makeText(getApplicationContext(), "Refreshing Contacts", Toast.LENGTH_SHORT).show();
            readContacts();
        } else if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void readContacts() {
        if (!connected) {
            showSnakBar(recyclerView);
            initializeContacts();
        } else {
            initLoader();
        }
    }

    public void initializeContacts() {
        Cursor currentUser = db.query(StringContract.User.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                StringContract.User.COLUMN_CREATED_AT + " ASC",
                "1");

        if (currentUser != null && currentUser.moveToFirst()) {
            String phone = currentUser.getString(currentUser.getColumnIndex(StringContract.User.COLUMN_PHONE));
            currentUserId = currentUser.getString(currentUser.getColumnIndex(StringContract.User.COLUMN_OBJECTID));
            currentUser.close();

            contacts = db.query(StringContract.User.TABLE_NAME,
                    null,
                    StringContract.User.COLUMN_PHONE + " != ?",
                    new String[]{phone},
                    null,
                    null,
                    null,
                    null);
        } else {
            contacts = null;
        }
        setupRecyclerview(contacts);
    }

    public void setupRecyclerview(final Cursor cursor) {
        adapter = new ContactsAdapter(contacts, new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View item, int position) {
                if (cursor != null) {
                    if (cursor.moveToPosition(position)) {
                        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                        intent.putExtra("user_id", currentUserId);
                        intent.putExtra("chatuser_id", cursor.getString(cursor.getColumnIndex(StringContract.User.COLUMN_OBJECTID)));
                        intent.putExtra("username", cursor.getString(cursor.getColumnIndex(StringContract.User.COLUMN_NAME)));
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Toast.makeText(activity, "Read contacts permission allows us to read your contacts. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readContacts();
                } else {
                    Toast.makeText(activity, "Permission Denied. We can't read contacts.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String order = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        return new ContactsLoader(this,
                getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, order));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        initializeContacts();
        Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        contacts = null;
    }


    private static class ContactsLoader extends AsyncTaskLoader<Cursor> {

        private Cursor mCursor;
        private Cursor crContacts;

        ContactsLoader(Context context, Cursor cursor) {
            super(context);
            crContacts = cursor;
        }

        @Override
        protected void onStartLoading() {
            if (mCursor != null) {
                deliverResult(mCursor);
            } else {
                forceLoad();
            }
        }


        @Override
        public void deliverResult(Cursor cursor) {
            mCursor = cursor;
            super.deliverResult(cursor);
        }

        @Override
        public Cursor loadInBackground() {

            if (crContacts != null) {
                HashMap<String, String> map = new HashMap<>();
                if (crContacts.moveToFirst()) {
                    do {
                        name = crContacts.getString(crContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phone = crContacts.getString(crContacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phone = phone.replaceAll("[^+0-9]", "");

                        if (phone.startsWith("+")) {
                            phone = phone.substring(3);
                        }
                        if (phone.startsWith("0")) {
                            phone = phone.substring(1);
                        }

                        if (phone.length() < 10) {
                            continue;
                        }

                        if (map.containsKey(phone)) {
                            if (map.get(phone).equals("1")) {
                                continue;
                            }
                        } else {
                            map.put(phone, "1");
                        }

                        ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("UserInfo");
                        parseQuery.whereEqualTo("Phone", phone);
                        parseQuery.setLimit(1);
                        try {
                            List<ParseObject> info = parseQuery.find();
                            if (info.size() > 0) {
                                ParseQuery<ParseUser> query = ParseUser.getQuery();
                                query.whereEqualTo("objectId", info.get(0).get("UserObjectId"));
                                query.setLimit(1);
                                try {
                                    List<ParseUser> objects = query.find();
                                    if (objects.size() > 0) {
                                        Cursor cursor = db.query(StringContract.User.TABLE_NAME,
                                                null,
                                                StringContract.User.COLUMN_PHONE + " = ? ",
                                                new String[]{phone},
                                                null,
                                                null,
                                                null,
                                                null);

                                        ContentValues cv = new ContentValues();
                                        cv.put(StringContract.User.COLUMN_NAME, name);
                                        ParseFile file = info.get(0).getParseFile("Photo");
                                        if (file != null) {
                                            byte[] image = file.getData();
                                            cv.put(StringContract.User.COLUMN_PROFILE_PHOTO, image);
                                        }
                                        cv.put(StringContract.User.COLUMN_PHONE, phone);
                                        cv.put(StringContract.User.COLUMN_OBJECTID, objects.get(0).getObjectId());
                                        if (cursor.getCount() == 0) {
                                            db.insert(StringContract.User.TABLE_NAME,
                                                    null,
                                                    cv);
                                        } else {
                                            db.update(StringContract.User.TABLE_NAME,
                                                    cv,
                                                    StringContract.User.COLUMN_PHONE + " = ?",
                                                    new String[]{phone});
                                            cursor.close();
                                        }
                                    }
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } while (crContacts.moveToNext());
                }
            }
            return null;
        }
    }
}
