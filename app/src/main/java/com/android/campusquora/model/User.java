package com.android.campusquora.model;

public class User {
    public static final String FIELD_PROFILE_PIC = "profile pic";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_MOBILE_NO = "mobile";
    public static final String FIELD_BIO = "bio";

    public String Uid;
    public String profilePic;
    public String email;
    public String name;
    public String mobile;
    public String bio;

    public User() {
    }

    public User(String Uid, String email) {
        this.Uid = Uid;
        this.email = email;
    }

    public static String getFieldProfilePic() {
        return FIELD_PROFILE_PIC;
    }

    public static String getFieldEmail() {
        return FIELD_EMAIL;
    }

    public static String getFieldName() {
        return FIELD_NAME;
    }

    public static String getFieldMobileNo() {
        return FIELD_MOBILE_NO;
    }

    public static String getFieldBio() {
        return FIELD_BIO;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUid() {
        return Uid;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getBio() {
        return bio;
    }
}
