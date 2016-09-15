package net.kyouko.cloudier.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.util.ImageUtil;

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
    private int defaultImageIndicatorColor;
    private int defaultImageIndicatorTextColor;


    public TimelineAdapter(Context context, Timeline timeline) {
        this.context = context;
        this.timeline = timeline;

        defaultSourceCardColor = context.getResources().getColor(R.color.grey_100);
        defaultTextColor = context.getResources().getColor(R.color.black_87alpha);
        imagePlaceholderColor = context.getResources().getColor(R.color.grey_300);
        defaultImageIndicatorColor = context.getResources().getColor(R.color.black);
        defaultImageIndicatorTextColor = context.getResources().getColor(R.color.white);
    }


    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.template_tweet_card, parent, false);
        return new TweetViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final TweetViewHolder holder, int position) {
        Tweet tweet = timeline.tweets.get(position);

        holder.avatar.setImageURI(ImageUtil.getInstance(context).parseImageUrl(tweet.avatarUrl));
        holder.nickname.setText(tweet.nickname);
        holder.username.setText(context.getString(R.string.text_pattern_username, tweet.username));

        boolean hasContent = (tweet.content.length() > 0);
        holder.content.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        holder.content.setText(tweet.content);

        boolean hasImages = (tweet.imageUrls != null && !tweet.imageUrls.isEmpty());
        if (hasImages) {
            holder.imageWrapper.setVisibility(View.VISIBLE);

            Picasso.with(context)
                    .load(ImageUtil.getInstance(context).parseImageUrl(tweet.imageUrls.get(0)))
                    .placeholder(new ColorDrawable(imagePlaceholderColor))
                    .into(holder.image);
            holder.image.setVisibility(View.VISIBLE);

            if (tweet.imageUrls.size() > 1) {
                holder.imageCount.setVisibility(View.VISIBLE);
                holder.imageCount.setText(String.valueOf(tweet.imageUrls.size()));
            } else {
                holder.imageCount.setVisibility(View.GONE);
            }
        } else {
            holder.imageWrapper.setVisibility(View.GONE);
        }

        boolean hasSourceTweet = (tweet.sourceTweet != null);
        if (hasSourceTweet) {
            holder.sourceCard.setCardBackgroundColor(defaultSourceCardColor);
            holder.sourceNickname.setTextColor(defaultTextColor);
            holder.sourceContent.setTextColor(defaultTextColor);

            holder.sourceCard.setVisibility(View.VISIBLE);
            holder.sourceNickname.setText(tweet.sourceTweet.nickname);
            holder.sourceContent.setText(tweet.sourceTweet.content);

            if (tweet.sourceTweet.imageUrls != null && !tweet.sourceTweet.imageUrls.isEmpty()) {
                holder.sourceImage.setVisibility(View.VISIBLE);

                Picasso.with(context)
                        .load(ImageUtil.getInstance(context)
                                .parseImageUrl(tweet.sourceTweet.imageUrls.get(0)))
                        .placeholder(new ColorDrawable(imagePlaceholderColor))
                        .into(holder.sourceImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                BitmapDrawable drawable = (BitmapDrawable) holder.sourceImage.getDrawable();
                                Bitmap bitmap = drawable.getBitmap();

                                Palette palette = Palette.from(bitmap).generate();

                                holder.sourceCard.setCardBackgroundColor(
                                        palette.getDarkMutedColor(defaultSourceCardColor));
                                holder.sourceNickname.setTextColor(
                                        palette.getLightMutedColor(defaultTextColor));
                                holder.sourceContent.setTextColor(
                                        palette.getLightMutedColor(defaultTextColor));
                            }

                            @Override
                            public void onError() {
                                // Ignore
                            }
                        });
            } else {
                holder.sourceImage.setVisibility(View.GONE);
            }
        } else {
            holder.sourceCard.setVisibility(View.GONE);
        }

        holder.spaceBelowContent.setVisibility((hasContent && (hasImages || hasSourceTweet)) ?
                View.VISIBLE : View.GONE);
        holder.spaceBelowImage.setVisibility((hasImages && hasSourceTweet) ? View.VISIBLE : View.GONE);

        holder.commentCount.setText(String.valueOf(tweet.commentCount));
        holder.commentCount.setVisibility((tweet.commentCount > 0) ? View.VISIBLE : View.GONE);
        holder.retweetCount.setText(String.valueOf(tweet.retweetCount));
        holder.retweetCount.setVisibility((tweet.retweetCount > 0) ? View.VISIBLE : View.GONE);
    }


    @Override
    public int getItemCount() {
        return timeline.tweets.size();
    }


    public class TweetViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card) CardView card;
        @BindView(R.id.avatar) SimpleDraweeView avatar;
        @BindView(R.id.nickname) TextView nickname;
        @BindView(R.id.username) TextView username;
        @BindView(R.id.content) TextView content;
        @BindView(R.id.space_below_content) View spaceBelowContent;
        @BindView(R.id.image_wrapper) View imageWrapper;
        @BindView(R.id.image) ImageView image;
        @BindView(R.id.image_count) TextView imageCount;
        @BindView(R.id.space_below_image) View spaceBelowImage;
        @BindView(R.id.source_card) CardView sourceCard;
        @BindView(R.id.source_wrapper) View sourceWrapper;
        @BindView(R.id.source_image) ImageView sourceImage;
        @BindView(R.id.source_nickname) TextView sourceNickname;
        @BindView(R.id.source_content) TextView sourceContent;
        @BindView(R.id.button_comment) View commentButton;
        @BindView(R.id.comment_count) TextView commentCount;
        @BindView(R.id.button_retweet) View retweetButton;
        @BindView(R.id.retweet_count) TextView retweetCount;
        @BindView(R.id.button_share) View shareButton;
        @BindView(R.id.button_menu) View menuButton;

        public TweetViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

    }

}
