package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

/**
 * Class for data updates.
 *
 * @author beta
 */
public class Update {

    @SerializedName("home")
    public int newHomeTweetCount;
    @SerializedName("create")
    public int newHomeOriginalTweetCount;
    @SerializedName("mentions")
    public int newMentionsCount;
    @SerializedName("fans")
    public int newFollowerCount;
    @SerializedName("private")
    public int newPrivateMessageCount;

}
