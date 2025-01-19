package com.demo.lifecyclemgmt;

public class BlobInfo {
    private String container;
    private String blob;

    public BlobInfo(String container, String blob) {
        this.container = container;
        this.blob = blob;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getBlob() {
        return blob;
    }

    public void setBlob(String blob) {
        this.blob = blob;
    }
}
