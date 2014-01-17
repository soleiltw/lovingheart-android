package com.edwardinubuntu.dailykind.object;

import com.parse.ParseObject;

/**
 * Created by edward_chiang on 2014/1/17.
 */
public class IdeaObject {

    private int titleImageResource;

    private String title;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }
}
