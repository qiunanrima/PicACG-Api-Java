package com.picacomic.fregata.objects.responses;

/**
 * 密码相关响应。
 * Password-related response.
 */
public class PasswordResponse {
    String password;

    public PasswordResponse(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "PasswordResponse{password='" + this.password + "'}";
    }
}
