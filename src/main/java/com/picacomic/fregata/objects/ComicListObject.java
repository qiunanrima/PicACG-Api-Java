package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * 漫画列表项。
 * Comic list entry.
 */
public class ComicListObject {
    String author;
    ArrayList<String> categories;

    @SerializedName("_id")
    String comicId;

    @SerializedName("epsCount")
    int episodeCount;
    boolean finished;
    int likesCount;
    int pagesCount;
    ThumbnailObject thumb;
    String title;


    public ComicListObject(String comicId) {
        this.comicId = comicId;
        this.title = null;
        this.author = null;
        this.likesCount = 0;
        this.pagesCount = 1;
        this.episodeCount = 1;
        this.finished = false;
        this.categories = null;
        this.thumb = null;
    }

    public ComicListObject(String comicId, String title, String author, int likesCount, int pagesCount, int episodeCount, boolean finished, ArrayList<String> categories, ThumbnailObject thumb) {
        this.comicId = comicId;
        this.title = title;
        this.author = author;
        this.likesCount = likesCount;
        this.pagesCount = pagesCount;
        this.episodeCount = episodeCount;
        this.finished = finished;
        this.categories = categories;
        this.thumb = thumb;
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

    public int getLikesCount() {
        return this.likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
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

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public ArrayList<String> getCategories() {
        return this.categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public ThumbnailObject getThumb() {
        return this.thumb;
    }

    public void setThumb(ThumbnailObject thumb) {
        this.thumb = thumb;
    }

    public String toString() {
        return "ComicListObject{comicId='" + this.comicId + "', title='" + this.title + "', author='" + this.author + "', likesCount=" + this.likesCount + ", pagesCount=" + this.pagesCount + ", episodeCount=" + this.episodeCount + ", finished=" + this.finished + ", categories=" + this.categories + ", thumb=" + this.thumb + '}';
    }

}
