package com.picacomic.fregata.objects.requests;

/**
 * 头像更新请求体。
 * Avatar update request body.
 */
public class AvatarBody {
    String avatar;

    public AvatarBody(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String toString() {
        return "AvatarBody{avatar='" + this.avatar + "'}";
    }
}
