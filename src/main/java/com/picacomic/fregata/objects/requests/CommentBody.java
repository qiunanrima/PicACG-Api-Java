package com.picacomic.fregata.objects.requests;

/**
 * 发送评论请求体。
 * Post-comment request body.
 */
public class CommentBody {
    String content;

    public CommentBody(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
