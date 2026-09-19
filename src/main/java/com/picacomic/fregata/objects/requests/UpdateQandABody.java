package com.picacomic.fregata.objects.requests;

/**
 * 更新密保问题请求体。
 * Update-security-questions request body.
 */
public class UpdateQandABody {
    String answer1;
    String answer2;
    String answer3;
    String question1;
    String question2;
    String question3;

    public UpdateQandABody(String question1, String question2, String question3, String answer1, String answer2, String answer3) {
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
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

    public String getAnswer1() {
        return this.answer1;
    }

    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    public String getAnswer2() {
        return this.answer2;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public String getAnswer3() {
        return this.answer3;
    }

    public void setAnswer3(String answer3) {
        this.answer3 = answer3;
    }

    public String toString() {
        return "UpdateQandABody{question1='" + this.question1 + "', question2='" + this.question2 + "', question3='" + this.question3 + "', answer1='" + this.answer1 + "', answer2='" + this.answer2 + "', answer3='" + this.answer3 + "'}";
    }
}
