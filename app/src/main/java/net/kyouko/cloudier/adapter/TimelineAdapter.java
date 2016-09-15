package net.kyouko.cloudier.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.util.TweetCardUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class for {@link RecyclerView} to display a list of tweets.
 */
public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TweetViewHolder> {

    private Context context;
    private Timeline timeline;

    private int defaultSourceCardColor;
    private int defaultTextColor;
    private int imagePlaceholderColor;


    public TimelineAdapter(Context context, Timeline timeline) {
        this.context = context;
        this.timeline = timeline;

        defaultSourceCardColor = context.getResources().getColor(R.color.grey_100);
        defaultTextColor = context.getResources().getColor(R.color.black_87alpha);
        imagePlaceholderColor = context.getResources().getColor(R.color.grey_300);
    }


    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.template_tweet_card, parent, false);
        return new TweetViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final TweetViewHolder holder, int position) {
        final Tweet tweet = timeline.tweets.get(position);

        TweetCardUtil.displayTweet(tweet, holder.card, true);
    }


    @Override
    public int getItemCount() {
        return timeline.tweets.size();
    }


    public class TweetViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card) CardView card;

        public TweetViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

    }

}
