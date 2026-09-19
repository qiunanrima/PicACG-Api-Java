package com.picacomic.fregata.objects.responses;

/**
 * 签到响应。
 * Punch-in response.
 */
public class PunchInResponse {
    PunchInObject res;

    public PunchInResponse(PunchInObject res) {
        this.res = res;
    }

    public PunchInObject getRes() {
        return this.res;
    }

    public void setRes(PunchInObject res) {
        this.res = res;
    }

    public String toString() {
        return "PunchInResponse{res=" + this.res + '}';
    }
}
