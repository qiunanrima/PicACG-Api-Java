package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.ChatroomListObject;
import java.util.ArrayList;

/**
 * 聊天室列表响应。
 * Chatroom list response.
 */
public class ChatroomListResponse {
    ArrayList<ChatroomListObject> chatList;

    public ChatroomListResponse(ArrayList<ChatroomListObject> chatList) {
        this.chatList = chatList;
    }

    public ArrayList<ChatroomListObject> getChatList() {
        return this.chatList;
    }

    public void setChatList(ArrayList<ChatroomListObject> chatList) {
        this.chatList = chatList;
    }
}
