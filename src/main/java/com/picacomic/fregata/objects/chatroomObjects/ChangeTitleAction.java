package com.picacomic.fregata.objects.chatroomObjects;

/**
 * 修改头衔动作。
 * Change-title action.
 */
public class ChangeTitleAction extends ChatroomSystemAction {
    String from;
    String title;
    String user;
    String user_id;

    public ChangeTitleAction(String str, String str2) {
        super(str, str2);
    }

    public ChangeTitleAction(String str, String str2, String title, String from, String user, String user_id) {
        super(str, str2);
        this.title = title;
        this.from = from;
        this.user = user;
        this.user_id = user_id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser_id() {
        return this.user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override // com.picacomic.fregata.objects.chatroomObjects.ChatroomSystemAction
    public String toString() {
        return "ChangeTitleAction{action='" + this.action + "', title='" + this.title + "', from='" + this.from + "', user='" + this.user + "', user_id='" + this.user_id + "'}";
    }
}
