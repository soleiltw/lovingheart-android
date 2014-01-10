package com.edwardinubuntu.dailykind.object;

import java.io.Serializable;

/**
 * Created by edward_chiang on 2013/12/29.
 */
public class Graphic implements Serializable {

    private String objectId;

    private String imageUrl;

    private String parseFileUrl;

    private String fileType;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getParseFileUrl() {
        return parseFileUrl;
    }

    public void setParseFileUrl(String parseFileUrl) {
        this.parseFileUrl = parseFileUrl;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
