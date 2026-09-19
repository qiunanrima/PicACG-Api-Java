package com.picacomic.fregata.objects.responses;

/**
 * 评论置顶结果。
 * Comment pin result.
 */
public class CommentPostToTopResponse {
    boolean isTop;

    public CommentPostToTopResponse(boolean isTop) {
        this.isTop = isTop;
    }

    public boolean isTop() {
        return this.isTop;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public String toString() {
        return "TopCommentResponse{isTop=" + this.isTop + '}';
    }
}
