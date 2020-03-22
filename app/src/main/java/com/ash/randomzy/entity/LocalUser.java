package com.ash.randomzy.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocalUser {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "profilePicUrlLocal")
    private String profilePicUrlLocal;

    @ColumnInfo(name = "profilePicUrlServer")
    private String profilePicUrlServer;

    @ColumnInfo(name = "isFav")
    private int isFav;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getIsFav() {
        return isFav;
    }

    public void setIsFav(int isFav) {
        this.isFav = isFav;
    }
}
