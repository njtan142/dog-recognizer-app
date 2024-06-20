package com.lostpawsconnect.dogmobileapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Users {
    private int userID;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private int userType;
}
