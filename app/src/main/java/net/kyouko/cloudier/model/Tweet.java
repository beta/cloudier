package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class for a piece of tweet.
 *
 * @author beta
 */
public class Tweet extends SourceTweet {

    public final static int STATUS_NORMAL = 0;
    public final static int STATUS_DELETED_BY_SYSTEM = 1;
    public final static int STATUS_UNDER_REVIEW = 2;
    public final static int STATUS_DELETED_BY_USER = 3;
    public final static int STATUS_DELETED_BY_ORIGIN = 4;

    public final static int TYPE_ORIGINAL = 1;
    public final static int TYPE_RETWEET = 2;
    public final static int TYPE_PRIVATE_MESSAGE = 3;
    public final static int TYPE_REPLY = 4;
    public final static int TYPE_EMPTY_REPLY = 5;
    public final static int TYPE_MENTION = 6;
    public final static int TYPE_COMMENT = 7;


    @SerializedName("source")
    public SourceTweet sourceTweet;

}
