package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.otto.Subscribe;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.event.CommentTweetEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.Update;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.ui.widget.listener.RecyclerViewDisabler;
import net.kyouko.cloudier.util.AuthUtil;
import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.RequestUtil;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends TimelineActivity {

    protected final static int REQUEST_COMPOSER_NEW_TWEET = 2;

    protected final static int REQUEST_NOTIFICATIONS = 10;


    @BindView(R.id.avatar) SimpleDraweeView draweeAvatar;
    @BindView(R.id.title) TextView textTitle;
    @BindView(R.id.fab) FloatingActionButton fab;

    private Account account;
    private User currentUser;

    private MenuItem notificationMenuItem;

    private RecyclerViewDisabler recyclerViewDisabler;


    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_home;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        notificationMenuItem = menu.findItem(R.id.action_notification);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notification) {
            enterNotifications();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void initView() {
        super.initView();
        initFab();
    }


    @Override
    protected void initToolbar() {
        super.initToolbar();

        setTitle(null);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }


    @Override
    protected void initRecyclerView() {
        super.initRecyclerView();

        recyclerViewDisabler = new RecyclerViewDisabler();
        recyclerView.addOnItemTouchListener(recyclerViewDisabler);
    }


    private void initFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewTweet();
            }
        });
    }


    @Override
    protected void prepare() {
        checkAuthorization();
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
                loadTimeline();
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


    private void enterNotifications() {
        startActivityForResult(new Intent(this, NotificationsActivity.class), REQUEST_NOTIFICATIONS);
        overridePendingTransition(R.anim.swipe_in_from_right, R.anim.swipe_out_to_left);
    }


    private void loadNotificationsUpdate() {
        Call<Update> updateCall = RequestUtil.getApiInstance()
                .getUpdates(RequestUtil.getOAuthParams(this));
        updateCall.enqueue(new Callback<Update>() {
            @Override
            public void onResponse(Call<Update> call, Response<Update> response) {
                if (response.body() != null && response.body().newMentionsCount > 0) {
                    notificationMenuItem.setIcon(R.drawable.ic_notifications_white_24dp);

                    Snackbar.make(coordinatorLayout,
                            getString(R.string.text_info_new_notifications,
                                    response.body().newMentionsCount), Snackbar.LENGTH_LONG)
                            .setAction(R.string.title_action_view, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    enterNotifications();
                                }
                            })
                            .show();
                } else {
                    notificationMenuItem.setIcon(R.drawable.ic_notifications_none_white_24dp);
                }
            }

            @Override
            public void onFailure(Call<Update> call, Throwable t) {
                // Ignore
            }
        });
    }


    @Override
    protected void loadTimeline() {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getLatestHomeTimeline(RequestUtil.getOAuthParams(this));
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.body() != null && !response.body().tweets.isEmpty()) {
                    timeline.tweets.clear();
                    timeline.tweets.addAll(response.body().tweets);
                    timeline.users.putAll(response.body().users);

                    adapter.notifyDataSetChanged();
                } else {
                    onNoNewTweets();
                }

                loadNotificationsUpdate();
            }


            private void onNoNewTweets() {
                Snackbar.make(coordinatorLayout, R.string.text_info_no_new_tweets,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }


            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_timeline,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadTimeline();
                            }
                        })
                        .show();
            }
        });
    }


    @Subscribe
    @Override
    public void loadMoreTimeline(LoadMoreTweetsEvent event) {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getMoreHomeTimeline(RequestUtil.getOAuthParams(this),
                timeline.tweets.get(timeline.tweets.size() - 1).id,
                timeline.tweets.get(timeline.tweets.size() - 1).timestamp);
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                adapter.completeLoadingMore();

                if (response.body() != null && !response.body().tweets.isEmpty()) {
                    timeline.tweets.addAll(response.body().tweets);
                    timeline.users.putAll(response.body().users);

                    adapter.notifyDataSetChanged();
                } else {
                    onNoMoreTweets();
                }
            }


            private void onNoMoreTweets() {
                Snackbar.make(coordinatorLayout, R.string.text_info_no_more_tweets,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }


            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                adapter.completeLoadingMore();
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_timeline,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadMoreTimeline(new LoadMoreTweetsEvent());
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
    public void onViewTweet(ViewTweetEvent event) {
        viewTweet(event);
    }


    @Subscribe
    public void onViewImages(ViewImageEvent event) {
        viewImages(event);
    }


    @Subscribe
    public void onCommentOrRetweetTweet(CommentTweetEvent event) {
        commentOrRetweetTweet(event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
        } else if (requestCode == REQUEST_COMPOSER_RETWEET && resultCode == RESULT_OK) {
            final boolean hasTweet = data.hasExtra("TWEET");
            if (hasTweet) {
                recyclerView.scrollToPosition(0);

                Tweet tweet = (Tweet) data.getSerializableExtra("TWEET");
                timeline.tweets.add(0, tweet);
                timeline.users.putAll(tweet.users);
                adapter.notifyItemInserted(0);
            }
        } else if (requestCode == REQUEST_NOTIFICATIONS && resultCode == RESULT_OK) {
            notificationMenuItem.setIcon(R.drawable.ic_notifications_none_white_24dp);
        }
    }

}
