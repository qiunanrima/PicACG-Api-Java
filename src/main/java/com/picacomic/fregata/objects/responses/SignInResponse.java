package com.picacomic.fregata.objects.responses;

/**
 * 登录响应（令牌）。
 * Sign-in response (token).
 */
public class SignInResponse {
    String token;

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
