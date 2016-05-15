package JSON_objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Date;

public class JSONMessage {
    private String content;
    private String senderId;
    private String receiverId;
    private String senderName;
    private String receiverName;
    private Date date;

    @JsonCreator
    public JSONMessage() {
        super();
    }

    public JSONMessage(String content, String senderId, String receiverId, String senderName, String receiverName, Date date) {
        this.content = content;
        this.date = date;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.date = date;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public String toString() {
        return "JSONMessage{" +
                "content='" + content + '\'' +
                ", date=" + date +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", receiverName='" + receiverName + '\'' +
                '}';
    }
}
