package JSON_objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Date;

public class JSONNotification {
    private String content;
    private String recipientId;
    private String accepted;
    private int id;

    @JsonCreator
    public JSONNotification() {
        super();
    }

    public JSONNotification(String content, String recipientId, String accepted, int id) {
        this.content = content;
        this.recipientId = recipientId;
        this.accepted = accepted;
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "JSONNotification{" +
                "content='" + content + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", accepted='" + accepted + '\'' +
                ", id=" + id +
                '}';
    }
}
