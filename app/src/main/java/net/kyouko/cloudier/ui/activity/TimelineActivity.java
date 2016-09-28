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
import net.kyouko.cloudier.event.DeleteTweetEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsEvent;
import net.kyouko.cloudier.event.RetweetTweetEvent;
import net.kyouko.cloudier.event.ShareTweetEvent;
import net.kyouko.cloudier.event.ShowTweetMenuEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.event.ViewUserEvent;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.TweetResult;
import net.kyouko.cloudier.ui.adapter.TimelineAdapter;
import net.kyouko.cloudier.util.PreferenceUtil;
import net.kyouko.cloudier.util.RequestUtil;
import net.kyouko.cloudier.util.TweetCardUtil;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base class for activities containing a {@link RecyclerView} for displaying timeline.
 *
 * @author beta
 */
public abstract class TimelineActivity extends AppCompatActivity {

    protected final static int REQUEST_COMPOSER_COMMENT = 0;
    protected final static int REQUEST_COMPOSER_RETWEET = 1;
    protected final static int REQUEST_VIEW_TWEET = 11;


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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findLastVisibleItemPosition() >= timeline.tweets.size() - 3) {
                    if (PreferenceUtil.with(TimelineActivity.this)
                            .getBoolean(PreferenceUtil.PREF_TIMELINE_AUTO_LOAD_MORE)) {
                        adapter.loadMore();
                    }
                }
            }
        });
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


    protected void viewUser(ViewUserEvent event) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("USERNAME", event.username);
        startActivity(intent);
    }


    protected void viewTweet(ViewTweetEvent event) {
        Intent intent = new Intent(this, TweetDetailActivity.class);

        if (event.type == ViewTweetEvent.TYPE_TWEET) {
            intent.putExtra("TWEET", event.tweet);
            intent.putExtra("USERS", event.users);
            if (event.card != null) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(this, event.card.cardView, "card");
                startActivityForResult(intent, REQUEST_VIEW_TWEET, options.toBundle());
            } else {
                startActivityForResult(intent, REQUEST_VIEW_TWEET);
            }
        } else if (event.type == ViewTweetEvent.TYPE_ID) {
            intent.putExtra("TWEET_ID", event.tweetId);
            startActivity(intent);
        }
    }


    protected void viewImages(ViewImageEvent event) {
        new ImageViewer.Builder(this, (ArrayList<String>) event.imageUrls)
                .setStartPosition(event.startPosition)
                .show();
    }


    protected void commentOrRetweetTweet(CommentTweetEvent event) {
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
        intent.putExtra("SOURCE_TIME", event.sourceTweetTime);
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


    protected void shareTweet(ShareTweetEvent event) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_pattern_tweet_link, event.tweetId));
        startActivity(Intent.createChooser(intent, getString(R.string.text_info_share_tweet)));
    }


    protected void showTweetMenu(ShowTweetMenuEvent event) {
        TweetCardUtil.showTweetMenu(this, event.tweet);
    }


    protected void deleteTweet(final DeleteTweetEvent event) {
        Call<TweetResult> deleteTweetCall = RequestUtil.getApiInstance().deleteTweet(
                RequestUtil.getConstantParams(), RequestUtil.getOAuthParams(this), event.tweetId);
        deleteTweetCall.enqueue(new Callback<TweetResult>() {
            @Override
            public void onResponse(Call<TweetResult> call, Response<TweetResult> response) {
                if (response.body() != null) {
                    Snackbar.make(coordinatorLayout, R.string.text_info_tweet_deleted,
                            Snackbar.LENGTH_SHORT)
                            .show();

                    for (int i = 0; i < timeline.tweets.size(); i += 1) {
                        if (timeline.tweets.get(i).id.equals(event.tweetId)) {
                            timeline.tweets.remove(i);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                } else {
                    onFailure();
                }
            }


            private void onFailure() {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_delete_tweet,
                        Snackbar.LENGTH_SHORT);
            }


            @Override
            public void onFailure(Call<TweetResult> call, Throwable t) {
                onFailure();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COMPOSER_COMMENT && resultCode == RESULT_OK) {
            Snackbar.make(coordinatorLayout, R.string.text_info_comment_sent, Snackbar.LENGTH_SHORT)
                    .show();
        } else if (requestCode == REQUEST_COMPOSER_RETWEET && resultCode == RESULT_OK) {
            Snackbar.make(coordinatorLayout, R.string.text_info_retweet_sent, Snackbar.LENGTH_SHORT)
                    .show();
        } else if (requestCode == REQUEST_VIEW_TWEET && resultCode == TweetDetailActivity.RESULT_DELETED) {
            String tweetId = data.getStringExtra("TWEET_ID");
            for (int i = 0; i < timeline.tweets.size(); i += 1) {
                if (timeline.tweets.get(i).id.equals(tweetId)) {
                    timeline.tweets.remove(i);
                    adapter.notifyItemRemoved(i);
                    break;
                }
            }

            Snackbar.make(coordinatorLayout, R.string.text_info_tweet_deleted,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

}
