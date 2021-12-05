package mvc.model;

import javax.persistence.*;

@Entity
@Table(name = "Audit")
public class Audit {
    @Column(name = "change_msg", nullable = false)
    private String changeMsg;

    @Column(name = "changed_by", nullable = false)
    private long changedBy;

    @Column(name = "when_occurred")
    private String timeStamp;

    @Column(name = "person_id", nullable = false)
    private long personId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public Audit(String changeMsg, long changedBy, long personId, String timeStamp){

        this.changeMsg = changeMsg;
        this.changedBy = changedBy;
        this.personId = personId;
        this.timeStamp = timeStamp;
    }

    public Audit(String changeMsg, long changedBy, long personId){

        this.changeMsg = changeMsg;
        this.changedBy = changedBy;
        this.personId = personId;
    }

    public Audit() {
        this.id = 0;
        this.changeMsg = "";
        this.changedBy = 0;
        this.personId = 0;
        this.timeStamp = "";
    }

    public void setChangedBy(long changedBy) {
        this.changedBy = changedBy;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getChangeMsg() {
        return changeMsg;
    }

    public void setChangeMsg(String changeMsg) {
        this.changeMsg = changeMsg;
    }

    public long getChangedBy() {
        return changedBy;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

}
