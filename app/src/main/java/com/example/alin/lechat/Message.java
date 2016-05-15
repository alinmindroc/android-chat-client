package com.example.alin.lechat;

class Message {
    public String userName;
    public String messageText;
    public String senderName;

    public Message(String senderName, String userName, String messageText) {
        this.userName = userName;
        this.messageText = messageText;
        this.senderName = senderName;
    }

    public boolean isFromCurrentUser(){
        return userName.equals(senderName);
    }
}
