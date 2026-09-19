package com.picacomic.fregata.objects.responses.DataClass.PostCommentResponse;

import com.google.gson.annotations.SerializedName;
import com.picacomic.fregata.objects.UserBasicObject;

/**
 * 发表评论结果数据。
 * Posted-comment data.
 */
public class PostCommentData {
    String content;

    @SerializedName("_user")
    UserBasicObject user;

    public PostCommentData() {
    }

    public PostCommentData(String content, UserBasicObject user) {
        this.content = content;
        this.user = user;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserBasicObject getUser() {
        return this.user;
    }

    public void setUser(UserBasicObject user) {
        this.user = user;
    }

    public String toString() {
        return "PostCommentData{content='" + this.content + "', user=" + this.user + '}';
    }
}
