package net.kyouko.cloudier.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.model.Tweet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Util class for displaying tweets onto cards.
 *
 * @author beta
 */
public class TweetCardUtil {

    public static class Card {

        public CardView cardView;
        @BindView(R.id.wrapper) View wrapper;
        @BindView(R.id.avatar) SimpleDraweeView avatar;
        @BindView(R.id.nickname) TextView nickname;
        @BindView(R.id.username) TextView username;
        @BindView(R.id.content) TextView content;
        @BindView(R.id.space_below_content) View spaceBelowContent;
        @BindView(R.id.image_wrapper) View imageWrapper;
        @BindView(R.id.image) ImageView image;
        @BindView(R.id.image_mask) View imageMask;
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

        private int defaultSourceCardColor;
        private int defaultTextColor;
        private int imagePlaceholderColor;

        private Context context;


        public Card(CardView cardView) {
            this.cardView = cardView;
            context = cardView.getContext();

            ButterKnife.bind(this, cardView);

            loadResources();
        }


        private void loadResources() {
            Context context = cardView.getContext();
            defaultSourceCardColor = context.getResources().getColor(R.color.grey_100);
            defaultTextColor = context.getResources().getColor(R.color.black_87alpha);
            imagePlaceholderColor = context.getResources().getColor(R.color.grey_300);
        }


        void displayTweet(final SourceTweet tweet, final HashMap<String, String> users,
                          boolean clickable, boolean minimized) {
            final Context context = cardView.getContext();

            if (clickable) {
                wrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CloudierApplication.getBus().post(new ViewTweetEvent(tweet, users, Card.this));
                    }
                });
            }

            avatar.setImageURI(ImageUtil.getInstance(context).parseImageUrl(tweet.avatarUrl));
            nickname.setText(tweet.nickname);
            username.setText(context.getString(R.string.text_pattern_username, tweet.username));

            boolean hasContent = (tweet.content.length() > 0);
            content.setVisibility(hasContent ? View.VISIBLE : View.GONE);

            SpannableStringBuilder tweetContent = TextUtil.addLinkToUrlsInText(context,
                    tweet.originalContent, false);
            tweetContent = TextUtil.addLinkToTopicsInText(context, tweetContent, false);
            tweetContent = NicknameUtil.replaceUsernameWithNicknameInContent(tweetContent, users);
            content.setText(tweetContent);

            boolean hasImages = (tweet.imageUrls != null && !tweet.imageUrls.isEmpty());
            if (hasImages) {
                displayImages(tweet.imageUrls);
            } else {
                imageWrapper.setVisibility(View.GONE);
            }

            boolean hasSourceTweet = (tweet instanceof Tweet && ((Tweet) tweet).sourceTweet != null);
            boolean sourceTweetShown = !minimized && hasSourceTweet;
            if (sourceTweetShown) {
                displaySourceTweet(((Tweet) tweet).sourceTweet, users);
            } else {
                sourceCard.setVisibility(View.GONE);
            }

            spaceBelowContent.setVisibility((hasContent && (hasImages || sourceTweetShown)) ?
                    View.VISIBLE : View.GONE);
            spaceBelowImage.setVisibility((hasImages && sourceTweetShown) ? View.VISIBLE : View.GONE);

            commentCount.setText(String.valueOf(tweet.commentCount));
            commentCount.setVisibility((tweet.commentCount > 0) ? View.VISIBLE : View.GONE);
            retweetCount.setText(String.valueOf(tweet.retweetCount));
            retweetCount.setVisibility((tweet.retweetCount > 0) ? View.VISIBLE : View.GONE);
        }


        private void displayImages(final List<String> imageUrls) {
            imageWrapper.setVisibility(View.VISIBLE);

            Picasso.with(context)
                    .load(ImageUtil.getInstance(context).parseImageUrl(imageUrls.get(0)))
                    .placeholder(new ColorDrawable(imagePlaceholderColor))
                    .fit()
                    .centerCrop()
                    .into(image);
            image.setVisibility(View.VISIBLE);

            imageMask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<String> newImageUrls = new ArrayList<>();
                    for (String imageUrl : imageUrls) {
                        newImageUrls.add(ImageUtil.getInstance(context)
                                .parseImageUrl(imageUrl));
                    }

                    CloudierApplication.getBus().post(new ViewImageEvent(newImageUrls));
                }
            });

            if (imageUrls.size() > 1) {
                imageCount.setVisibility(View.VISIBLE);
                imageCount.setText(String.valueOf(imageUrls.size()));
            } else {
                imageCount.setVisibility(View.GONE);
            }
        }


        private void displaySourceTweet(final SourceTweet sourceTweet,
                                        final HashMap<String, String> users) {
            sourceCard.setCardBackgroundColor(defaultSourceCardColor);
            sourceNickname.setTextColor(defaultTextColor);
            sourceContent.setTextColor(defaultTextColor);

            sourceCard.setVisibility(View.VISIBLE);
            sourceWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CloudierApplication.getBus().post(new ViewTweetEvent(sourceTweet, users, null));
                }
            });

            sourceNickname.setText(sourceTweet.nickname);

            SpannableStringBuilder tweetContent = TextUtil.addLinkToUrlsInText(context,
                    sourceTweet.originalContent, false);
            tweetContent = TextUtil.addLinkToTopicsInText(context, tweetContent, false);
            tweetContent = NicknameUtil.replaceUsernameWithNicknameInContent(tweetContent, users);
            sourceContent.setText(tweetContent);

            if (sourceTweet.imageUrls != null && !sourceTweet.imageUrls.isEmpty()) {
                sourceImage.setVisibility(View.VISIBLE);

                Picasso.with(context)
                        .load(ImageUtil.getInstance(context)
                                .parseImageUrl(sourceTweet.imageUrls.get(0)))
                        .placeholder(new ColorDrawable(imagePlaceholderColor))
                        .fit()
                        .centerCrop()
                        .into(sourceImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                BitmapDrawable drawable = (BitmapDrawable) sourceImage.getDrawable();
                                Bitmap bitmap = drawable.getBitmap();

                                Palette palette = Palette.from(bitmap).generate();

                                sourceCard.setCardBackgroundColor(
                                        palette.getDarkMutedColor(defaultSourceCardColor));
                                sourceNickname.setTextColor(
                                        palette.getLightMutedColor(defaultTextColor));
                                sourceContent.setTextColor(
                                        palette.getLightMutedColor(defaultTextColor));
                            }

                            @Override
                            public void onError() {
                                // Ignore
                            }
                        });
            } else {
                sourceImage.setVisibility(View.GONE);
            }
        }

    }


    public static Card displayTweet(SourceTweet tweet, HashMap<String, String> users,
                                    CardView cardView) {
        return displayTweet(tweet, users, cardView, false);
    }


    public static Card displayTweet(SourceTweet tweet, HashMap<String, String> users,
                                    CardView cardView, boolean clickable) {
        return displayTweet(tweet, users, cardView, clickable, false);
    }


    public static Card displayTweet(SourceTweet tweet, HashMap<String, String> users,
                                    CardView cardView, boolean clickable, boolean minimized) {
        Card card = new Card(cardView);
        card.displayTweet(tweet, users, clickable, minimized);
        return card;
    }

}
