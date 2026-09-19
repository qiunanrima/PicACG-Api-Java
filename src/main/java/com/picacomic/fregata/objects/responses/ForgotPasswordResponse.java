package com.picacomic.fregata.objects.responses;

/**
 * 忘记密码响应（验证码）。
 * Forgot-password response (verification code).
 */
public class ForgotPasswordResponse {
    String question1;
    String question2;
    String question3;

    public ForgotPasswordResponse(String question1, String question2, String question3) {
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
    }

    public String getQuestion1() {
        return this.question1;
    }

    public void setQuestion1(String question1) {
        this.question1 = question1;
    }

    public String getQuestion2() {
        return this.question2;
    }

    public void setQuestion2(String question2) {
        this.question2 = question2;
    }

    public String getQuestion3() {
        return this.question3;
    }

    public void setQuestion3(String question3) {
        this.question3 = question3;
    }

    public String toString() {
        return "ForgotPasswordResponse{question1='" + this.question1 + "', question2='" + this.question2 + "', question3='" + this.question3 + "'}";
    }
}
