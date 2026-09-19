package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.ComicDetailObject;

/**
 * 漫画详情响应。
 * Comic detail response.
 */
public class ComicDetailResponse {
    ComicDetailObject comic;

    public ComicDetailResponse(ComicDetailObject comic) {
        this.comic = comic;
    }

    public ComicDetailObject getComic() {
        return this.comic;
    }

    public void setComic(ComicDetailObject comic) {
        this.comic = comic;
    }

    public String toString() {
        return "ComicDetailResponse{comic=" + this.comic + '}';
    }
}
