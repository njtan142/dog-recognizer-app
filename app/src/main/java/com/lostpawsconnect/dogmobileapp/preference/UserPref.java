package com.lostpawsconnect.dogmobileapp.preference;

import android.content.Context;
import java.util.Map;

public class UserPref {
    private static UserPref instance;
    private Context mContext;

    private String userID;

    private UserPref(Context mContext) {
        this.mContext = mContext;
    }

    private UserPref() {
    }

    public static UserPref getInstance(Context context) {
        if (instance == null) {
            instance = new UserPref(context);
        }
        return instance;
    }

    public static UserPref getInstance() {
        if (instance == null) {
            instance = new UserPref();
        }
        return instance;
    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID){
        this.userID = userID;
    }
}
