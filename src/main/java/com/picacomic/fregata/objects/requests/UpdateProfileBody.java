package com.picacomic.fregata.objects.requests;

/**
 * 更新资料请求体。
 * Update-profile request body.
 */
public class UpdateProfileBody {
    String slogan;

    public UpdateProfileBody(String slogan) {
        this.slogan = slogan;
    }

    public String getSlogan() {
        return this.slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String toString() {
        return "UpdateProfileBody{slogan='" + this.slogan + "'}";
    }
}
