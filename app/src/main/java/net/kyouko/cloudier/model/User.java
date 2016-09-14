package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class for a user.
 *
 * @author beta
 */
public class User {

    @SerializedName("name")
    public String username;
    @SerializedName("nick")
    public String nickname;
    @SerializedName("https_head")
    public String avatarUrl;
    public String introduction;
    @SerializedName("fansnum")
    public int followerCount;
    @SerializedName("idolnum")
    public int followingCount;
    @SerializedName("tweetnum")
    public int tweetCount;

    @SerializedName("ismyidol")
    public boolean followed;
    @SerializedName("ismyfans")
    public boolean following;
    @SerializedName("ismyblack")
    public boolean blocked;

    @SerializedName("isvip")
    public boolean isVip;

}
