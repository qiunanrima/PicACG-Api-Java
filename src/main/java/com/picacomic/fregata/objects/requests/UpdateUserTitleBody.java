package com.picacomic.fregata.objects.requests;

/**
 * 修改用户头衔请求体。
 * Update-user-title request body.
 */
public class UpdateUserTitleBody {
    String title;

    public UpdateUserTitleBody(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return "UpdateUserTitleBody{title='" + this.title + "'}";
    }
}
