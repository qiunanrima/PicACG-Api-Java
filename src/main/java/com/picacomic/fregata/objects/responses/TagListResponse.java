package com.picacomic.fregata.objects.responses;

import java.util.ArrayList;

/**
 * 标签列表响应。
 * Tag list response.
 */
public class TagListResponse {
    ArrayList<String> tags;

    public TagListResponse(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<String> getTags() {
        return this.tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
}
