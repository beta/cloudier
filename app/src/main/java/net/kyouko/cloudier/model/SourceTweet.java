package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Class for a piece of tweet retweeted by another tweet.
 *
 * @author beta
 */
public class SourceTweet implements Serializable {

    @SerializedName("id")
    public String id;
    @SerializedName("text")
    public String content;
    @SerializedName("origtext")
    public String originalContent;
    @SerializedName("self")
    public boolean sentBySelf;
    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("image")
    public List<String> imageUrls;

    @SerializedName("name")
    public String username;
    @SerializedName("nick")
    public String nickname;
    @SerializedName("openid")
    public String openId;
    @SerializedName("https_head")
    public String avatarUrl;
    @SerializedName("isvip")
    public boolean isVip;

    @SerializedName("mcount")
    public int commentCount;
    @SerializedName("count")
    public int retweetCount;
    @SerializedName("likecount")
    public int likeCount;

    @SerializedName("status")
    public int status;
    @SerializedName("type")
    public int type;

}
