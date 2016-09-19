package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class for Imgur's response data of uploading images.
 *
 * @author beta
 */
public class ImgurResponseModel {

    public Image data;
    public boolean success;
    public int status;


    public static class Image {

        @SerializedName("link")
        public String imageUrl;

    }

}
