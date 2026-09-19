package com.picacomic.fregata.objects.responses.DataClass.ProfileCommentsResponse;

import com.picacomic.fregata.objects.ProfileCommentObject;
import java.util.ArrayList;

/**
 * 我的评论分页数据。
 * Profile comments page data.
 */
public class ProfileCommentsData {
    ArrayList<ProfileCommentObject> docs;
    int limit;
    int page;
    int pages;
    int total;

    public ProfileCommentsData(int total, int limit, int page, int pages, ArrayList<ProfileCommentObject> docs) {
        this.total = total;
        this.limit = limit;
        this.page = page;
        this.pages = pages;
        this.docs = docs;
    }

    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total) {
        this.total = total;
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

    public ArrayList<ProfileCommentObject> getDocs() {
        return this.docs;
    }

    public void setDocs(ArrayList<ProfileCommentObject> docs) {
        this.docs = docs;
    }
}
