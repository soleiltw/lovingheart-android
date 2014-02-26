package com.edwardinubuntu.dailykind.object;

/**
 * Created by edward_chiang on 2014/2/25.
 */
public class Info {

    private int graphicResource;
    private String title;
    private String description;

    public static enum  GraphicDirection {
        LEFT,
        RIGHT
    }

    private GraphicDirection graphicDirection;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGraphicResource() {
        return graphicResource;
    }

    public void setGraphicResource(int graphicResource) {
        this.graphicResource = graphicResource;
    }

    public GraphicDirection getGraphicDirection() {
        return graphicDirection;
    }

    public void setGraphicDirection(GraphicDirection graphicDirection) {
        this.graphicDirection = graphicDirection;
    }
}
