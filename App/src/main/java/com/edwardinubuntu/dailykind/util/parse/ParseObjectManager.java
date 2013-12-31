package com.edwardinubuntu.dailykind.util.parse;

import com.edwardinubuntu.dailykind.object.Category;
import com.edwardinubuntu.dailykind.object.Graphic;
import com.edwardinubuntu.dailykind.object.Idea;
import com.parse.ParseObject;

/**
 * Created by edward_chiang on 2013/12/29.
 */
public class ParseObjectManager {

    private ParseObject parseObject;

    public ParseObjectManager(ParseObject parseObject) {
        this.parseObject = parseObject;
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
        ParseObject graphicObject = parseObject.getParseObject("graphicPointer");
        if (graphicObject!=null) {
            Graphic graphic = new Graphic();
            if (graphicObject.getParseFile("imageFile") != null) {
                graphic.setParseFileUrl(graphicObject.getParseFile("imageFile").getUrl());
            }
            graphic.setObjectId(graphicObject.getObjectId());
            graphic.setImageUrl(graphicObject.getString("imageUrl"));
            return graphic;
        }
        return null;
    }

    public Category getCategory() {
        Category category = new Category();
        ParseObject categoryObject = parseObject.getParseObject("categoryPointer");
        category.setObjectId(categoryObject.getObjectId());
        category.setName(categoryObject.getString("Name"));

        return category;
    }
}
