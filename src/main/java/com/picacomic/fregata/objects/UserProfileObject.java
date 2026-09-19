package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * 用户资料。
 * User profile.
 */
public class UserProfileObject {

    @SerializedName("activation_date")
    String activationDate;
    ThumbnailObject avatar;
    String birthday;
    String character;
    ArrayList<String> characters;
    String email;
    int exp;
    String gender;
    boolean isPunched;
    int level;
    String name;
    String role;
    String slogan;
    String title;

    @SerializedName("_id")
    String userId;
    boolean verified;


    public UserProfileObject() {
    }

    public UserProfileObject(String userId, String email, String name, String title, String birthday, String gender, String slogan, String role, String character, ArrayList<String> characters, String activationDate, int exp, int level, boolean isPunched, boolean verified, ThumbnailObject avatar) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.title = title;
        this.birthday = birthday;
        this.gender = gender;
        this.slogan = slogan;
        this.role = role;
        this.character = character;
        this.characters = characters;
        this.activationDate = activationDate;
        this.exp = exp;
        this.level = level;
        this.isPunched = isPunched;
        this.verified = verified;
        this.avatar = avatar;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getSlogan() {
        return this.slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCharacter() {
        return this.character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public ArrayList<String> getCharacters() {
        return this.characters;
    }

    public String[] getCharactersStringArray() {
        if (this.characters == null || this.characters.size() <= 0) {
            return null;
        }
        String[] strArr = new String[this.characters.size()];
        for (int i = 0; i < this.characters.size(); i++) {
            strArr[i] = this.characters.get(i);
        }
        return strArr;
    }

    public void setCharacters(ArrayList<String> characters) {
        this.characters = characters;
    }

    public String getActivationDate() {
        return this.activationDate;
    }

    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isPunched() {
        return this.isPunched;
    }

    public void setPunched(boolean isPunched) {
        this.isPunched = isPunched;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public ThumbnailObject getAvatar() {
        return this.avatar;
    }

    public void setAvatar(ThumbnailObject avatar) {
        this.avatar = avatar;
    }

    public String toString() {
        return "UserProfileObject{userId='" + this.userId + "', email='" + this.email + "', name='" + this.name + "', title='" + this.title + "', birthday='" + this.birthday + "', gender='" + this.gender + "', slogan='" + this.slogan + "', role='" + this.role + "', character='" + this.character + "', characters='" + this.characters + "', activationDate='" + this.activationDate + "', exp=" + this.exp + ", level=" + this.level + ", isPunched=" + this.isPunched + ", verified=" + this.verified + ", avatar=" + this.avatar + '}';
    }


}
