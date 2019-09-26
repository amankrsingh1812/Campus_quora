package com.android.campusquora;

import java.util.ArrayList;

public class posts {
    private String heading;
    private String text;

    private Long likes,dislikes;
    private ArrayList<String> tags;

    public posts(String heading, String text, Long likes, Long dislikes, ArrayList<String> tags){
        this.heading = heading;
        this.text = text;
        this.likes = likes;
        this.dislikes = dislikes;
        this.tags = tags;


    }


    public String getHeading() {
        return heading;
    }

    public String getText() {
        return text;
    }

    public Long getLikes() {
        return likes;
    }

    public Long getDislikes() {
        return dislikes;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

}
