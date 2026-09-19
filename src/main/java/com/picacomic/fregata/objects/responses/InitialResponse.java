package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.CategoryObject;
import com.picacomic.fregata.objects.LatestApplicationObject;
import com.picacomic.fregata.objects.NotificationObject;
import java.util.ArrayList;

/**
 * 初始化响应。
 * Init response.
 */
public class InitialResponse {
    public ArrayList<CategoryObject> categories;
    public String imageServer;
    public boolean isIdUpdated;
    public boolean isPunched;
    public LatestApplicationObject latestApplication;
    public NotificationObject notification;

    public LatestApplicationObject getLatestApplication() {
        return this.latestApplication;
    }

    public void setLatestApplication(LatestApplicationObject latestApplication) {
        this.latestApplication = latestApplication;
    }

    public boolean isPunched() {
        return this.isPunched;
    }

    public void setPunched(boolean isPunched) {
        this.isPunched = isPunched;
    }

    public String getImageServer() {
        return this.imageServer;
    }

    public void setImageServer(String imageServer) {
        this.imageServer = imageServer;
    }

    public ArrayList<CategoryObject> getCategories() {
        return this.categories;
    }

    public void setCategories(ArrayList<CategoryObject> categories) {
        this.categories = categories;
    }

    public NotificationObject getNotification() {
        return this.notification;
    }

    public void setNotification(NotificationObject notification) {
        this.notification = notification;
    }

    public String toString() {
        return "InitialResponse{latestApplication=" + this.latestApplication + ", isPunched=" + this.isPunched + ", imageServer='" + this.imageServer + "', categories=" + this.categories + ", notification=" + this.notification + ", isIdUpdated=" + this.isIdUpdated + '}';
    }

    public boolean isIdUpdated() {
        return this.isIdUpdated;
    }

    public void setIdUpdated(boolean isIdUpdated) {
        this.isIdUpdated = isIdUpdated;
    }
}
