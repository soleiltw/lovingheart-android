package com.edwardinubuntu.dailykind.object;

import com.parse.ParseObject;

/**
 * Created by edward_chiang on 2014/1/27.
 */
public class User {

    private String name;

    private ParseObject avatar;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParseObject getAvatar() {
        return avatar;
    }

    public void setAvatar(ParseObject avatar) {
        this.avatar = avatar;
    }
}
