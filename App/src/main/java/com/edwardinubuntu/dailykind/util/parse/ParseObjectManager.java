package com.edwardinubuntu.dailykind.util.parse;

import com.edwardinubuntu.dailykind.object.Category;
import com.edwardinubuntu.dailykind.object.Graphic;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.object.Story;
import com.parse.ParseObject;

/**
 * Created by edward_chiang on 2013/12/29.
 */
public class ParseObjectManager {

    private ParseObject parseObject;

    public ParseObjectManager(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public Story getStory() {
        Story story = new Story();
        story.setObjectId(parseObject.getObjectId());
        story.setStoryTeller(parseObject.getParseUser("StoryTeller"));
        story.setContent(parseObject.getString("Content"));
        story.setCreatedAt(parseObject.getCreatedAt());
        story.setUpdatedAt(parseObject.getUpdatedAt());
        story.setLocationAreaName(parseObject.getString("areaName"));
        story.setViewCount(parseObject.getInt("viewCount"));
        return story;
    }

    public Idea getIdea() {
        Idea idea = new Idea();
        idea.setObjectId(parseObject.getObjectId());
        idea.setName(parseObject.getString("Name"));
        idea.setIdeaDescription(parseObject.getString("Description"));
        idea.setDoneCount(parseObject.getInt("doneCount"));

        return idea;
    }

    public Graphic getGraphic() {
        if (parseObject != null) {
            Graphic graphic = new Graphic();
            if (parseObject.has("imageFile") && parseObject.getParseFile("imageFile") != null) {
                graphic.setParseFileUrl(parseObject.getParseFile("imageFile").getUrl());
            }
            graphic.setFileType(parseObject.getString("imageType"));
            graphic.setObjectId(parseObject.getObjectId());
            graphic.setImageUrl(parseObject.getString("imageUrl"));
            return graphic;
        }
        return null;
    }

    public Category getCategory() {
        if (parseObject != null) {
            Category category = new Category();
            category.setObjectId(parseObject.getObjectId());
            category.setName(parseObject.getString("Name"));

            return category;
        }
        return null;
    }
}
