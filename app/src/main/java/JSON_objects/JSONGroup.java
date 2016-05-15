package JSON_objects;

import com.fasterxml.jackson.annotation.JsonCreator;

public class JSONGroup {
    private int id;
    private String name;
    private String membersId;

    @JsonCreator
    public JSONGroup() {
        super();
    }

    public JSONGroup(String name, String membersId) {
        this.name = name;
        this.membersId = membersId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMembersId() {
        return membersId;
    }

    public void setMembersId(String membersId) {
        this.membersId = membersId;
    }

    @Override
    public String toString() {
        return "JSONGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", membersId='" + membersId + '\'' +
                '}';
    }
}
