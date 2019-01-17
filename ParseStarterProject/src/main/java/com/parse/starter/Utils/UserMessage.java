package com.parse.starter.Utils;

import android.graphics.Bitmap;

import java.util.Date;

public class UserMessage {
    private String objectId;
    private Date createdAt;
    private Bitmap photo;
    private String message;
    private String sender;
    private String recipient;

    public UserMessage(){};

    public UserMessage(String objectId, Date createdAt, String sender, String recipient, String message, Bitmap photo){
        this.objectId = objectId;
        this.createdAt = createdAt;
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.photo = photo;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public Bitmap getPhoto() {
        return photo;
    }
}
