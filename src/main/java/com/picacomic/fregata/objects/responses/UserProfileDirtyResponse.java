package com.picacomic.fregata.objects.responses;

/**
 * 资料增量（dirty）响应。
 * Dirty profile response.
 */
public class UserProfileDirtyResponse {
    boolean dirty;

    public UserProfileDirtyResponse(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String toString() {
        return "UserProfileDirtyResponse{dirty=" + this.dirty + '}';
    }
}
