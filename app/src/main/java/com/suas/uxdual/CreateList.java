package com.suas.uxdual;

public class CreateList {

    private String image_title;
    private Integer image_id;
    private String image_location;
    private String thumb_location;

    String getImage_title() {
        return image_title;
    }

    void setImage_title(String android_version_name) {
        this.image_title = android_version_name;
    }

    public Integer getImage_ID() {
        return image_id;
    }

    public void setImage_ID(Integer android_image_url) {
        this.image_id = android_image_url;
    }

    void setImage_Location(String pathName) {
        this.image_location = pathName;
    }

    void setThumb_location(String pathName) {
        this.thumb_location = pathName;
    }

    String getImage_Location() {
        return this.image_location;
    }

    String getThumb_location() {
        return this.thumb_location;
    }
}

