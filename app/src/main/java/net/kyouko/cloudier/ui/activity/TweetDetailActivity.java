package net.kyouko.cloudier.ui.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.adapter.TabsFragmentPagerAdapter;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.ui.fragment.TweetListFragment;
import net.kyouko.cloudier.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TweetDetailActivity extends AppCompatActivity implements
        AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.srl) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.card) CardView card;
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

    private Tweet tweet;

    private int defaultSourceCardColor;
    private int defaultTextColor;
    private int imagePlaceholderColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        ButterKnife.bind(this);

        initView();

        getTweet();
        loadResources();
        displayTweet();
    }


    @Override
    protected void onResume() {
        super.onResume();

        appBarLayout.addOnOffsetChangedListener(this);
    }


    @Override
    protected void onPause() {
        super.onPause();

        appBarLayout.removeOnOffsetChangedListener(this);
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        swipeRefreshLayout.setEnabled(verticalOffset == 0);
    }


    private void initView() {
        initToolbar();
        initSwipeRefreshLayout();
        initTabs();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void initSwipeRefreshLayout() {
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float actionBarSize, swipeEndPosition;
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarSize = TypedValue.complexToDimensionPixelSize(typedValue.data, displayMetrics);
        } else {
            actionBarSize = toolbar.getHeight();
        }

        actionBarSize -= swipeRefreshLayout.getProgressCircleDiameter();

        swipeEndPosition = actionBarSize + 64 * displayMetrics.density;

        swipeRefreshLayout.setProgressViewOffset(false, (int) actionBarSize, (int) swipeEndPosition);
        swipeRefreshLayout.setEnabled(false);
    }


    private void initTabs() {
        TabsFragmentPagerAdapter adapter = new TabsFragmentPagerAdapter(getSupportFragmentManager());
        adapter.add(getString(R.string.title_tab_comments), new TweetListFragment());
        adapter.add(getString(R.string.title_tab_retweets), new TweetListFragment());

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
    }


    private void getTweet() {
        tweet = (Tweet) getIntent().getSerializableExtra("TWEET");
    }


    private void loadResources() {
        defaultSourceCardColor = getResources().getColor(R.color.grey_100);
        defaultTextColor = getResources().getColor(R.color.black_87alpha);
        imagePlaceholderColor = getResources().getColor(R.color.grey_300);
    }


    private void displayTweet() {
        avatar.setImageURI(ImageUtil.getInstance(this).parseImageUrl(tweet.avatarUrl));
        nickname.setText(tweet.nickname);
        username.setText(this.getString(R.string.text_pattern_username, tweet.username));

        boolean hasContent = (tweet.content.length() > 0);
        content.setVisibility(hasContent ? View.VISIBLE : View.GONE);
        content.setText(tweet.content);

        boolean hasImages = (tweet.imageUrls != null && !tweet.imageUrls.isEmpty());
        if (hasImages) {
            imageWrapper.setVisibility(View.VISIBLE);

            Picasso.with(this)
                    .load(ImageUtil.getInstance(this).parseImageUrl(tweet.imageUrls.get(0)))
                    .placeholder(new ColorDrawable(imagePlaceholderColor))
                    .into(image);
            image.setVisibility(View.VISIBLE);

            imageMask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<String> imageUrls = new ArrayList<>();
                    for (String imageUrl : tweet.imageUrls) {
                        imageUrls.add(ImageUtil.getInstance(TweetDetailActivity.this)
                                .parseImageUrl(imageUrl));
                    }

                    viewImages(imageUrls);
                }
            });

            if (tweet.imageUrls.size() > 1) {
                imageCount.setVisibility(View.VISIBLE);
                imageCount.setText(String.valueOf(tweet.imageUrls.size()));
            } else {
                imageCount.setVisibility(View.GONE);
            }
        } else {
            imageWrapper.setVisibility(View.GONE);
        }

        boolean hasSourceTweet = (tweet.sourceTweet != null);
        if (hasSourceTweet) {
            sourceCard.setCardBackgroundColor(defaultSourceCardColor);
            sourceNickname.setTextColor(defaultTextColor);
            sourceContent.setTextColor(defaultTextColor);

            sourceCard.setVisibility(View.VISIBLE);
            sourceNickname.setText(tweet.sourceTweet.nickname);
            sourceContent.setText(tweet.sourceTweet.content);

            if (tweet.sourceTweet.imageUrls != null && !tweet.sourceTweet.imageUrls.isEmpty()) {
                sourceImage.setVisibility(View.VISIBLE);

                Picasso.with(this)
                        .load(ImageUtil.getInstance(this)
                                .parseImageUrl(tweet.sourceTweet.imageUrls.get(0)))
                        .placeholder(new ColorDrawable(imagePlaceholderColor))
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
        } else {
            sourceCard.setVisibility(View.GONE);
        }

        spaceBelowContent.setVisibility((hasContent && (hasImages || hasSourceTweet)) ?
                View.VISIBLE : View.GONE);
        spaceBelowImage.setVisibility((hasImages && hasSourceTweet) ? View.VISIBLE : View.GONE);

        commentCount.setText(String.valueOf(tweet.commentCount));
        commentCount.setVisibility((tweet.commentCount > 0) ? View.VISIBLE : View.GONE);
        retweetCount.setText(String.valueOf(tweet.retweetCount));
        retweetCount.setVisibility((tweet.retweetCount > 0) ? View.VISIBLE : View.GONE);
    }


    public void viewImages(List<String> imageUrls) {
        new ImageViewer.Builder(this, (ArrayList<String>) imageUrls).show();
    }

}
