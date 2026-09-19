package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.ThumbnailObject;

/**
 * 头像更新响应。
 * Avatar update response.
 */
public class PutAvatarResponse {
    ThumbnailObject avatar;

    public PutAvatarResponse(ThumbnailObject avatar) {
        this.avatar = avatar;
    }

    public ThumbnailObject getAvatar() {
        return this.avatar;
    }

    public void setAvatar(ThumbnailObject avatar) {
        this.avatar = avatar;
    }

    public String toString() {
        return "PutAvatarResponse{avatar=" + this.avatar + '}';
    }
}
