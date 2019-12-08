package com.android.campusquora.model;

import java.util.ArrayList;

public class Post {
    private String postID;
    private String heading;
    private String text;
    private Long upvotes;
    private Long downvotes;
    private Long numberOfComments;
    private ArrayList<String> tags;

    public Post(String postID, String heading, String text, Long upvotes, Long downvotes, Long numberOfComments, ArrayList<String> tags){
        this.postID = postID;
        this.heading = heading;
        this.text = text;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.numberOfComments = numberOfComments;
        this.tags = tags;
    }

    public void setUpvotes(Long upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(Long downvotes) {
        this.downvotes = downvotes;
    }

    public void setNumberOfComments(Long numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public String getPostID() {
        return postID;
    }

    public String getHeading() {
        return heading;
    }

    public String getText() {
        return text;
    }

    public Long getUpvotes() {
        return upvotes;
    }

    public Long getDownvotes() {
        return downvotes;
    }

    public Long getNumberOfComments() {
        return numberOfComments;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

}
