package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;

/**
 * 用户简要信息。
 * Basic user info.
 */
public class UserBasicObject {
    ThumbnailObject avatar;
    String character;
    int exp;
    String gender;
    int level;
    String name;

    @SerializedName("_id")
    String userId;
    boolean verified;


    public UserBasicObject(String userId, String name, String gender, String character, int exp, int level, boolean verified, ThumbnailObject avatar) {
        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.character = character;
        this.exp = exp;
        this.level = level;
        this.verified = verified;
        this.avatar = avatar;
    }

    public UserBasicObject(UserProfileObject userId) {
        this.userId = userId.getUserId();
        this.name = userId.getName();
        this.gender = userId.getGender();
        this.character = userId.getCharacter();
        this.exp = userId.getExp();
        this.level = userId.getLevel();
        this.verified = userId.isVerified();
        this.avatar = userId.getAvatar();
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCharacter() {
        return this.character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public ThumbnailObject getAvatar() {
        return this.avatar;
    }

    public void setAvatar(ThumbnailObject avatar) {
        this.avatar = avatar;
    }

    public String toString() {
        return "UserBasicObject{userId='" + this.userId + "', name='" + this.name + "', gender='" + this.gender + "', character='" + this.character + "', exp=" + this.exp + ", level=" + this.level + ", verified=" + this.verified + ", avatar=" + this.avatar + '}';
    }


}
