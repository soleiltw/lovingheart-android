package com.edwardinubuntu.dailykind.object;

import com.parse.ParseObject;

/**
 * Created by edward_chiang on 2014/1/17.
 */
public class IdeaObject {

    private int titleImageResource;

    private int titleResource;

    private ParseObject parseObject;

    public IdeaObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public int getTitleImageResource() {
        return titleImageResource;
    }

    public void setTitleImageResource(int titleImageResource) {
        this.titleImageResource = titleImageResource;
    }

    public int getTitleResource() {
        return titleResource;
    }

    public void setTitleResource(int titleResource) {
        this.titleResource = titleResource;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }
}
