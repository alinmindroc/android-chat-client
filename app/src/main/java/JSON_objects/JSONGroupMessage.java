package JSON_objects;

import com.fasterxml.jackson.annotation.JsonCreator;

public class JSONGroupMessage {
    private int id;
    private String text;
    private String senderId;
    private String senderName;
    private String groupId;

    @JsonCreator
    public JSONGroupMessage() {
        super();
    }

    public JSONGroupMessage(String text, String senderId, String senderName, String groupId) {
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "JSONGroupMessage{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
