package com.ticket.helpers;


import android.net.Uri;

import java.sql.Time;
import java.util.Date;

/**
 * Created by Mohamed on 4/26/2015.
 */
public class Message {


    int type = 3;
    boolean isSender = true;
    String data ="";
    String path= "";
    Date DateSent ;
    Uri uri;

    public Message() {
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean isSender) {
        this.isSender = isSender;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDateSent() {
        return DateSent;
    }

    public void setDateSent(Date dateSent) {
        DateSent = dateSent;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
