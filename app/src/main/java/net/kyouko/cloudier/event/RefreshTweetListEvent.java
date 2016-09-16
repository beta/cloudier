package net.kyouko.cloudier.event;

import net.kyouko.cloudier.model.Tweet;

/**
 * Event for refreshing tweet lists in tabs of comments and retweets.
 *
 * @author beta
 */
public class RefreshTweetListEvent {

    public int type = Tweet.TYPE_COMMENT;


    public RefreshTweetListEvent(int type) {
        this.type = type;
    }

}
