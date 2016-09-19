package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class for result of uploading an image.
 *
 * @author beta
 */
public class UploadImageResult {

    @SerializedName("imgurl")
    public String imageUrl;

}
