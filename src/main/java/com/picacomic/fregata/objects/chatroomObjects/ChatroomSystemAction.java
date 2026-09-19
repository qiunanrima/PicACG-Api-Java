package com.picacomic.fregata.objects.chatroomObjects;

/**
 * 聊天室系统动作基类。
 * Base chatroom system action.
 */
public class ChatroomSystemAction {
    public String action;
    public String email;

    public ChatroomSystemAction(String action, String email) {
        this.action = action;
        this.email = email;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String toString() {
        return "ChatroomSystemAction{action='" + this.action + "', email='" + this.email + "'}";
    }
}
