package com.picacomic.fregata.objects.responses.DataClass.NotificationsResponse;

import com.picacomic.fregata.objects.NotificationObject;
import java.util.ArrayList;

/**
 * 通知分页数据。
 * Notifications page data.
 */
public class NotificationsData {
    ArrayList<NotificationObject> docs;
    int limit;
    int page;
    int pages;
    int total;

    public NotificationsData(int limit, int page, int pages, int total, ArrayList<NotificationObject> docs) {
        this.limit = limit;
        this.page = page;
        this.pages = pages;
        this.total = total;
        this.docs = docs;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPages() {
        return this.pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public ArrayList<NotificationObject> getDocs() {
        return this.docs;
    }

    public void setDocs(ArrayList<NotificationObject> docs) {
        this.docs = docs;
    }

    public String toString() {
        return "NotificationsData{limit=" + this.limit + ", page=" + this.page + ", pages=" + this.pages + ", total=" + this.total + ", docs=" + this.docs + '}';
    }
}
