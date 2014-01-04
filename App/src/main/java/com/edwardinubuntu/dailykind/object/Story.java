package com.edwardinubuntu.dailykind.object;

import com.parse.ParseUser;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by edward_chiang on 2014/1/1.
 */
public class Story implements Serializable {

    private String objectId;
    private ParseUser storyTeller;
    private String content;
    private Idea idea;
    private Date createdAt;
    private Date updatedAt;
    private Graphic graphic;

    private String locationAreaName;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public ParseUser getStoryTeller() {
        return storyTeller;
    }

    public void setStoryTeller(ParseUser storyTeller) {
        this.storyTeller = storyTeller;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Idea getIdea() {
        return idea;
    }

    public void setIdea(Idea idea) {
        this.idea = idea;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public void setGraphic(Graphic graphic) {
        this.graphic = graphic;
    }

    public String getLocationAreaName() {
        return locationAreaName;
    }

    public void setLocationAreaName(String locationAreaName) {
        this.locationAreaName = locationAreaName;
    }
}
