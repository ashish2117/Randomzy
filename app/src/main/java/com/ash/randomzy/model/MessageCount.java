package com.ash.randomzy.model;

import com.google.gson.Gson;

public class MessageCount {

    private int count;
    private String userId;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString(){
        return new Gson().toJson(this);
    }
}
