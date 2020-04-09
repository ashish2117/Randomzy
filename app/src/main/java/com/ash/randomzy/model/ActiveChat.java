package com.ash.randomzy.model;

import com.google.gson.Gson;

public class ActiveChat {

    private String id;
    private String name;
    private String lastMessageText;
    private Long lastMessageTime;
    private int lastMessageStatus;
    private String sentBy;
    private String profilePicUrlLocal;
    private String profilePicUrlServer;
    private int unreadCount;
    private int isTyping;
    private int lastMessageType;

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

    public String getLastMessageText() {
        return lastMessageText;
    }

    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public Long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getLastMessageStatus() {
        return lastMessageStatus;
    }

    public void setLastMessageStatus(int lastMessageStatus) {
        this.lastMessageStatus = lastMessageStatus;
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

    public int getIsTyping() {
        return isTyping;
    }

    public void setIsTyping(int isTyping) {
        this.isTyping = isTyping;
    }

    public int getLastMessageType() {
        return lastMessageType;
    }

    public void setLastMessageType(int lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
