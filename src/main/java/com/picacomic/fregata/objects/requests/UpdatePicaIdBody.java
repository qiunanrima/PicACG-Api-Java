package com.picacomic.fregata.objects.requests;

/**
 * 修改 Pica ID 请求体。
 * Update Pica ID request body.
 */
public class UpdatePicaIdBody {
    String email;
    String name;

    public UpdatePicaIdBody(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "UpdatePicaIdBody{email='" + this.email + "', name='" + this.name + "'}";
    }
}
