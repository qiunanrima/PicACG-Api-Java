package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;

/**
 * 评论所关联的漫画信息。
 * Comic reference carried by a comment.
 */
public class CommentComicIdTitleObject {

    @SerializedName("_id")
    String comicId;

    @SerializedName("title")
    String title;

    public CommentComicIdTitleObject(String comicId, String title) {
        this.comicId = comicId;
        this.title = title;
    }

    public String getComicId() {
        return this.comicId;
    }

    public void setComicId(String comicId) {
        this.comicId = comicId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return "CommentComicObject{comicId='" + this.comicId + "', title='" + this.title + "'}";
    }
}
