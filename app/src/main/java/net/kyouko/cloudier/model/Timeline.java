package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class for a timeline of tweets.
 *
 * @author beta
 */
public class Timeline implements Serializable {

    public final static int PAGE_FLAG_LATEST = 0;
    public final static int PAGE_FLAG_DOWN = 1;
    public final static int PAGE_FLAG_UP = 2;

    public final static int FLAG_HAS_MORE = 0;
    public final static int FLAG_NO_MORE = 1;


    @SerializedName("hasnext")
    public int hasMoreTweetsFlag = FLAG_NO_MORE;
    @SerializedName("info")
    public List<Tweet> tweets = new ArrayList<>();
    @SerializedName("user")
    public HashMap<String, String> users = new HashMap<>();


    public boolean containsTweet(Tweet newTweet) {
        for (Tweet tweet : tweets) {
            if (tweet.id.equals(newTweet.id)) {
                return true;
            }
        }
        return false;
    }

}
