package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;

/**
 * 匿名聊天动作数据。
 * Anonymous chat action data.
 */
public class AnonymousChatActionDataObject {

    @SerializedName("actionType")
    String actionType;
    AnonymousChatDataObject data;
    String responseType;

    public AnonymousChatActionDataObject(String actionType, String responseType, AnonymousChatDataObject data) {
        this.actionType = actionType;
        this.responseType = responseType;
        this.data = data;
    }

    public String getActionType() {
        return this.actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getResponseType() {
        return this.responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public AnonymousChatDataObject getData() {
        return this.data;
    }

    public void setData(AnonymousChatDataObject data) {
        this.data = data;
    }

    public String toString() {
        return "AnonymousChatActionDataObject{actionType='" + this.actionType + "', responseType='" + this.responseType + "', data=" + this.data + '}';
    }
}
