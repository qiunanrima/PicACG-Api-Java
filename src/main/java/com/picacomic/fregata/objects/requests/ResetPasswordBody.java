package com.picacomic.fregata.objects.requests;

/**
 * 重置密码请求体。
 * Reset-password request body.
 */
public class ResetPasswordBody {
    String answer;
    String email;
    int questionNo;

    public ResetPasswordBody(String email, int questionNo, String answer) {
        this.email = email;
        this.questionNo = questionNo;
        this.answer = answer;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getQuestionNo() {
        return this.questionNo;
    }

    public void setQuestionNo(int questionNo) {
        this.questionNo = questionNo;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String toString() {
        return "ResetPasswordBody{email='" + this.email + "', questionNo=" + this.questionNo + ", answer='" + this.answer + "'}";
    }
}
