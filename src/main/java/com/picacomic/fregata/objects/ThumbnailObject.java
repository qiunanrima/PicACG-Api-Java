package com.picacomic.fregata.objects;


/**
 * 图片 / 缩略图资源。
 * Image or thumbnail resource.
 */
public class ThumbnailObject {
    String fileServer;
    String originalName;
    String path;


    public ThumbnailObject() {
    }

    public ThumbnailObject(String fileServer, String path, String originalName) {
        this.fileServer = fileServer;
        this.path = path;
        this.originalName = originalName;
    }


    public String getFileServer() {
        return this.fileServer;
    }

    public void setFileServer(String fileServer) {
        this.fileServer = fileServer;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOriginalName() {
        return this.originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String toString() {
        return "ThumbnailObject{fileServer='" + this.fileServer + "', path='" + this.path + "', originalName='" + this.originalName + "'}";
    }

}
