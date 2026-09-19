package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * 漫画详情。
 * Comic detail.
 */
public class ComicDetailObject {
    boolean allowComment;
    boolean allowDownload;
    String author;
    ArrayList<String> categories;
    String chineseTeam;

    @SerializedName("_id")
    String comicId;
    int commentsCount;

    @SerializedName("created_at")
    String createdAt;

    @SerializedName("_creator")
    CreatorObject creator;
    String description;

    @SerializedName("epsCount")
    int episodeCount;
    boolean finished;
    boolean isFavourite;
    boolean isLiked;
    int likesCount;
    int pagesCount;
    ArrayList<String> tags;
    ThumbnailObject thumb;
    String title;

    @SerializedName("updated_at")
    String updatedAt;
    int viewsCount;

    public ComicDetailObject() {
    }

    public ComicDetailObject(String comicId, String title, String author, String description, String chineseTeam, CreatorObject creator, ThumbnailObject thumb, ArrayList<String> categories, ArrayList<String> tags, int commentsCount, int pagesCount, int episodeCount, int likesCount, int viewsCount, boolean finished, boolean isFavourite, boolean isLiked, boolean allowDownload, boolean allowComment, String updatedAt, String createdAt) {
        this.comicId = comicId;
        this.title = title;
        this.author = author;
        this.description = description;
        this.chineseTeam = chineseTeam;
        this.creator = creator;
        this.thumb = thumb;
        this.categories = categories;
        this.tags = tags;
        this.commentsCount = commentsCount;
        this.pagesCount = pagesCount;
        this.episodeCount = episodeCount;
        this.likesCount = likesCount;
        this.viewsCount = viewsCount;
        this.finished = finished;
        this.isFavourite = isFavourite;
        this.isLiked = isLiked;
        this.allowDownload = allowDownload;
        this.allowComment = allowComment;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    public void updateWithComicDetailObject(ComicDetailObject comicId) {
        this.comicId = comicId.comicId;
        this.title = comicId.title;
        this.author = comicId.author;
        this.description = comicId.description;
        this.chineseTeam = comicId.chineseTeam;
        this.creator = comicId.creator;
        this.thumb = comicId.thumb;
        this.categories = comicId.categories;
        this.tags = comicId.tags;
        this.commentsCount = comicId.commentsCount;
        this.pagesCount = comicId.pagesCount;
        this.episodeCount = comicId.episodeCount;
        this.likesCount = comicId.likesCount;
        this.viewsCount = comicId.viewsCount;
        this.finished = comicId.finished;
        this.isFavourite = comicId.isFavourite;
        this.isLiked = comicId.isLiked;
        this.allowDownload = comicId.allowDownload;
        this.allowComment = comicId.allowComment;
        this.updatedAt = comicId.updatedAt;
        this.createdAt = comicId.createdAt;
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

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChineseTeam() {
        return this.chineseTeam;
    }

    public void setChineseTeam(String chineseTeam) {
        this.chineseTeam = chineseTeam;
    }

    public CreatorObject getCreator() {
        return this.creator;
    }

    public void setCreator(CreatorObject creator) {
        this.creator = creator;
    }

    public ThumbnailObject getThumb() {
        return this.thumb;
    }

    public void setThumb(ThumbnailObject thumb) {
        this.thumb = thumb;
    }

    public ArrayList<String> getCategories() {
        return this.categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public ArrayList<String> getTags() {
        return this.tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public int getCommentsCount() {
        return this.commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getPagesCount() {
        return this.pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public int getEpisodeCount() {
        return this.episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public int getLikesCount() {
        return this.likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getViewsCount() {
        return this.viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isFavourite() {
        return this.isFavourite;
    }

    public void setFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public boolean isLiked() {
        return this.isLiked;
    }

    public void setLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean isAllowDownload() {
        return this.allowDownload;
    }

    public void setAllowDownload(boolean allowDownload) {
        this.allowDownload = allowDownload;
    }

    public boolean isAllowComment() {
        return this.allowComment;
    }

    public void setAllowComment(boolean allowComment) {
        this.allowComment = allowComment;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String toString() {
        return "ComicDetailObject{comicId='" + this.comicId + "', title='" + this.title + "', author='" + this.author + "', description='" + this.description + "', chineseTeam='" + this.chineseTeam + "', creator=" + this.creator + ", thumb=" + this.thumb + ", categories=" + this.categories + ", tags=" + this.tags + ", commentsCount=" + this.commentsCount + ", pagesCount=" + this.pagesCount + ", episodeCount=" + this.episodeCount + ", likesCount=" + this.likesCount + ", viewsCount=" + this.viewsCount + ", finished=" + this.finished + ", isFavourite=" + this.isFavourite + ", isLiked=" + this.isLiked + ", allowDownload=" + this.allowDownload + ", allowComment=" + this.allowComment + ", updatedAt='" + this.updatedAt + "', createdAt='" + this.createdAt + "'}";
    }
}
