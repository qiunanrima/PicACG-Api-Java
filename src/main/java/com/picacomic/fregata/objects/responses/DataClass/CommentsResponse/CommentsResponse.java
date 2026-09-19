package com.picacomic.fregata.objects.responses.DataClass.CommentsResponse;

import com.picacomic.fregata.objects.CommentObject;
import java.util.ArrayList;

/**
 * 评论列表响应。
 * Comments response.
 */
public class CommentsResponse {
    CommentsData comments;
    ArrayList<CommentObject> topComments;

    public CommentsResponse(CommentsData comments, ArrayList<CommentObject> topComments) {
        this.comments = comments;
        this.topComments = topComments;
    }

    public CommentsData getComments() {
        return this.comments;
    }

    public void setComments(CommentsData comments) {
        this.comments = comments;
    }

    public ArrayList<CommentObject> getTopComments() {
        return this.topComments;
    }

    public void setTopComments(ArrayList<CommentObject> topComments) {
        this.topComments = topComments;
    }

    public String toString() {
        return "CommentsResponse{comments=" + this.comments + ", topComments=" + this.topComments + '}';
    }
}
