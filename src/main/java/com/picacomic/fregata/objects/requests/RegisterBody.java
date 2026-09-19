package com.picacomic.fregata.objects.requests;

/**
 * 注册请求体。
 * Register request body.
 */
public class RegisterBody {
    String answer1;
    String answer2;
    String answer3;
    String birthday;
    String email;
    String gender;
    String name;
    String password;
    String question1;
    String question2;
    String question3;

    public RegisterBody() {
    }

    public RegisterBody(String name, String email, String password, String birthday, String gender, String question1, String question2, String question3, String answer1, String answer2, String answer3) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthday = birthday;
        this.gender = gender;
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
}
