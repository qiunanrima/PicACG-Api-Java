package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.UserProfileObject;

/**
 * 用户资料响应。
 * User profile response.
 */
public class UserProfileResponse {
    UserProfileObject user;

    public UserProfileResponse() {
    }

    public UserProfileResponse(UserProfileObject user) {
        this.user = user;
    }

    public UserProfileObject getUser() {
        return this.user;
    }

    public void setUser(UserProfileObject user) {
        this.user = user;
    }

    public String toString() {
        return "UserProfileResponse{user=" + this.user + '}';
    }
}
