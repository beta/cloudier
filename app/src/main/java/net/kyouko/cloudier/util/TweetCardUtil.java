package net.kyouko.cloudier.util;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.picasso.Picasso;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.CommentTweetEvent;
import net.kyouko.cloudier.event.DeleteTweetEvent;
import net.kyouko.cloudier.event.RetweetTweetEvent;
import net.kyouko.cloudier.event.ShareTweetEvent;
import net.kyouko.cloudier.event.ShowTweetMenuEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.event.ViewUserEvent;
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

    public static class MiniCard {

        public CardView cardView;
        @BindView(R.id.wrapper) View wrapper;
        @BindView(R.id.avatar) SimpleDraweeView avatar;
        @BindView(R.id.nickname) public TextView nickname;
        @BindView(R.id.time) public TextView time;
        @BindView(R.id.content) public TextView content;
        @BindView(R.id.image_wrapper) View imageWrapper;
        @BindView(R.id.image) ImageView image;
        @BindView(R.id.image_mask) View imageMask;
        @BindView(R.id.image_count) TextView imageCount;

        protected Context context;

        protected int imagePlaceholderColor;


        public MiniCard(CardView cardView) {
            this.cardView = cardView;
            context = cardView.getContext();

            ButterKnife.bind(this, cardView);
        }


        protected void loadResources() {
            imagePlaceholderColor = context.getResources().getColor(R.color.grey_300);
        }


        public void displayTweet(final SourceTweet tweet, final HashMap<String, String> users,
                                 boolean clickable) {
            if (clickable) {
                wrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CloudierApplication.getBus().post(new ViewTweetEvent(tweet, users));
                    }
                });
            }

            avatar.setImageURI(ImageUtil.getInstance(context).parseImageUrl(tweet.avatarUrl));
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloudierApplication.getBus().post(new ViewUserEvent(tweet.username));
                }
            });

            nickname.setText(tweet.nickname);
            time.setText(DateTimeUtil.getDateTimeDescription(context, tweet.timestamp));

            boolean hasContent = (tweet.originalContent.length() > 0);
            content.setVisibility(hasContent ? View.VISIBLE : View.GONE);

            SpannableStringBuilder tweetContent = TextUtil.addLinkToUrlsInText(context,
                    Html.fromHtml(tweet.originalContent).toString(), false);
            tweetContent = TextUtil.addLinkToTopicsInText(context, tweetContent, false);
            tweetContent = NicknameUtil.replaceUsernameWithNicknameInContent(tweetContent, users);
            content.setText(tweetContent);

            boolean hasImages = (tweet.imageUrls != null && !tweet.imageUrls.isEmpty());
            if (hasImages) {
                displayImages(tweet.imageUrls);
            } else {
                imageWrapper.setVisibility(View.GONE);
            }
        }


        protected void displayImages(final List<String> imageUrls) {
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

    }


    public static class Card extends MiniCard {

        @BindView(R.id.username) TextView username;
        @BindView(R.id.space_below_content) View spaceBelowContent;
        @BindView(R.id.deleted_source) View deletedSourceCard;
        @BindView(R.id.source_card) CardView sourceCard;
        @BindView(R.id.source_wrapper) View sourceWrapper;
        @BindView(R.id.source_nickname) public TextView sourceNickname;
        @BindView(R.id.source_time) public TextView sourceTime;
        @BindView(R.id.source_content) public TextView sourceContent;
        @BindView(R.id.source_image_wrapper) View sourceImageWrapper;
        @BindView(R.id.source_image) ImageView sourceImage;
        @BindView(R.id.source_image_mask) View sourceImageMask;
        @BindView(R.id.source_image_count) TextView sourceImageCount;
        @BindView(R.id.button_comment) View commentButton;
        @BindView(R.id.comment_count) TextView commentCount;
        @BindView(R.id.button_retweet) View retweetButton;
        @BindView(R.id.retweet_count) TextView retweetCount;
        @BindView(R.id.button_share) View shareButton;
        @BindView(R.id.button_menu) View menuButton;


        public Card(CardView cardView) {
            super(cardView);

            ButterKnife.bind(this, cardView);

            loadResources();
        }


        @Override
        protected void loadResources() {
            super.loadResources();
        }


        @Override
        public void displayTweet(final SourceTweet tweet, final HashMap<String, String> users,
                                 boolean clickable) {
            super.displayTweet(tweet, users, clickable);

            if (clickable) {
                wrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CloudierApplication.getBus().post(new ViewTweetEvent(tweet, users, Card.this));
                    }
                });
            }

            username.setText(context.getString(R.string.text_pattern_username, tweet.username));

            boolean hasContent = (tweet.content.length() > 0);
            boolean hasImages = (tweet.imageUrls != null && !tweet.imageUrls.isEmpty());

            final boolean hasSourceTweet = (tweet instanceof Tweet && ((Tweet) tweet).sourceTweet != null);
            if (hasSourceTweet) {
                displaySourceTweet(((Tweet) tweet).sourceTweet, users);
            } else {
                deletedSourceCard.setVisibility(View.GONE);
                sourceCard.setVisibility(View.GONE);
            }

            spaceBelowContent.setVisibility((hasContent && (hasImages || hasSourceTweet)) ?
                    View.VISIBLE : View.GONE);

            commentCount.setText(String.valueOf(tweet.commentCount));
            commentCount.setVisibility((tweet.commentCount > 0) ? View.VISIBLE : View.GONE);
            retweetCount.setText(String.valueOf(tweet.retweetCount));
            retweetCount.setVisibility((tweet.retweetCount > 0) ? View.VISIBLE : View.GONE);

            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CloudierApplication.getBus().post(new CommentTweetEvent(tweet, tweet.nickname,
                            time.getText().toString(), content.getText().toString(), Card.this));
                }
            });
            retweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (hasSourceTweet) {
                        CloudierApplication.getBus().post(new RetweetTweetEvent(tweet,
                                sourceNickname.getText().toString(),
                                sourceTime.getText().toString(),
                                sourceContent.getText().toString(), Card.this,
                                context.getString(R.string.text_pattern_comment, tweet.username,
                                        tweet.originalContent), false));
                    } else {
                        CloudierApplication.getBus().post(new RetweetTweetEvent(tweet,
                                tweet.nickname, time.getText().toString(),
                                content.getText().toString(), Card.this));
                    }
                }
            });

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CloudierApplication.getBus().post(new ShareTweetEvent(tweet.id));
                }
            });

            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CloudierApplication.getBus().post(new ShowTweetMenuEvent(tweet));
                }
            });
        }


        private void displaySourceTweet(final SourceTweet sourceTweet,
                                        final HashMap<String, String> users) {
            if (sourceTweet.status != Tweet.STATUS_NORMAL) {
                deletedSourceCard.setVisibility(View.VISIBLE);
                sourceCard.setVisibility(View.GONE);
                return;
            }

            deletedSourceCard.setVisibility(View.GONE);
            sourceCard.setVisibility(View.VISIBLE);
            sourceWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CloudierApplication.getBus().post(new ViewTweetEvent(sourceTweet, users, null));
                }
            });

            sourceNickname.setText(sourceTweet.nickname);

            sourceTime.setText(DateTimeUtil.getDateTimeDescription(context, sourceTweet.timestamp));

            SpannableStringBuilder tweetContent = TextUtil.addLinkToUrlsInText(context,
                    Html.fromHtml(sourceTweet.originalContent).toString(), false);
            tweetContent = TextUtil.addLinkToTopicsInText(context, tweetContent, false);
            tweetContent = NicknameUtil.replaceUsernameWithNicknameInContent(tweetContent, users);
            sourceContent.setText(tweetContent);
            sourceContent.setVisibility((tweetContent.length() > 0) ? View.VISIBLE : View.GONE);

            if (sourceTweet.imageUrls != null && !sourceTweet.imageUrls.isEmpty()) {
                displaySourceImages(sourceTweet.imageUrls);
            } else {
                sourceImageWrapper.setVisibility(View.GONE);
            }
        }


        private void displaySourceImages(final List<String> imageUrls) {
            sourceImageWrapper.setVisibility(View.VISIBLE);

            sourceImage.setVisibility(View.VISIBLE);

            Picasso.with(context)
                    .load(ImageUtil.getInstance(context)
                            .parseImageUrl(imageUrls.get(0)))
                    .placeholder(new ColorDrawable(imagePlaceholderColor))
                    .fit()
                    .centerCrop()
                    .into(sourceImage);

            sourceImageMask.setOnClickListener(new View.OnClickListener() {
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
                sourceImageCount.setVisibility(View.VISIBLE);
                sourceImageCount.setText(String.valueOf(imageUrls.size()));
            } else {
                sourceImageCount.setVisibility(View.GONE);
            }
        }

    }


    public static Card displayTweet(SourceTweet tweet, HashMap<String, String> users,
                                    CardView cardView) {
        return displayTweet(tweet, users, cardView, false);
    }


    public static Card displayTweet(SourceTweet tweet, HashMap<String, String> users,
                                    CardView cardView, boolean clickable) {
        Card card = new Card(cardView);
        card.displayTweet(tweet, users, clickable);
        return card;
    }


    public static MiniCard displayTweetMinimized(SourceTweet tweet, HashMap<String, String> users,
                                                 CardView cardView, boolean clickable) {
        MiniCard card = new MiniCard(cardView);
        card.displayTweet(tweet, users, clickable);
        return card;
    }


    public static void showTweetMenu(final Context context, final SourceTweet tweet) {
        List<CharSequence> selections = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();

        selections.add(context.getString(R.string.title_action_copy_tweet));
        actions.add(new Runnable() {
            @Override
            public void run() {
                ClipboardManager clipboardManager =
                        (ClipboardManager) context.getSystemService(Service.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(
                        context.getString(R.string.text_description_tweet), tweet.originalContent));
                MessageUtil.showToast(context, context.getString(R.string.text_info_tweet_copied));
            }
        });

        selections.add(context.getString(R.string.title_action_copy_tweet_link));
        actions.add(new Runnable() {
            @Override
            public void run() {
                ClipboardManager clipboardManager =
                        (ClipboardManager) context.getSystemService(Service.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(
                        context.getString(R.string.text_description_tweet_link),
                        context.getString(R.string.text_pattern_tweet_link, tweet.id)));
                MessageUtil.showToast(context, context.getString(R.string.text_info_tweet_link_copied));
            }
        });

        if (tweet.sentBySelf) {
            selections.add(context.getString(R.string.title_action_delete_tweet));
            actions.add(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(context)
                            .setMessage(R.string.text_info_delete_tweet)
                            .setPositiveButton(R.string.title_action_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            CloudierApplication.getBus().post(
                                                    new DeleteTweetEvent(tweet.id));
                                        }
                                    })
                            .setNegativeButton(R.string.title_action_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Ignore
                                        }
                                    })
                            .show();
                }
            });
        }

        new AlertDialog.Builder(context)
                .setItems(selections.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actions.get(which).run();
                    }
                })
                .show();
    }

}
