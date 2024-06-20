package com.lostpawsconnect.dogmobileapp.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lostpawsconnect.dogmobileapp.model.Users;

public class ResponseParser {
    public static <T> Users parseUser(T any) {
        Users users = null;
        if (any instanceof Users) {
            users = (Users) any;
        }
        return users;
    }

    public static Users parseUser(String response) {
        Users users = new Gson().fromJson(response, new TypeToken<Users>() {
        }.getType());
        return users;
    }
}
