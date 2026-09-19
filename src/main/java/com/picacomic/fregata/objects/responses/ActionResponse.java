package com.picacomic.fregata.objects.responses;

/**
 * 操作结果响应（点赞、收藏等）。
 * Generic action result (like, favourite, ...).
 */
public class ActionResponse {
    String action;

    public ActionResponse(String action) {
        this.action = action;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
