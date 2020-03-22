package com.ash.randomzy.model;

import com.google.gson.Gson;

public class ActiveChat {

    private String id;
    private String name;
    private String lastText;
    private Long lastTextTime;
    private int lastTextStatus;
    private String sentBy;
    private String profilePicUrlLocal;
    private String profilePicUrlServer;
    private int unreadCount;

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

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public String getProfilePicUrlLocal() {
        return profilePicUrlLocal;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
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
