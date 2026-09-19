package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;

/**
 * 分区条目。
 * Category entry.
 */
public class CategoryObject {

    @SerializedName("_id")
    String categoryId;
    String description;
    boolean isWeb;
    String link;
    ThumbnailObject thumb;
    String title;

    public CategoryObject() {
    }

    public CategoryObject(String categoryId, String title, String description, ThumbnailObject thumb, boolean isWeb, String link) {
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.thumb = thumb;
        this.isWeb = isWeb;
        this.link = link;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ThumbnailObject getThumb() {
        return this.thumb;
    }

    public void setThumb(ThumbnailObject thumb) {
        this.thumb = thumb;
    }

    public boolean isWeb() {
        return this.isWeb;
    }

    public void setWeb(boolean isWeb) {
        this.isWeb = isWeb;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String toString() {
        return "CategoryObject{categoryId='" + this.categoryId + "', title='" + this.title + "', description='" + this.description + "', thumb=" + this.thumb + ", isWeb=" + this.isWeb + ", link='" + this.link + "'}";
    }
}
