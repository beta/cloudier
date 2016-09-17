package net.kyouko.cloudier.event;

import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.util.TweetCardUtil;

/**
 * Event for retweeting tweets.
 *
 * @author beta
 */
public class RetweetTweetEvent extends CommentTweetEvent {

    public RetweetTweetEvent(SourceTweet tweet, String sourceTweetContent, TweetCardUtil.Card card) {
        super(tweet, sourceTweetContent, card);
    }


    public RetweetTweetEvent(SourceTweet tweet, String sourceTweetContent, String retweetContent,
                             TweetCardUtil.Card card, boolean isSourceTweet) {
        super(tweet, sourceTweetContent, retweetContent, card, isSourceTweet);
    }

}
