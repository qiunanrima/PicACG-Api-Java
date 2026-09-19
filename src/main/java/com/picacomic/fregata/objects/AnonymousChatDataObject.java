package com.picacomic.fregata.objects;

/**
 * 匿名聊天消息数据。
 * Anonymous chat message data.
 */
public class AnonymousChatDataObject {
    String id;
    String message;
    String name;
    String roomId;
    String userId;

    public AnonymousChatDataObject(String id, String userId, String name, String message, String roomId) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.message = message;
        this.roomId = roomId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String toString() {
        return "AnonymousChatDataObject{id='" + this.id + "', userId='" + this.userId + "', name='" + this.name + "', message='" + this.message + "', roomId='" + this.roomId + "'}";
    }
}
