package com.parse.starter.Database;

import android.provider.BaseColumns;

public final class StringContract {

    private StringContract(){};

    public final class User implements BaseColumns{
        public static final String TABLE_NAME = "User";
        public static final String COLUMN_OBJECTID = "objectId";
        public static final String COLUMN_CREATED_AT = "createdAt";
        public static final String COLUMN_NAME = "username";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_PROFILE_PHOTO = "photo";
    }

    public final class Message implements BaseColumns{
        public static final String TABLE_NAME = "Message";
        public static final String COLUMN_CREATED_AT = "createdAt";
        public static final String COLUMN_SENDER = "sender";
        public static final String COLUMN_RECIPIENT = "recipient";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_PHOTO = "photo";
    }
}
