package com.lostpawsconnect.dogmobileapp.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Dogs {
    private String dogID;
    private String userID;
    private String dogName;
    private String dogBreed;
    private String dogColor;
    private String dogGender;
    private String imageurl;

    public Float similarity;

    private String remarks;

    private String status;

    public Dogs() {
        // Default constructor required for Firestore
    }

    // Constructor to initialize the object
    public Dogs(String dogID, String userID, String dogName, String dogBreed, String dogColor, String dogGender, String imageurl) {
        this.dogID = dogID;
        this.userID = userID;
        this.dogName = dogName;
        this.dogBreed = dogBreed;
        this.dogColor = dogColor;
        this.dogGender = dogGender;
        this.imageurl = imageurl;
    }

    public Dogs(String dogID, String userID, String dogName, String dogBreed, String dogColor, String dogGender, String imageurl, String remarks) {
        this.dogID = dogID;
        this.userID = userID;
        this.dogName = dogName;
        this.dogBreed = dogBreed;
        this.dogColor = dogColor;
        this.dogGender = dogGender;
        this.imageurl = imageurl;
        this.remarks = remarks;
    }

    public Dogs(String userID, String dogName, String dogBreed, String dogColor, String dogGender, String imageurl) {
        this.dogID = dogID;
        this.userID = userID;
        this.dogName = dogName;
        this.dogBreed = dogBreed;
        this.dogColor = dogColor;
        this.dogGender = dogGender;
        this.imageurl = imageurl;
    }


    // Getter methods
    public String getDogID() {
        return dogID;
    }

    public String getUserID() {
        return userID;
    }

    public String getDogName() {
        return dogName;
    }

    public String getDogBreed() {
        return dogBreed;
    }

    public String getDogColor() {
        return dogColor;
    }

    public String getDogGender() {
        return dogGender;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setDogID(String dogID) {
        this.dogID = dogID;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    // Convert the object to a Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("userID", userID);
        data.put("dogName", dogName);
        data.put("breed", dogBreed);
        data.put("color", dogColor);
        data.put("gender", dogGender);
        data.put("imagePath", imageurl);
        data.put("remarks", remarks);
        data.put("status", status);
        return data;
    }

    // Convert Firestore data to an object
    public static Dogs fromMap(Map<String, Object> data) {
        String dogID = (String) data.get("id");
        String userID = (String) data.get("userID");
        String dogName = (String) data.get("dogName");
        String dogBreed = (String) data.get("breed");
        String dogColor = (String) data.get("color");
        String dogGender = (String) data.get("gender");
        String imagePathString = (String) data.get("imagePath");
        if (data.containsKey("remarks")) {
            String dogRemarks = (String) data.get("remarks");
            if (data.containsKey("status")) {
                String dogStatus = (String) data.get("status");
                Dogs dog = new Dogs(dogID, userID, dogName, dogBreed, dogColor, dogGender, imagePathString, dogRemarks);
                dog.setStatus(dogStatus);
                return dog;
            }
            return new Dogs(dogID, userID, dogName, dogBreed, dogColor, dogGender, imagePathString, dogRemarks);
        }

        return new Dogs(dogID, userID, dogName, dogBreed, dogColor, dogGender, imagePathString);
    }
}
