package com.picacomic.fregata.objects;

/**
 * 聊天系统消息。
 * System chat message.
 */
public class ChatSystemObject extends ChatBaseObject {
    String message;

    public ChatSystemObject(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return "ChatSystemObject{message='" + this.message + "'}";
    }
}
