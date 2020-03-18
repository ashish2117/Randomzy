package com.ash.randomzy.model;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class User {

    private String uId;
    private String Name;
    private Date dateOfBirth;
    private String emailId;
    private String sex;
    private String bio;
    private String profilePicUrl;
    private String profilePicThumbUrl;

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicThumbUrl() {
        return profilePicThumbUrl;
    }

    public void setProfilePicThumbUrl(String profilePicThumbUrl) {
        this.profilePicThumbUrl = profilePicThumbUrl;
    }

    public JSONObject toJson() throws JSONException {
        return new JSONObject(new Gson().toJson(this));
    }
}
