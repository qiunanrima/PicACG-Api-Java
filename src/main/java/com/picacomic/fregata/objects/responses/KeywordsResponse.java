package com.picacomic.fregata.objects.responses;

import java.util.ArrayList;

/**
 * 搜索关键词响应。
 * Keywords response.
 */
public class KeywordsResponse {
    ArrayList<String> keywords;

    public KeywordsResponse(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    public ArrayList<String> getKeywords() {
        return this.keywords;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    public String toString() {
        return "KeywordsResponse{keywords=" + this.keywords + '}';
    }
}
