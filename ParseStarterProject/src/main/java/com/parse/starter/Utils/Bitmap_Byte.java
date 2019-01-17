package com.parse.starter.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;

public class Bitmap_Byte {

    public static Bitmap getBitmap(byte[] data){
        if(data == null){
            return null;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static byte[] getBytearray(Bitmap bitmap){
        if(bitmap == null){
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

        return stream.toByteArray();
    }

    public static ParseFile getFile(Bitmap bitmap) {
        if(bitmap == null){
            return null;
        }
        return new ParseFile("image.jpeg", getBytearray(bitmap));
    }

    public static Bitmap getPhoto(ParseFile file) {
        if (file == null)
            return null;
        try {
            byte[] data = file.getData();
            if (data != null) {
                return getBitmap(data);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
