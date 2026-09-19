package com.picacomic.fregata.objects.chatroomObjects;

/**
 * 时间显示开关动作。
 * Time-toggle action.
 */
public class TimeAction extends ChatroomSystemAction {
    String from;
    boolean toggle;

    public TimeAction(String str, String str2) {
        super(str, str2);
    }

    public TimeAction(String str, String str2, boolean toggle, String from) {
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
        return "TimeAction{action=" + this.action + ", toggle=" + this.toggle + ", from='" + this.from + "'}";
    }
}
