package com.picacomic.fregata.objects.responses.DataClass.ProfileCommentsResponse;

/**
 * 我的评论响应。
 * Profile comments response.
 */
public class ProfileCommentsResponse {
    ProfileCommentsData comments;

    public ProfileCommentsResponse(ProfileCommentsData comments) {
        this.comments = comments;
    }

    public ProfileCommentsData getComments() {
        return this.comments;
    }

    public void setComments(ProfileCommentsData comments) {
        this.comments = comments;
    }
}
