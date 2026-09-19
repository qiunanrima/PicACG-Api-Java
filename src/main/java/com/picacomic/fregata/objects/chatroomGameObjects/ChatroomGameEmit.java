package com.picacomic.fregata.objects.chatroomGameObjects;

/**
 * 聊天室小游戏事件。
 * Chatroom game event.
 */
public class ChatroomGameEmit<DataClass> {
    String action;
    DataClass data;

    public ChatroomGameEmit(String action) {
        this.action = action;
    }

    public ChatroomGameEmit(String action, DataClass data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public DataClass getData() {
        return this.data;
    }

    public void setData(DataClass data) {
        this.data = data;
    }

    public String toString() {
        return "ChatroomGameEmit{action='" + this.action + "', data=" + this.data + '}';
    }
}
