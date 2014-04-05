package com.lovingheart.app.object;

import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by edward_chiang on 2014/1/27.
 */
public class Review {

    private ParseUser userObject;

    private User user;

    private int value;

    private String reviewDescription;

    private Date createdAt;

    public ParseUser getUserObject() {
        return userObject;
    }

    public void setUserObject(ParseUser userObject) {
        this.userObject = userObject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getReviewDescription() {
        return reviewDescription;
    }

    public void setReviewDescription(String reviewDescription) {
        this.reviewDescription = reviewDescription;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
