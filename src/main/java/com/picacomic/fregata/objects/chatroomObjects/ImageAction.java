package com.picacomic.fregata.objects.chatroomObjects;

/**
 * 图片显示开关动作。
 * Image toggle action.
 */
public class ImageAction extends ChatroomSystemAction {
    String from;
    boolean toggle;

    public ImageAction(String str, String str2) {
        super(str, str2);
    }

    public ImageAction(String str, String str2, boolean toggle, String from) {
        super(str, str2);
        this.toggle = toggle;
        this.from = from;
    }

    public boolean isToggle() {
        return this.toggle;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override // com.picacomic.fregata.objects.chatroomObjects.ChatroomSystemAction
    public String toString() {
        return "ImageAction{action=" + this.action + ", toggle=" + this.toggle + ", from='" + this.from + "'}";
    }
}
