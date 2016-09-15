package net.kyouko.cloudier.event;

import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.util.TweetCardUtil;

import java.util.HashMap;

/**
 * Event for viewing tweets.
 *
 * @author beta
 */
public class ViewTweetEvent {

    public final static int TYPE_TWEET = 0;
    public final static int TYPE_ID = 1;


    public int type = TYPE_TWEET;

    public SourceTweet tweet;
    public HashMap<String, String> users;

    public int tweetId;
    public TweetCardUtil.Card card;


    public ViewTweetEvent(SourceTweet tweet, HashMap<String, String> users) {
        this(tweet, users, null);
    }


    public ViewTweetEvent(SourceTweet tweet, HashMap<String, String> users, TweetCardUtil.Card card) {
        type = TYPE_TWEET;
        this.tweet = tweet;
        this.users = users;
        this.card = card;
    }


    public ViewTweetEvent(int tweetId) {
        type = TYPE_ID;
        this.tweetId = tweetId;
    }

}
