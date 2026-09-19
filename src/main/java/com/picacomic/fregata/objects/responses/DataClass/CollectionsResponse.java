package com.picacomic.fregata.objects.responses.DataClass;

import com.picacomic.fregata.objects.CollectionObject;
import java.util.ArrayList;

/**
 * 合集列表响应。
 * Collections response.
 */
public class CollectionsResponse {
    ArrayList<CollectionObject> collections;

    public CollectionsResponse(ArrayList<CollectionObject> collections) {
        this.collections = collections;
    }

    public ArrayList<CollectionObject> getCollections() {
        return this.collections;
    }

    public void setCollections(ArrayList<CollectionObject> collections) {
        this.collections = collections;
    }

    public String toString() {
        return "CollectionsResponse{collections=" + this.collections + '}';
    }
}
