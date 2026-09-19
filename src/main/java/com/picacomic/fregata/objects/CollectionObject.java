package com.picacomic.fregata.objects;

import java.util.ArrayList;

/**
 * 合集条目。
 * Collection entry.
 */
public class CollectionObject {
    ArrayList<ComicListObject> comics;
    String title;

    public CollectionObject(String title, ArrayList<ComicListObject> comics) {
        this.title = title;
        this.comics = comics;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<ComicListObject> getComics() {
        return this.comics;
    }

    public void setComics(ArrayList<ComicListObject> comics) {
        this.comics = comics;
    }

    public String toString() {
        return "CollectionObject{title='" + this.title + "', comics=" + this.comics + '}';
    }
}
