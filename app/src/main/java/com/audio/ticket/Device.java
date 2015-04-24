package com.audio.ticket;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.provider.Settings.Secure;


/**
 * Created by Mohamed on 4/9/2015.
 */
public class Device {

    Context context;

    public Device( Context context) {
        this.context = context;
    }


    /**
     * Get Device Unique Id.
     * @return
     */
    public String getDeviceID() {
        String android_id = Secure.getString(this.context.getContentResolver(),
                Secure.ANDROID_ID);
        return android_id;
    }

    /**
     * Return the Google Account.
     * @return
     */
    public String getGoogleAccount(){
        Account[] accounts = AccountManager.get(this.context).getAccountsByType("com.google");
        for (Account account : accounts) {
            // this is where the email should be in:
            return account.name;
        }
        return "";
    }


}
