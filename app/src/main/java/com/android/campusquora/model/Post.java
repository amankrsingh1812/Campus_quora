package com.android.campusquora.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;

public class Post implements Parcelable {
    private String postID;
    private String heading;
    private String text;
    private Long upvotes;
    private Long downvotes;
    private Long numberOfComments;
    private ArrayList<String> tags;
    private Timestamp postTime;
    public Post(String postID, String heading, String text, Long upvotes, Long downvotes, Long numberOfComments, ArrayList<String> tags,Timestamp postTime){
        this.postID = postID;
        this.heading = heading;
        this.text = text;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.numberOfComments = numberOfComments;
        this.tags = tags;
        this.postTime=postTime;
    }

    protected Post(Parcel in) {
        postID = in.readString();
        heading = in.readString();
        text = in.readString();
        if (in.readByte() == 0) {
            upvotes = null;
        } else {
            upvotes = in.readLong();
        }
        if (in.readByte() == 0) {
            downvotes = null;
        } else {
            downvotes = in.readLong();
        }
        if (in.readByte() == 0) {
            numberOfComments = null;
        } else {
            numberOfComments = in.readLong();
        }
        tags = in.createStringArrayList();
        postTime = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public Timestamp getPostTime() {
        return postTime;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(postID);
        parcel.writeString(heading);
        parcel.writeString(text);
        if (upvotes == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(upvotes);
        }
        if (downvotes == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(downvotes);
        }
        if (numberOfComments == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(numberOfComments);
        }
        parcel.writeStringList(tags);
        parcel.writeParcelable(postTime, i);
    }
}
