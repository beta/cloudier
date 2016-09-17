package net.kyouko.cloudier.event;

import android.widget.TextView;

import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.util.TweetCardUtil;

/**
 * Event for commenting on tweets.
 *
 * @author beta
 */
public class CommentTweetEvent {

    public SourceTweet tweet;
    public boolean isSourceTweet;
    public String sourceTweetContent;

    public String commentContent;

    public TweetCardUtil.MiniCard card;
    public TextView nickname;
    public TextView time;
    public TextView content;


    public CommentTweetEvent(SourceTweet tweet, String sourceTweetComment, TweetCardUtil.Card card) {
        this(tweet, sourceTweetComment, "", card, true);
    }


    public CommentTweetEvent(SourceTweet tweet, String sourceTweetComment, String commentContent,
                             TweetCardUtil.Card card, boolean isSourceTweet) {
        this.tweet = tweet;
        this.sourceTweetContent = sourceTweetComment;
        this.commentContent = commentContent;

        this.card = card;
        if (isSourceTweet) {
            this.nickname = card.nickname;
            this.time = card.time;
            this.content = card.content;
        } else {
            this.nickname = card.sourceNickname;
            this.time = card.sourceTime;
            this.content = card.sourceContent;
        }
    }

}
