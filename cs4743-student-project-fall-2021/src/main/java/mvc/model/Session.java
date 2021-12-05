package mvc.model;

import javax.persistence.*;

@Entity
@Table(name = "session")
public class Session {
    @Id
    @Column(name = "token")
    private String sessionId;

    @Column(name = "user_id")
    private long userId;

    @Transient
    private String userFullName;

    @Transient
    private String url;

    public Session() {
    }

    public Session(String sessionId) {
        this.sessionId = sessionId;
        this.userFullName = "";
    }

    public Session(String sessionId, int userId){
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public Session(String sessionId, String userFullName) {
        this.sessionId = sessionId;
        this.userFullName = userFullName;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    // getters setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
