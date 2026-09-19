package com.picacomic.fregata.objects.responses.DataClass.ComicListResponse;

/**
 * 漫画列表响应。
 * Comics response.
 */
public class ComicListResponse {
    ComicListData comics;

    public ComicListResponse(ComicListData comics) {
        this.comics = comics;
    }

    public ComicListData getComics() {
        return this.comics;
    }

    public void setComics(ComicListData comics) {
        this.comics = comics;
    }

    public String toString() {
        return "ComicListResponse{, comics=" + this.comics + '}';
    }
}
