package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for a timeline of tweets.
 *
 * @author beta
 */
public class Timeline {

    public final static int PAGE_FLAG_LATEST = 0;
    public final static int PAGE_FLAG_DOWN = 1;
    public final static int PAGE_FLAG_UP = 2;


    @SerializedName("info")
    public List<Tweet> tweets = new ArrayList<>();
    @SerializedName("user")
    public Map<String, String> users = new HashMap<>();

}
