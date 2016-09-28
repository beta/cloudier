package net.kyouko.cloudier.event;

/**
 * Event class for deleting a tweet.
 *
 * @author beta
 */
public class DeleteTweetEvent {

    public String tweetId;


    public DeleteTweetEvent(String tweetId) {
        this.tweetId = tweetId;
    }

}
