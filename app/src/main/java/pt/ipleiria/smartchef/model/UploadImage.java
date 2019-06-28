package pt.ipleiria.smartchef.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

public class UploadImage implements Serializable {

    private String url;
    private Bitmap bitmap;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
