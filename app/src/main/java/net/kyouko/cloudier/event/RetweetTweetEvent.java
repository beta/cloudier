package net.kyouko.cloudier.event;

import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.util.TweetCardUtil;

/**
 * Event for retweeting tweets.
 *
 * @author beta
 */
public class RetweetTweetEvent extends CommentTweetEvent {

    public boolean isSourceTweet;
    public String retweetContent;


    public RetweetTweetEvent(SourceTweet tweet, String sourceTweetContent, TweetCardUtil.Card card) {
        super(tweet, sourceTweetContent, card);
    }


    public RetweetTweetEvent(SourceTweet tweet, String sourceTweetContent, TweetCardUtil.Card card,
                             String retweetContent, boolean isSourceTweet) {
        super(tweet, sourceTweetContent, card);

        this.retweetContent = retweetContent;
        this.isSourceTweet = isSourceTweet;
        if (!isSourceTweet) {
            this.nickname = card.sourceNickname;
            this.time = card.sourceTime;
            this.content = card.sourceContent;
        }
    }

}
