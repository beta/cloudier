package net.kyouko.cloudier.event;

/**
 * Event for sharing tweets.
 *
 * @author beta
 */
public class ShareTweetEvent {

    public String tweetId;


    public ShareTweetEvent(String tweetId) {
        this.tweetId = tweetId;
    }

}
