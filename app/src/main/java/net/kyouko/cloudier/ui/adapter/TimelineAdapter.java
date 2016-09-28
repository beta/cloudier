package net.kyouko.cloudier.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.LoadMoreTweetsEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsWithTypeEvent;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.TweetCardUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class for {@link RecyclerView} to display a list of tweets.
 */
public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.BaseViewHolder> {

    private final static int ITEM_TYPE_TWEET = 0;
    private final static int ITEM_TYPE_COMPOSER = 1;
    private final static int ITEM_TYPE_LOAD_MORE = 99;


    private Context context;
    private Timeline timeline;
    private boolean clickable = false;
    private boolean minimized = false;

    private Account account;
    private boolean showComposer = false;
    private CardView composerCard;

    private boolean hasTweetType = false;
    private int tweetType = Tweet.TYPE_ORIGINAL;

    private LoadMoreViewHolder loadMoreViewHolder;
    private boolean isLoadingMore = false;

    private int defaultSourceCardColor;
    private int defaultTextColor;
    private int imagePlaceholderColor;

    private int shortAnimationDuration;


    public TimelineAdapter(Context context, Timeline timeline) {
        this(context, timeline, true, false);
    }


    public TimelineAdapter(Context context, Timeline timeline, boolean clickable, boolean minimized) {
        this.context = context;
        this.timeline = timeline;
        this.clickable = clickable;
        this.minimized = minimized;

        defaultSourceCardColor = context.getResources().getColor(R.color.grey_100);
        defaultTextColor = context.getResources().getColor(R.color.black_87alpha);
        imagePlaceholderColor = context.getResources().getColor(R.color.grey_300);

        shortAnimationDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_COMPOSER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.template_composer_card, parent, false);
            return new ComposerViewHolder(view);
        } else if (viewType == ITEM_TYPE_TWEET) {
            View view;
            if (minimized) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.template_tweet_card_mini, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.template_tweet_card, parent, false);
            }
            return new TweetViewHolder(view);
        } else if (viewType == ITEM_TYPE_LOAD_MORE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.template_load_more, parent, false);
            return new LoadMoreViewHolder(view);
        }

        return null;
    }


    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_TYPE_COMPOSER:
                bindComposerViewHolder((ComposerViewHolder) holder);
                break;
            case ITEM_TYPE_LOAD_MORE:
                bindLoadMoreViewHolder((LoadMoreViewHolder) holder);
                break;
            case ITEM_TYPE_TWEET:
            default:
                position -= (showComposer ? 1 : 0);
                Tweet tweet = timeline.tweets.get(position);
                bindTweetViewHolder((TweetViewHolder) holder, tweet);
                break;
        }
    }


    private void bindComposerViewHolder(ComposerViewHolder holder) {
        composerCard = holder.card;

        holder.avatar.setImageURI(Uri.parse(ImageUtil.getInstance(context).
                parseImageUrl(account.avatarUrl)));
        holder.nickname.setText(account.nickname);
        holder.username.setText(context.getString(R.string.text_pattern_username, account.username));
        holder.content.setText(null);
    }


    private void bindTweetViewHolder(TweetViewHolder holder, Tweet tweet) {
        if (minimized) {
            TweetCardUtil.displayTweetMinimized(tweet, timeline.users, holder.card, clickable);
        } else {
            TweetCardUtil.displayTweet(tweet, timeline.users, holder.card, clickable);
        }
    }


    private void bindLoadMoreViewHolder(final LoadMoreViewHolder holder) {
        loadMoreViewHolder = holder;

        this.loadMoreViewHolder = holder;
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMore();
            }
        });
    }


    public void loadMore() {
        if (isLoadingMore || loadMoreViewHolder == null) {
            return;
        }

        isLoadingMore = true;
        loadMoreViewHolder.button.setClickable(false);

        loadMoreViewHolder.progress.setAlpha(0f);
        loadMoreViewHolder.progress.setVisibility(View.VISIBLE);

        loadMoreViewHolder.progress.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);

        loadMoreViewHolder.button.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadMoreViewHolder.button.setVisibility(View.GONE);
                        loadMoreViewHolder.button.setClickable(true);

                        if (!hasTweetType) {
                            CloudierApplication.getBus().post(new LoadMoreTweetsEvent());
                        } else {
                            CloudierApplication.getBus().post(new LoadMoreTweetsWithTypeEvent(tweetType));
                        }
                    }
                });
    }


    public void completeLoadingMore() {
        isLoadingMore = false;
        if (loadMoreViewHolder != null) {
            loadMoreViewHolder.progress.setVisibility(View.GONE);

            loadMoreViewHolder.button.setAlpha(1f);
            loadMoreViewHolder.button.setVisibility(View.VISIBLE);
        }
    }


    public void showComposer(Account account) {
        this.account = account;
        showComposer = true;
        notifyItemInserted(0);
    }


    public void hideComposer() {
        showComposer = false;
    }


    public CardView getComposerCard() {
        return composerCard;
    }


    @Override
    public int getItemViewType(int position) {
        boolean showLoadMore = !timeline.tweets.isEmpty();

        if (showComposer && position == 0) {
            return ITEM_TYPE_COMPOSER;
        } else if (showLoadMore && position == (getItemCount() - 1)) {
            return ITEM_TYPE_LOAD_MORE;
        } else {
            return ITEM_TYPE_TWEET;
        }
    }


    @Override
    public int getItemCount() {
        return (timeline.tweets.size() + (showComposer ? 1 : 0) +
                (!timeline.tweets.isEmpty() ? 1 : 0));
    }


    public void setTweetType(int tweetType) {
        this.tweetType = tweetType;
        this.hasTweetType = true;
    }


    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

    }


    public class ComposerViewHolder extends BaseViewHolder {

        @BindView(R.id.card) CardView card;
        @BindView(R.id.avatar) ImageView avatar;
        @BindView(R.id.nickname) TextView nickname;
        @BindView(R.id.username) TextView username;
        @BindView(R.id.content) EditText content;

        public ComposerViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

    }


    public class TweetViewHolder extends BaseViewHolder {

        @BindView(R.id.card) CardView card;

        public TweetViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

    }


    public class LoadMoreViewHolder extends BaseViewHolder {

        @BindView(R.id.button) View button;
        @BindView(R.id.progress) ProgressBar progress;

        public LoadMoreViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

    }

}
