package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.ComicListObject;
import java.util.ArrayList;

/**
 * 随机 / 推荐漫画响应。
 * Random or recommended comics response.
 */
public class ComicRandomListResponse {
    ArrayList<ComicListObject> comics;

    public ComicRandomListResponse(ArrayList<ComicListObject> comics) {
        this.comics = comics;
    }

    public ArrayList<ComicListObject> getComics() {
        return this.comics;
    }

    public void setComics(ArrayList<ComicListObject> comics) {
        this.comics = comics;
    }
}
