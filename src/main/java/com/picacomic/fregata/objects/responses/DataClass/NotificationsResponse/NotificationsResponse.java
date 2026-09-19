package com.picacomic.fregata.objects.responses.DataClass.NotificationsResponse;

/**
 * 通知列表响应。
 * Notifications response.
 */
public class NotificationsResponse {
    NotificationsData notifications;

    public NotificationsResponse(NotificationsData notifications) {
        this.notifications = notifications;
    }

    public NotificationsData getNotifications() {
        return this.notifications;
    }

    public void setNotifications(NotificationsData notifications) {
        this.notifications = notifications;
    }

    public String toString() {
        return "NotificationsResponse{notifications=" + this.notifications + '}';
    }
}
