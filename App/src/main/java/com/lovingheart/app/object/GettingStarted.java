package com.lovingheart.app.object;

import com.parse.ParseObject;

/**
 * Created by edward_chiang on 2014/3/12.
 */
public class GettingStarted {

    private ParseObject contentObject;

    private ParseObject userLog;

    public ParseObject getContentObject() {
        return contentObject;
    }

    public void setContentObject(ParseObject contentObject) {
        this.contentObject = contentObject;
    }

    public ParseObject getUserLog() {
        return userLog;
    }

    public void setUserLog(ParseObject userLog) {
        this.userLog = userLog;
    }
}
