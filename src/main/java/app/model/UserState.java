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

    public UserState() {
    }

    public UserState(Integer userId, String state) {
        this.userId = userId;
        this.state = state;
    }

    public UserState(Integer id, Integer userId, String state) {
        this.id = id;
        this.userId = userId;
        this.state = state;
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
}
