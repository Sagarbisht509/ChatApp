package com.sagar.chatapp.Models;

public class User {

    private String name, phoneNumber, profileImage, uId, about;

    public User() {
    }

    public User(String name, String phoneNumber, String profileImage, String uId, String about) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.uId = uId;
        this.about = about;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}
