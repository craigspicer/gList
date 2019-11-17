package com.nullparams.glist.models;

public class User {

    private String id;
    private String emailAddress;

    public User() {
        //empty constructor needed
    }

    public User(String id, String emailAddress) {

        this.id = id;
        this.emailAddress = emailAddress;
    }

    public String getId() {
        return id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
