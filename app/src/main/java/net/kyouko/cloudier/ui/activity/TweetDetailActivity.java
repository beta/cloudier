package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.CommentTweetEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsWithTypeEvent;
import net.kyouko.cloudier.event.RetweetTweetEvent;
import net.kyouko.cloudier.event.ShareTweetEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.ui.adapter.TabsFragmentPagerAdapter;
import net.kyouko.cloudier.ui.fragment.TweetListFragment;
import net.kyouko.cloudier.util.RequestUtil;
import net.kyouko.cloudier.util.TweetCardUtil;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TweetDetailActivity extends AppCompatActivity implements
        AppBarLayout.OnOffsetChangedListener {

    private final static int REQUEST_COMPOSER_COMMENT = 0;
    private final static int REQUEST_COMPOSER_RETWEET = 1;


    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.srl) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.card) CardView cardView;

    private SourceTweet tweet;
    private HashMap<String, String> users;

    private Timeline commentsTimeline = new Timeline();
    private Timeline retweetsTimeline = new Timeline();

    private TweetListFragment commentsFragment;
    private TweetListFragment retweetsFragment;

    private String tweetId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        ButterKnife.bind(this);

        initView();

        fetchTweet();
        loadTweet();
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

        actionBarSize -= swipeRefreshLayout.getProgressCircleDiameter() / 2;

        swipeEndPosition = actionBarSize + 64 * displayMetrics.density;

        swipeRefreshLayout.setProgressViewOffset(true, (int) actionBarSize, (int) swipeEndPosition);

        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_500, R.color.light_blue_700);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTweet();
            }
        });
    }


    private void initTabs() {
        TabsFragmentPagerAdapter adapter = new TabsFragmentPagerAdapter(getSupportFragmentManager());

        commentsFragment = new TweetListFragment();
        Bundle commentsArgs = new Bundle();
        commentsArgs.putSerializable("TIMELINE", commentsTimeline);
        commentsArgs.putInt("TYPE", Tweet.TYPE_COMMENT);
        commentsFragment.setArguments(commentsArgs);
        adapter.add(getString(R.string.title_tab_comments), commentsFragment);

        retweetsFragment = new TweetListFragment();
        Bundle retweetArgs = new Bundle();
        retweetArgs.putSerializable("TIMELINE", retweetsTimeline);
        retweetArgs.putInt("TYPE", Tweet.TYPE_RETWEET);
        retweetsFragment.setArguments(retweetArgs);
        adapter.add(getString(R.string.title_tab_retweets), retweetsFragment);

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
    }


    private void fetchTweet() {
        SourceTweet tweet = (SourceTweet) getIntent().getSerializableExtra("TWEET");
        users = (HashMap<String, String>) getIntent().getSerializableExtra("USERS");
        TweetCardUtil.displayTweet(tweet, users, cardView);

        tweetId = tweet.id;
    }


    private void loadTweet() {
        Call<Tweet> tweetCall = RequestUtil.getApiInstance().getTweet(
                RequestUtil.getOAuthParams(this), tweetId);
        tweetCall.enqueue(new Callback<Tweet>() {
            @Override
            public void onResponse(Call<Tweet> call, Response<Tweet> response) {
                swipeRefreshLayout.setRefreshing(false);

                tweet = response.body();
                if (tweet != null) {
                    TweetCardUtil.displayTweet(tweet, users, cardView);

                    loadComments();
                    loadRetweets();
                } else {
                    onFailure();
                }
            }


            private void onFailure() {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_tweet,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadTweet();
                            }
                        })
                        .show();
            }


            @Override
            public void onFailure(Call<Tweet> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                onFailure();
            }
        });
    }


    private void loadComments() {
        Call<Timeline> commentsCall = RequestUtil.getApiInstance()
                .getTweetComments(RequestUtil.getOAuthParams(this), tweetId);
        commentsCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                commentsTimeline.tweets.clear();
                commentsTimeline.users.clear();

                if (response.body() != null) {
                    commentsTimeline.tweets.addAll(response.body().tweets);
                    commentsTimeline.users.putAll(response.body().users);
                }

                commentsFragment.refreshTweetList();
            }

            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_comments,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadComments();
                            }
                        })
                        .show();
            }
        });
    }


    private void loadMoreComments() {
        Call<Timeline> commentsCall = RequestUtil.getApiInstance()
                .getMoreTweetComments(RequestUtil.getOAuthParams(this), tweetId,
                        commentsTimeline.tweets.get(commentsTimeline.tweets.size() - 1).id,
                        commentsTimeline.tweets.get(commentsTimeline.tweets.size() - 1).timestamp);
        commentsCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                if (response.body() != null) {
                    if (response.body().tweets.isEmpty()) {
                        Snackbar.make(coordinatorLayout, R.string.text_info_no_more_comments,
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        commentsTimeline.tweets.addAll(response.body().tweets);
                        commentsTimeline.users.putAll(response.body().users);
                    }
                } else {
                    Snackbar.make(coordinatorLayout, R.string.text_info_no_more_comments,
                            Snackbar.LENGTH_SHORT).show();
                }

                commentsFragment.completeLoadingMore();
                commentsFragment.refreshTweetList();
            }

            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_comments,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadMoreComments();
                            }
                        })
                        .show();
            }
        });
    }


    private void loadRetweets() {
        Call<Timeline> retweetsCall = RequestUtil.getApiInstance()
                .getTweetRetweets(RequestUtil.getOAuthParams(this), tweetId);
        retweetsCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                retweetsTimeline.tweets.clear();
                retweetsTimeline.users.clear();

                if (response.body() != null) {
                    retweetsTimeline.tweets.addAll(response.body().tweets);
                    retweetsTimeline.users.putAll(response.body().users);
                }

                retweetsFragment.refreshTweetList();
            }

            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_retweets,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadRetweets();
                            }
                        })
                        .show();
            }
        });
    }


    private void loadMoreRetweets() {
        Call<Timeline> retweetsCall = RequestUtil.getApiInstance()
                .getMoreTweetRetweets(RequestUtil.getOAuthParams(this), tweetId,
                        retweetsTimeline.tweets.get(retweetsTimeline.tweets.size() - 1).id,
                        retweetsTimeline.tweets.get(retweetsTimeline.tweets.size() - 1).timestamp);
        retweetsCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                if (response.body() != null) {
                    if (response.body().tweets.isEmpty()) {
                        Snackbar.make(coordinatorLayout, R.string.text_info_no_more_retweets,
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        retweetsTimeline.tweets.addAll(response.body().tweets);
                        retweetsTimeline.users.putAll(response.body().users);
                    }
                } else {
                    Snackbar.make(coordinatorLayout, R.string.text_info_no_more_retweets,
                            Snackbar.LENGTH_SHORT).show();
                }

                retweetsFragment.completeLoadingMore();
                retweetsFragment.refreshTweetList();
            }

            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_retweets,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadMoreRetweets();
                            }
                        })
                        .show();
            }
        });
    }


    @Subscribe
    public void loadMoreTweets(LoadMoreTweetsWithTypeEvent event) {
        if (event.type == Tweet.TYPE_COMMENT) {
            loadMoreComments();
        } else if (event.type == Tweet.TYPE_RETWEET) {
            loadMoreRetweets();
        }
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


    @Subscribe
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


    @Subscribe
    public void onShareTweet(ShareTweetEvent event) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_pattern_tweet_link, event.tweetId));
        startActivity(Intent.createChooser(intent, getString(R.string.text_info_share_tweet)));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COMPOSER_COMMENT && resultCode == RESULT_OK) {
            tweet.commentCount += 1;
            ((TextView) cardView.findViewById(R.id.comment_count)).setText(String.valueOf(tweet.commentCount));

            final boolean hasTweet = data.hasExtra("TWEET");
            if (hasTweet) {
                Tweet tweet = (Tweet) data.getSerializableExtra("TWEET");
                commentsTimeline.tweets.add(0, tweet);
                commentsTimeline.users.putAll(tweet.users);
                commentsFragment.notifyItemInserted(0);
            }
        } else if (requestCode == REQUEST_COMPOSER_RETWEET && resultCode == RESULT_OK) {
            tweet.retweetCount += 1;
            ((TextView) cardView.findViewById(R.id.retweet_count)).setText(String.valueOf(tweet.retweetCount));

            final boolean hasTweet = data.hasExtra("TWEET");
            if (hasTweet) {
                Tweet tweet = (Tweet) data.getSerializableExtra("TWEET");
                retweetsTimeline.tweets.add(0, tweet);
                retweetsTimeline.users.putAll(tweet.users);
                retweetsFragment.notifyItemInserted(0);
            }
        }
    }

}
