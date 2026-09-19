package com.picacomic.fregata.objects.chatroomObjects;

/**
 * 禁言动作。
 * Mute action.
 */
public class MuteAction extends ChatroomSystemAction {
    String from;
    int minutes;
    String user;
    String user_id;

    public MuteAction(String str, String str2) {
        super(str, str2);
    }

    public MuteAction(String str, String str2, int minutes, String user, String user_id, String from) {
        super(str, str2);
        this.minutes = minutes;
        this.user = user;
        this.user_id = user_id;
        this.from = from;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
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

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override // com.picacomic.fregata.objects.chatroomObjects.ChatroomSystemAction
    public String toString() {
        return "MuteAction{action=" + this.action + ", minutes=" + this.minutes + ", user='" + this.user + "', user_id='" + this.user_id + "', from='" + this.from + "'}";
    }
}
