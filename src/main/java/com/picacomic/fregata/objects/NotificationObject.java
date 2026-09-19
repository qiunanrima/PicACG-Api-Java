package com.picacomic.fregata.objects;

import com.google.gson.annotations.SerializedName;

/**
 * 通知条目。
 * Notification entry.
 */
public class NotificationObject {
    String content;
    ThumbnailObject cover;

    @SerializedName("created_at")
    String createdAt;
    String link;

    @SerializedName("_id")
    String notificationId;

    @SerializedName("_redirectId")
    String redirectId;
    String redirectType;

    @SerializedName("_sender")
    UserProfileObject sender;
    boolean system;
    String title;

    public NotificationObject(String notificationId, String title, String content, String redirectId, String redirectType, String link, boolean system, ThumbnailObject cover, UserProfileObject sender, String createdAt) {
        this.notificationId = notificationId;
        this.title = title;
        this.content = content;
        this.redirectId = redirectId;
        this.redirectType = redirectType;
        this.link = link;
        this.system = system;
        this.cover = cover;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public String getNotificationId() {
        return this.notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRedirectId() {
        return this.redirectId;
    }

    public void setRedirectId(String redirectId) {
        this.redirectId = redirectId;
    }

    public String getRedirectType() {
        return this.redirectType;
    }

    public void setRedirectType(String redirectType) {
        this.redirectType = redirectType;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isSystem() {
        return this.system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public ThumbnailObject getCover() {
        return this.cover;
    }

    public void setCover(ThumbnailObject cover) {
        this.cover = cover;
    }

    public UserProfileObject getSender() {
        return this.sender;
    }

    public void setSender(UserProfileObject sender) {
        this.sender = sender;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String toString() {
        return "NotificationObject{notificationId='" + this.notificationId + "', title='" + this.title + "', content='" + this.content + "', redirectId='" + this.redirectId + "', redirectType='" + this.redirectType + "', link='" + this.link + "', system=" + this.system + ", cover=" + this.cover + ", sender=" + this.sender + ", createdAt='" + this.createdAt + "'}";
    }
}
