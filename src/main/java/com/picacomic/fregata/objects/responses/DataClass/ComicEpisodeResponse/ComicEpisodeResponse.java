package com.picacomic.fregata.objects.responses.DataClass.ComicEpisodeResponse;

/**
 * 章节列表响应。
 * Episodes response.
 */
public class ComicEpisodeResponse {
    ComicEpisodeData eps;

    public ComicEpisodeResponse() {
    }

    public ComicEpisodeResponse(ComicEpisodeData eps) {
        this.eps = eps;
    }

    public ComicEpisodeData getEps() {
        return this.eps;
    }

    public void setEps(ComicEpisodeData eps) {
        this.eps = eps;
    }

    public String toString() {
        return "ComicEpisodeResponse{eps=" + this.eps + '}';
    }
}
