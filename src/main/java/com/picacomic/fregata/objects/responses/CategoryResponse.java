package com.picacomic.fregata.objects.responses;

import com.picacomic.fregata.objects.CategoryObject;
import java.util.ArrayList;

/**
 * 分区列表响应。
 * Categories response.
 */
public class CategoryResponse {
    public ArrayList<CategoryObject> categories;

    public CategoryResponse(ArrayList<CategoryObject> categories) {
        this.categories = categories;
    }

    public ArrayList<CategoryObject> getCategories() {
        return this.categories;
    }

    public void setCategories(ArrayList<CategoryObject> categories) {
        this.categories = categories;
    }

    public String toString() {
        return "CategoryResponse{categories=" + this.categories + '}';
    }
}
