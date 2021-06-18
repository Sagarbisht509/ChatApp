package com.sagar.chatapp.Models;

public class Message {

    private long messageTime;
    private String messageId;
    private String senderId;
    private String message;
    private String imageUrl;

    public Message() {
    }

    public Message(long messageTime, String senderId, String message, String messageId) {
        this.messageTime = messageTime;
        this.senderId = senderId;
        this.message = message;
        this.messageId = messageId;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
