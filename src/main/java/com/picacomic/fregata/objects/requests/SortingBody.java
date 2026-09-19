package com.picacomic.fregata.objects.requests;

import java.util.ArrayList;

/**
 * 高级搜索排序请求体。
 * Advanced-search sorting request body.
 */
public class SortingBody {
    ArrayList<String> categories;
    String keyword;
    String sort;

    public SortingBody(String keyword, String sort, ArrayList<String> categories) {
        this.keyword = keyword;
        this.sort = sort;
        this.categories = categories;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSort() {
        return this.sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public ArrayList<String> getCategories() {
        return this.categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String toString() {
        return "SortingBody{keyword='" + this.keyword + "', sort='" + this.sort + "', categories=" + this.categories + '}';
    }
}
