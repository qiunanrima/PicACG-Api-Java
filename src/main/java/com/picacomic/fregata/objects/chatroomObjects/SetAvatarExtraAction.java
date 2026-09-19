package com.picacomic.fregata.objects.chatroomObjects;

/**
 * 设置额外头像动作。
 * Set-extra-avatar action.
 */
public class SetAvatarExtraAction extends ChatroomSystemAction {
    int extra;
    String from;
    String user;
    String user_id;

    public SetAvatarExtraAction(String str, String str2) {
        super(str, str2);
    }

    public SetAvatarExtraAction(String str, String str2, String user, String user_id, String from, int extra) {
        super(str, str2);
        this.user = user;
        this.user_id = user_id;
        this.from = from;
        this.extra = extra;
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

    public int getExtra() {
        return this.extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    @Override // com.picacomic.fregata.objects.chatroomObjects.ChatroomSystemAction
    public String toString() {
        return "HideAvatarAction{action='" + this.action + "', user='" + this.user + "', user_id='" + this.user_id + "', from='" + this.from + "', extra='" + this.extra + "'}";
    }
}
