package com.picacomic.fregata.objects.chatroomGameObjects;

/**
 * 小游戏初始化事件。
 * Chatroom game init event.
 */
public class ChatroomGameEmitInit {
    String userId;

    public ChatroomGameEmitInit(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String toString() {
        return "ChatroomGameEmitInit{userId='" + this.userId + "'}";
    }
}
