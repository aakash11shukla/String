/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.parse.starter.Database.StringContract;
import com.parse.starter.Database.StringDbHelper;
import com.parse.starter.Utils.Bitmap_Byte;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    SQLiteDatabase db;

    private EditText username;
    private EditText passwd;
    private EditText phone;
    Button login_or_singup;
    TextView toggle;
    Toast mToast;
    private CardView cardView;
    private ImageView imageView;
    Button button;
    Bitmap profile_photo;

    Boolean signupmodeActive = false;
    boolean connected = false;
    boolean disconnectedSnakbar = false;

    ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, UserChatsActivity.class));
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_linear_layout);
        linearLayout.setOnClickListener(this);

        username = (EditText) findViewById(R.id.username);
        passwd = (EditText) findViewById(R.id.password);
        phone = (EditText) findViewById(R.id.phone);
        login_or_singup = (Button) findViewById(R.id.login_or_signup);
        toggle = (TextView) findViewById(R.id.toggle);
        cardView = (CardView) findViewById(R.id.set_profile_card);
        imageView = (ImageView) findViewById(R.id.set_profile_image);
        button = (Button) findViewById(R.id.profile_set);


        login_or_singup.setOnClickListener(this);
        toggle.setOnClickListener(this);
        button.setOnClickListener(this);
        username.setOnKeyListener(this);
        phone.setOnKeyListener(this);
        passwd.setOnKeyListener(this);

        StringDbHelper dbHelper = new StringDbHelper(this);
        db = dbHelper.getWritableDatabase();

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                connectionStatus();
                if (!connected && !disconnectedSnakbar) {
                    showSnakBar(linearLayout);
                    disconnectedSnakbar = true;
                } else if (connected) {
                    disconnectedSnakbar = false;
                }
                handler.postDelayed(this, 1000);
            }

        };

        handler.post(runnable);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public void signup(View view) {

        if (!connected) {
            showSnakBar(view);
        } else {

            if (signupmodeActive && (username.getText().toString().isEmpty() || passwd.getText().toString().isEmpty() || phone.getText().toString().isEmpty())) {
                toast("A field cannot be empty!!");
            } else if (!signupmodeActive && (username.getText().toString().isEmpty() || passwd.getText().toString().isEmpty())) {
                toast("A field cannot be empty!!");
            } else {

                if (signupmodeActive) {

                    if (!Patterns.PHONE.matcher(phone.getText().toString()).matches()) {
                        toast("Invalid phone number!!");
                        return;
                    }

                    String num = phone.getText().toString();
                    num = num.replaceAll("[^+0-9]", "");
                    if (num.startsWith("+")) {
                        num = num.substring(3);
                    }
                    if (num.startsWith("0")) {
                        num = num.substring(1);
                    }

                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("UserInfo");
                    query.whereEqualTo("Phone", num);
                    query.setLimit(1);
                    final String finalNum = num;
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if (e == null) {
                                if (objects.size() == 0) {
                                    user = new ParseUser();
                                    ParseFile file = Bitmap_Byte.getFile(profile_photo);
                                    final ParseFile finalFile = file;
                                    file.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                user.setUsername(username.getText().toString());
                                                user.setPassword(passwd.getText().toString());
                                                user.signUpInBackground(new SignUpCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            toast("Successful");
                                                            ParseObject parseObject = new ParseObject("UserInfo");
                                                            parseObject.put("UserObjectId", user.getObjectId());
                                                            parseObject.put("Phone", finalNum);
                                                            parseObject.put("Photo", finalFile);

                                                            parseObject.saveEventually();

                                                            ContentValues cv = new ContentValues();
                                                            cv.put(StringContract.User.COLUMN_NAME, user.getUsername());
                                                            cv.put(StringContract.User.COLUMN_OBJECTID, user.getObjectId());
                                                            cv.put(StringContract.User.COLUMN_PHONE, finalNum);

                                                            if (profile_photo != null) {
                                                                byte[] array = Bitmap_Byte.getBytearray(profile_photo);
                                                                cv.put(StringContract.User.COLUMN_PROFILE_PHOTO, array);
                                                            }
                                                            db.insert(StringContract.User.TABLE_NAME, null, cv);
                                                            startActivity(new Intent(MainActivity.this, UserChatsActivity.class));
                                                            finish();
                                                        } else {
                                                            toast(e.getMessage());
                                                        }
                                                    }
                                                });
                                            } else {
                                                toast(e.getMessage());
                                            }
                                        }
                                    });
                                } else {
                                    toast("Account with phone number aready exists.");
                                }
                            }
                        }
                    });


                } else {
                    ParseUser.logInInBackground(username.getText().toString(), passwd.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (mToast != null) {
                                mToast.cancel();
                                mToast = null;
                            }

                            if (e == null) {
                                mToast = Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT);
                                mToast.show();
                                startActivity(new Intent(MainActivity.this, UserChatsActivity.class));
                                finish();
                            } else {
                                mToast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
                                mToast.show();
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.toggle) {
            if (signupmodeActive) {
                signupmodeActive = false;
                phone.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                toggle.setText(R.string.or_signup);
                login_or_singup.setText(R.string.login);
            } else {
                signupmodeActive = true;
                button.setVisibility(View.VISIBLE);
                phone.setVisibility(View.VISIBLE);
                toggle.setText(R.string.or_login);
                login_or_singup.setText(R.string.Signup);
            }
        } else if (id == R.id.login_or_signup) {
            signup(v);
        } else if (id == R.id.main_linear_layout) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            }
        }
        if (id == R.id.profile_set) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 1);
        }
    }

    public void toast(String msg) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            signup(view);
        }
        return false;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (bitmap != null) {
                    cardView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                    profile_photo = bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}