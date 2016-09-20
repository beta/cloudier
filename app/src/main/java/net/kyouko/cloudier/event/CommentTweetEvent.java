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
    public String sourceTweetNickname;
    public String sourceTweetTime;
    public String sourceTweetContent;

    public TweetCardUtil.MiniCard card;
    public TextView nickname;
    public TextView time;
    public TextView content;


    public CommentTweetEvent(SourceTweet tweet, String sourceTweetNickname, String sourceTweetTime,
                             String sourceTweetComment, TweetCardUtil.Card card) {
        this.tweet = tweet;
        this.sourceTweetNickname = sourceTweetNickname;
        this.sourceTweetTime = sourceTweetTime;
        this.sourceTweetContent = sourceTweetComment;
        this.card = card;
        this.nickname = card.nickname;
        this.time = card.time;
        this.content = card.content;
    }

}
