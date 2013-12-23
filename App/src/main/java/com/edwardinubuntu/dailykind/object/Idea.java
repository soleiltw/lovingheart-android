package com.edwardinubuntu.dailykind.object;

import java.io.Serializable;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class Idea implements Serializable {

    private Category category;

    private String name;

    private String ideaDescription;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdeaDescription() {
        return ideaDescription;
    }

    public void setIdeaDescription(String ideaDescription) {
        this.ideaDescription = ideaDescription;
    }
}
