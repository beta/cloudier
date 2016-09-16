package net.kyouko.cloudier.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.LoadMoreEvent;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.util.TweetCardUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class for {@link RecyclerView} to display a list of tweets.
 */
public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.BaseViewHolder> {

    private final static int ITEM_TYPE_TWEET = 0;
    private final static int ITEM_TYPE_LOAD_MORE = 99;


    private Context context;
    private Timeline timeline;

    private LoadMoreViewHolder loadMoreViewHolder;

    private int defaultSourceCardColor;
    private int defaultTextColor;
    private int imagePlaceholderColor;

    private int shortAnimationDuration;


    public TimelineAdapter(Context context, Timeline timeline) {
        this.context = context;
        this.timeline = timeline;

        defaultSourceCardColor = context.getResources().getColor(R.color.grey_100);
        defaultTextColor = context.getResources().getColor(R.color.black_87alpha);
        imagePlaceholderColor = context.getResources().getColor(R.color.grey_300);

        shortAnimationDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_TWEET) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.template_tweet_card, parent, false);
            return new TweetViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.template_load_more, parent, false);
            return new LoadMoreViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_TYPE_TWEET:
                Tweet tweet = timeline.tweets.get(position);
                bindTweetViewHolder((TweetViewHolder) holder, tweet);
                break;
            case ITEM_TYPE_LOAD_MORE:
                bindLoadMoreViewHolder((LoadMoreViewHolder) holder);
                break;
        }
    }


    private void bindTweetViewHolder(TweetViewHolder holder, Tweet tweet) {
        TweetCardUtil.displayTweet(tweet, timeline.users, holder.card, true);
    }


    private void bindLoadMoreViewHolder(final LoadMoreViewHolder holder) {
        loadMoreViewHolder = holder;

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMore(holder);
            }
        });
    }


    private void loadMore(final LoadMoreViewHolder holder) {
        holder.button.setClickable(false);

        holder.progress.setAlpha(0f);
        holder.progress.setVisibility(View.VISIBLE);

        holder.progress.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);

        holder.button.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        holder.button.setVisibility(View.GONE);
                        holder.button.setClickable(true);

                        CloudierApplication.getBus().post(new LoadMoreEvent());
                    }
                });
    }


    public void completeLoadingMore() {
        if (loadMoreViewHolder != null) {
            loadMoreViewHolder.progress.setVisibility(View.GONE);

            loadMoreViewHolder.button.setAlpha(1f);
            loadMoreViewHolder.button.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemViewType(int position) {
        boolean shouldShowLoadMoreButton = !timeline.tweets.isEmpty();
        if (position == timeline.tweets.size()) {
            return (shouldShowLoadMoreButton ? ITEM_TYPE_LOAD_MORE : ITEM_TYPE_TWEET);
        } else {
            return ITEM_TYPE_TWEET;
        }
    }


    @Override
    public int getItemCount() {
        return (timeline.tweets.size() + (timeline.tweets.isEmpty() ? 0 : 1));
    }


    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
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
