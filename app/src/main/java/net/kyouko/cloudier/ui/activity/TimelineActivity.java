package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.CommentTweetEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsEvent;
import net.kyouko.cloudier.event.RetweetTweetEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.ui.adapter.TimelineAdapter;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Base class for activities containing a {@link RecyclerView} for displaying timeline.
 *
 * @author beta
 */
public abstract class TimelineActivity extends AppCompatActivity {

    protected final static int REQUEST_COMPOSER_COMMENT = 0;
    protected final static int REQUEST_COMPOSER_RETWEET = 1;


    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.title) TextView textTitle;
    @BindView(R.id.srl) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler) RecyclerView recyclerView;

    protected Timeline timeline = new Timeline();
    protected TimelineAdapter adapter;


    @LayoutRes
    protected abstract int getContentViewLayoutId();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutId());

        ButterKnife.bind(this);

        initView();

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                prepare();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        CloudierApplication.getBus().register(this);
    }


    @Override
    protected void onPause() {
        super.onPause();

        CloudierApplication.getBus().unregister(this);
    }


    protected void initView() {
        initToolbar();
        initSwipeRefreshLayout();
        initRecyclerView();
    }


    protected void initToolbar() {
        setSupportActionBar(toolbar);
        setTitle(null);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(0);
            }
        });
    }


    protected void initSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_500, R.color.light_blue_700);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadLatestTimeline();
            }
        });
    }


    protected void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TimelineAdapter(this, timeline);
        recyclerView.setAdapter(adapter);
    }


    protected void prepare() {
        loadLatestTimeline();
    }


    protected abstract void loadLatestTimeline();


    protected void mergeLatestTimeline(Timeline latestTimeline) {
        if (timeline.tweets.isEmpty()) {
            timeline.tweets.clear();
            timeline.tweets.addAll(latestTimeline.tweets);
        } else {
            if (timeline.tweets.isEmpty()) {
                timeline.tweets.addAll(latestTimeline.tweets);
            } else if (timeline.containsTweet(latestTimeline.tweets.get(0))) {
                int startPosition = -1, endPosition = -1;
                for (int i = 0; i < timeline.tweets.size(); i += 1) {
                    if (timeline.tweets.get(i).id.equals(latestTimeline.tweets.get(0).id)) {
                        startPosition = i;
                        break;
                    }
                }
                if (startPosition >= 0) {
                    for (int i = startPosition; i < timeline.tweets.size(); i += 1) {
                        if (timeline.tweets.get(i).id.equals(
                                latestTimeline.tweets.get(latestTimeline.tweets.size() - 1).id)) {
                            endPosition = i;
                        }
                    }
                }
                if (startPosition >= 0 && endPosition >= startPosition) {
                    Iterator<Tweet> iterator = timeline.tweets.iterator();
                    iterator.next();

                    int i = 0;
                    while (i < startPosition) {
                        iterator.next();
                        i += 1;
                    }
                    while (i < endPosition) {
                        iterator.remove();
                        iterator.next();
                        i += 1;
                    }
                }
                timeline.tweets.addAll(startPosition, latestTimeline.tweets);

                onNoNewTweets(R.string.text_info_no_new_tweets);
            } else if (timeline.containsTweet(
                    latestTimeline.tweets.get(latestTimeline.tweets.size() - 1))) {
                Iterator<Tweet> iterator = timeline.tweets.iterator();
                iterator.next();
                Tweet tweet;
                do {
                    iterator.remove();
                    tweet = iterator.next();
                }
                while (!tweet.id.equals(latestTimeline.tweets.get(latestTimeline.tweets.size() - 1).id));
                iterator.remove();

                timeline.tweets.addAll(0, latestTimeline.tweets);
            } else {
                timeline.tweets.clear();
                timeline.tweets.addAll(latestTimeline.tweets);
            }
        }

        timeline.users.putAll(latestTimeline.users);

        adapter.notifyDataSetChanged();
    }


    protected void onNoNewTweets(@StringRes int messageId) {
        Snackbar.make(coordinatorLayout, messageId, Snackbar.LENGTH_SHORT).show();
    }


    @Subscribe
    public abstract void loadMoreTimeline(LoadMoreTweetsEvent event);


    protected void onNoMoreTweets(@StringRes int messageId) {
        Snackbar.make(coordinatorLayout, messageId, Snackbar.LENGTH_SHORT).show();
    }


    protected void viewTweet(ViewTweetEvent event) {
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


    public void viewImages(ViewImageEvent event) {
        new ImageViewer.Builder(this, (ArrayList<String>) event.imageUrls)
                .setStartPosition(event.startPosition)
                .show();
    }


    public void commentOrRetweetTweet(CommentTweetEvent event) {
        Intent intent = new Intent(this, ComposerActivity.class);

        int requestCode;
        if (event instanceof RetweetTweetEvent) {
            requestCode = REQUEST_COMPOSER_RETWEET;
            intent.putExtra("TYPE", ComposerActivity.TYPE_RETWEET);
            intent.putExtra("CONTENT", ((RetweetTweetEvent) event).retweetContent);
        } else {
            requestCode = REQUEST_COMPOSER_COMMENT;
            intent.putExtra("TYPE", ComposerActivity.TYPE_COMMENT);
        }
        intent.putExtra("TWEET", event.tweet);
        intent.putExtra("SOURCE_NICKNAME", event.sourceTweetNickname);
        intent.putExtra("SOURCE_CONTENT", event.sourceTweetContent);

        Pair<View, String> cardPair = Pair.create((View) event.card.cardView, "card");
        Pair<View, String> nicknamePair = Pair.create((View) event.nickname, "nickname");
        Pair<View, String> timePair = Pair.create((View) event.time, "time");
        Pair<View, String> contentPair = Pair.create((View) event.content, "content");
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, cardPair, nicknamePair, timePair, contentPair
        );

        startActivityForResult(intent, requestCode, options.toBundle());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COMPOSER_COMMENT && resultCode == RESULT_OK) {
            Snackbar.make(coordinatorLayout, R.string.text_info_comment_sent, Snackbar.LENGTH_SHORT)
                    .show();
        } else if (requestCode == REQUEST_COMPOSER_RETWEET && resultCode == RESULT_OK) {
            Snackbar.make(coordinatorLayout, R.string.text_info_retweet_sent, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

}
