package com.ash.randomzy.entity;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ActiveChat {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "lastText")
    private String lastText;

    @ColumnInfo(name = "lastTextTime")
    private Long lastTextTime;

    @ColumnInfo(name = "lastTextStatus")
    private int lastTextStatus;

    @ColumnInfo(name = "sentBy")
    private int sentBy;

    @ColumnInfo(name = "profilePicUrlLocal")
    private String profilePicUrlLocal;

    @ColumnInfo(name = "profilePicUrlServer")
    private String profilePicUrlServer;

    @ColumnInfo(name = "isFav")
    private int isFav;

    public int getIsFav() {
        return isFav;
    }

    public void setIsFav(int isFav) {
        this.isFav = isFav;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastText() {
        return lastText;
    }

    public void setLastText(String lastText) {
        this.lastText = lastText;
    }

    public Long getLastTextTime() {
        return lastTextTime;
    }

    public void setLastTextTime(Long lastTextTime) {
        this.lastTextTime = lastTextTime;
    }

    public int getLastTextStatus() {
        return lastTextStatus;
    }

    public void setLastTextStatus(int lastTextStatus) {
        this.lastTextStatus = lastTextStatus;
    }

    public int getSentBy() {
        return sentBy;
    }

    public void setSentBy(int sentBy) {
        this.sentBy = sentBy;
    }

    public String getProfilePicUrlLocal() {
        return profilePicUrlLocal;
    }

    public void setProfilePicUrlLocal(String profilePicUrlLocal) {
        this.profilePicUrlLocal = profilePicUrlLocal;
    }

    public String getProfilePicUrlServer() {
        return profilePicUrlServer;
    }

    public void setProfilePicUrlServer(String profilePicUrlServer) {
        this.profilePicUrlServer = profilePicUrlServer;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
