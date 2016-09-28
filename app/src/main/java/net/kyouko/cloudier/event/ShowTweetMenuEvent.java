package net.kyouko.cloudier.event;

import net.kyouko.cloudier.model.SourceTweet;

/**
 * Event class for showing dropdown menu of a tweet.
 *
 * @author beta
 */
public class ShowTweetMenuEvent {

    public SourceTweet tweet;


    public ShowTweetMenuEvent(SourceTweet tweet) {
        this.tweet = tweet;
    }

}
