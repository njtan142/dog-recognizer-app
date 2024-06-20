package com.lostpawsconnect.dogmobileapp.services;

import android.content.Context;

import com.github.MakMoinee.library.models.LocalVolleyRequestBody;
import com.github.MakMoinee.library.services.LocalVolleyRequest;
import com.lostpawsconnect.dogmobileapp.model.Dogs;

import java.util.HashMap;
import java.util.Map;

public class VRequest extends LocalVolleyRequest {

    public static final String IP = "192.168.1.13:8443";
    public static final String host = "http://" + IP;

    public static final String loginPath = "/login/api";

    public static final String registerDogPath = "/register/dog/api";
    public static final String reportDogPath = "/report/dog/api";

    public VRequest(Context mContext) {
        super(mContext);
    }


    public LocalVolleyRequestBody generateBody(String username, String password) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        map.put("btnLogin", "true");
        LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                .setBodyMap(map)
                .setUrl(String.format("%s%s", host, loginPath))
                .build();
        return body;
    }

    /**
     * Compose the request body to be passed to the server api
     *
     * @return
     */
//    public LocalVolleyRequestBody generateDogRequestBody(Dogs dogs) {
//        LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
//                .setBodyMap(dogs.getMultipartBody())
//                .setUrl(String.format("%s%s", host, registerDogPath))
//                .build();
//        return body;
//    }

    public LocalVolleyRequestBody generateDogGetRequest(){
        LocalVolleyRequestBody body = new LocalVolleyRequestBody.LocalVolleyRequestBodyBuilder()
                .setUrl(String.format("%s%s", host, reportDogPath))
                .build();

        return body;
    }
}
