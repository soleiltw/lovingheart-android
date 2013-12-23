package com.edwardinubuntu.dailykind.object;

import java.io.Serializable;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class Category implements Serializable {

    private String objectId;
    private String name;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
