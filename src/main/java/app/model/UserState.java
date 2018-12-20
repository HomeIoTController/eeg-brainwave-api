package app.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "UserState")
public class UserState {

    private static final Logger log = LoggerFactory.getLogger(UserState.class);

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;
    private Integer userId;
    private String state;
    private Boolean deleted;

    public UserState() {
    }

    public UserState(Integer userId, String state) {
        this.userId = userId;
        this.state = state;
        this.deleted = false;
    }

    public UserState(Integer id, Integer userId, String state, Boolean deleted) {
        this.id = id;
        this.userId = userId;
        this.state = state;
        this.deleted = deleted;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
