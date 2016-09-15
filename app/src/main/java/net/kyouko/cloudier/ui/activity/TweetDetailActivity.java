package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.squareup.otto.Subscribe;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.adapter.TabsFragmentPagerAdapter;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.ui.fragment.TweetListFragment;
import net.kyouko.cloudier.util.TweetCardUtil;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TweetDetailActivity extends AppCompatActivity implements
        AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.srl) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.card) CardView cardView;

    private SourceTweet tweet;
    private HashMap<String, String> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        ButterKnife.bind(this);

        initView();

        getTweet();
    }


    @Override
    protected void onResume() {
        super.onResume();

        CloudierApplication.getBus().register(this);

        appBarLayout.addOnOffsetChangedListener(this);
    }


    @Override
    protected void onPause() {
        super.onPause();

        CloudierApplication.getBus().unregister(this);

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
        tweet = (SourceTweet) getIntent().getSerializableExtra("TWEET");
        users = (HashMap<String, String>) getIntent().getSerializableExtra("USERS");
        TweetCardUtil.displayTweet(tweet, users, cardView);
    }


    @Subscribe
    public void viewTweet(ViewTweetEvent event) {
        Intent intent = new Intent(this, TweetDetailActivity.class);

        if (event.type == ViewTweetEvent.TYPE_TWEET) {
            intent.putExtra("TWEET", event.tweet);
            intent.putExtra("USERS", event.users);
            if (event.card != null) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(this, event.card.cardView, "card");
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        } else if (event.type == ViewTweetEvent.TYPE_ID) {
            intent.putExtra("TWEET_ID", event.tweetId);
            startActivity(intent);
        }
    }


    @Subscribe
    public void viewImages(ViewImageEvent event) {
        new ImageViewer.Builder(this, (ArrayList<String>) event.imageUrls)
                .setStartPosition(event.startPosition)
                .show();
    }

}
