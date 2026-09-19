package com.picacomic.fregata.objects.responses.DataClass.ComicPageResponse;

import com.picacomic.fregata.objects.ComicEpisodeObject;

/**
 * 漫画图片页响应。
 * Comic pages response.
 */
public class ComicPagesResponse {
    ComicEpisodeObject ep;
    ComicPageData pages;

    public ComicPagesResponse(ComicPageData pages, ComicEpisodeObject ep) {
        this.pages = pages;
        this.ep = ep;
    }

    public ComicPageData getPages() {
        return this.pages;
    }

    public void setPages(ComicPageData pages) {
        this.pages = pages;
    }

    public ComicEpisodeObject getEp() {
        return this.ep;
    }

    public void setEp(ComicEpisodeObject ep) {
        this.ep = ep;
    }

    public String toString() {
        return "ComicPagesResponse{pages=" + this.pages + ", ep=" + this.ep + '}';
    }
}
