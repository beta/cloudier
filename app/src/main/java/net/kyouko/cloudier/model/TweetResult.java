package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class for results of posting tweets.
 *
 * @author beta
 */
public class TweetResult {

    @SerializedName("id")
    public String tweetId;
    @SerializedName("time")
    public String timestamp;

}
