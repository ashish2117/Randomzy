package com.ash.randomzy.model;

public class MessageUpdate {

    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_EDITED = 1;

    private String messageId;
    private long timeStamp;
    private int messageUpdateType;
    private String message;

    public MessageUpdate(String messageId, long timeStamp, int messageUpdateType, String message) {
        this.messageId = messageId;
        this.timeStamp = timeStamp;
        this.messageUpdateType = messageUpdateType;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getMessageUpdateType() { return messageUpdateType; }

    public String getMessage(){ return message; };
}
