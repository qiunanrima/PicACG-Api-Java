package com.picacomic.fregata.objects.chatroomGameObjects.listens;

/**
 * 小游戏确认事件。
 * Chatroom game confirm event.
 */
public class ChatroomGameEmitConfirm {
    String id;
    String options;

    public ChatroomGameEmitConfirm(String id, String options) {
        this.id = id;
        this.options = options;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOptions() {
        return this.options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String toString() {
        return "ChatroomGameEmitConfirm{id='" + this.id + "', options='" + this.options + "'}";
    }
}
