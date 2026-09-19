package com.picacomic.fregata.objects.requests;

/**
 * 用户
 * User-id request body.
 */
public class UserIdBody {
    String userId;

    public UserIdBody(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
