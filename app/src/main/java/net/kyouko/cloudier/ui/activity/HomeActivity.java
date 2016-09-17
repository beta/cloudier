package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.otto.Subscribe;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.event.CommentTweetEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsEvent;
import net.kyouko.cloudier.event.RetweetTweetEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.ui.adapter.TimelineAdapter;
import net.kyouko.cloudier.ui.widget.listener.RecyclerViewDisabler;
import net.kyouko.cloudier.util.AuthUtil;
import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.RequestUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private final static int REQUEST_COMPOSER_NEW_TWEET = 0;
    private final static int REQUEST_COMPOSER_COMMENT = 1;
    private final static int REQUEST_COMPOSER_RETWEET = 2;


    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.avatar) SimpleDraweeView draweeAvatar;
    @BindView(R.id.title) TextView textTitle;
    @BindView(R.id.srl) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;

    private Account account;
    private User currentUser;

    private Timeline timeline = new Timeline();
    private TimelineAdapter adapter;

    private RecyclerViewDisabler recyclerViewDisabler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        initView();

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        checkAuthorization();
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


    private void initView() {
        initToolbar();
        initSwipeRefreshLayout();
        initRecyclerView();
        initFab();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        setTitle(null);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }


    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_500, R.color.light_blue_700);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.hideComposer();
                loadHomeTimeline();
            }
        });
    }


    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewDisabler = new RecyclerViewDisabler();
        recyclerView.addOnItemTouchListener(recyclerViewDisabler);

        adapter = new TimelineAdapter(this, timeline);
        recyclerView.setAdapter(adapter);
    }


    private void initFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewTweet();
            }
        });
    }


    private void checkAuthorization() {
        if (AuthUtil.hasAuthorized(this)) {
            account = AuthUtil.readAccount(this);
            draweeAvatar.setImageURI(ImageUtil.getInstance(this).parseImageUrl(account.avatarUrl));
            textTitle.setText(account.nickname);

            getAccountInfo();
        } else {
            AuthUtil.startAuth(this);
        }
    }


    private void getAccountInfo() {
        final Account account = AuthUtil.readAccount(this);

        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<User> userCall = api.getUser(RequestUtil.getOAuthParams(this), account.username);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                currentUser = response.body();

                account.nickname = currentUser.nickname;
                account.avatarUrl = currentUser.avatarUrl;
                AuthUtil.saveAccount(HomeActivity.this, account);

                updateAccountInfo();
                loadHomeTimeline();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_account,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getAccountInfo();
                            }
                        })
                        .show();
            }
        });
    }


    private void updateAccountInfo() {
        draweeAvatar.setImageURI(ImageUtil.getInstance(this).parseImageUrl(currentUser.avatarUrl));
        textTitle.setText(currentUser.nickname);
    }


    private void loadHomeTimeline() {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getHomeLatestTimeline(RequestUtil.getOAuthParams(this));
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                timeline.tweets.clear();
                timeline.tweets.addAll(response.body().tweets);
                timeline.users.putAll(response.body().users);

                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_timeline,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadHomeTimeline();
                            }
                        })
                        .show();
            }
        });
    }


    @Subscribe
    public void loadMoreHomeTimeline(LoadMoreTweetsEvent event) {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getMoreHomeTimeline(RequestUtil.getOAuthParams(this),
                timeline.tweets.get(timeline.tweets.size() - 1).timestamp);
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                timeline.tweets.addAll(response.body().tweets);
                timeline.users.putAll(response.body().users);

                adapter.notifyDataSetChanged();
                adapter.completeLoadingMore();
            }

            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                adapter.completeLoadingMore();
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_timeline,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadMoreHomeTimeline(new LoadMoreTweetsEvent());
                            }
                        })
                        .show();
            }
        });
    }


    private void createNewTweet() {
        fab.setEnabled(false);
        recyclerViewDisabler.setDisabled(true);
        swipeRefreshLayout.setEnabled(false);

        recyclerView.scrollToPosition(0);
        adapter.showComposer(account);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(HomeActivity.this, ComposerActivity.class);
                intent.putExtra("TYPE", ComposerActivity.TYPE_NEW);
                CardView composerCard = adapter.getComposerCard();
                if (composerCard != null) {
                    ActivityOptionsCompat options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(HomeActivity.this, composerCard, "card");
                    startActivityForResult(intent, REQUEST_COMPOSER_NEW_TWEET, options.toBundle());
                } else {
                    startActivityForResult(intent, REQUEST_COMPOSER_NEW_TWEET);
                }
            }
        }, 400);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COMPOSER_NEW_TWEET) {
            String content = data.getStringExtra("CONTENT");
            ((EditText) adapter.getComposerCard().findViewById(R.id.content)).setText(content);

            if (resultCode == RESULT_OK) {
                final boolean hasTweet = data.hasExtra("TWEET");
                if (hasTweet) {
                    Tweet tweet = (Tweet) data.getSerializableExtra("TWEET");
                    timeline.tweets.add(0, tweet);
                    timeline.users.putAll(tweet.users);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.hideComposer();
                        if (hasTweet) {
                            adapter.notifyItemChanged(0);
                        } else {
                            adapter.notifyItemRemoved(0);
                        }

                        fab.setEnabled(true);
                        recyclerViewDisabler.setDisabled(false);
                        swipeRefreshLayout.setEnabled(true);
                    }
                }, 350);
            } else if (resultCode == RESULT_CANCELED) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.hideComposer();
                        adapter.notifyItemRemoved(0);

                        fab.setEnabled(true);
                        recyclerViewDisabler.setDisabled(false);
                        swipeRefreshLayout.setEnabled(true);
                    }
                }, 350);
            }
        } else if (requestCode == REQUEST_COMPOSER_COMMENT && resultCode == RESULT_OK) {
            Snackbar.make(coordinatorLayout, R.string.text_info_comment_sent, Snackbar.LENGTH_SHORT)
                    .show();
        } else if (requestCode == REQUEST_COMPOSER_RETWEET && resultCode == RESULT_OK) {
            final boolean hasTweet = data.hasExtra("TWEET");
            if (hasTweet) {
                recyclerView.scrollToPosition(0);

                Tweet tweet = (Tweet) data.getSerializableExtra("TWEET");
                timeline.tweets.add(0, tweet);
                timeline.users.putAll(tweet.users);
                adapter.notifyItemInserted(0);
            }
        }
    }

}
