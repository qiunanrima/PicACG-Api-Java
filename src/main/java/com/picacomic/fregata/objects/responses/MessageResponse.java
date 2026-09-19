package com.picacomic.fregata.objects.responses;

/**
 * 消息响应。
 * Message response.
 */
public class MessageResponse {
    String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return "MessageResponse{message='" + this.message + "'}";
    }
}
