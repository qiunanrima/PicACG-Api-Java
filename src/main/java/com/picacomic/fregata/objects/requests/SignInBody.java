package com.picacomic.fregata.objects.requests;

import com.google.gson.annotations.SerializedName;

/**
 * 登录请求体。
 * Sign-in request body.
 */
public class SignInBody {

    @SerializedName("email")
    String email;

    @SerializedName("password")
    String password;

    public SignInBody(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public SignInBody() {
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
