package com.example.alin.lechat;

class Message {
    public String userName;
    public String messageText;

    public Message(String userName, String messageText) {
        this.userName = userName;
        this.messageText = messageText;
    }

    public boolean isCurrentUser(){
        return userName.equals("Alin");
    }
}
