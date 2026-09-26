package com.picacomic.fregata.objects.responses;

import java.util.ArrayList;

/**
 * Waka 初始化响应。
 * Waka init response.
 */
public class WakaInitResponse {
    String adKeyword;
    ArrayList<String> addresses;
    String message;
    String status;
    String waka;

    public WakaInitResponse(String status, ArrayList<String> addresses, String waka, String adKeyword, String message) {
        this.status = status;
        this.addresses = addresses;
        this.waka = waka;
        this.adKeyword = adKeyword;
        this.message = message;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getAddresses() {
        return this.addresses;
    }

    public void setAddresses(ArrayList<String> addresses) {
        this.addresses = addresses;
    }

    public String getWaka() {
        return this.waka;
    }

    public void setWaka(String waka) {
        this.waka = waka;
    }

    public String getAdKeyword() {
        return this.adKeyword;
    }

    public void setAdKeyword(String adKeyword) {
        this.adKeyword = adKeyword;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return "WakaInitResponse{status='" + this.status + "', addresses=" + this.addresses + ", waka='" + this.waka + ", adKeyword='" + this.adKeyword + ", message='" + this.message + "'}";
    }
}
