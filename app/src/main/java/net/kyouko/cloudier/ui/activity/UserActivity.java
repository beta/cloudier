package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.CommentTweetEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsEvent;
import net.kyouko.cloudier.event.LoadMoreUsersEvent;
import net.kyouko.cloudier.event.RetweetTweetEvent;
import net.kyouko.cloudier.event.ShareTweetEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.event.ViewUserEvent;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.model.UserList;
import net.kyouko.cloudier.ui.adapter.TabsFragmentPagerAdapter;
import net.kyouko.cloudier.ui.fragment.TweetListFragment;
import net.kyouko.cloudier.ui.fragment.UserListFragment;
import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.RequestUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    private final static int REQUEST_COMPOSER_COMMENT = 0;
    private final static int REQUEST_COMPOSER_RETWEET = 1;


    @BindView(R.id.coordinator) CoordinatorLayout coordinator;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.avatar) ImageView avatar;
    @BindView(R.id.introduction) TextView introduction;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.pager) ViewPager viewPager;

    private String username;

    private TweetListFragment userTimelineFragment;
    private Timeline userTimeline = new Timeline();

    private UserListFragment followingFragment;
    private ArrayList<User> following = new ArrayList<>();

    private UserListFragment followersFragment;
    private ArrayList<User> followers = new ArrayList<>();

    private MenuItem followMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ButterKnife.bind(this);

        initView();

        fetchUsername();
        loadUser();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        followMenuItem = menu.findItem(R.id.action_follow);
        return true;
    }


    private void initView() {
        initToolbar();
        initTabs();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        setTitle(null);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initTabs() {
        TabsFragmentPagerAdapter adapter = new TabsFragmentPagerAdapter(getSupportFragmentManager());

        userTimelineFragment = new TweetListFragment();
        Bundle userTimelineArgs = new Bundle();
        userTimelineArgs.putSerializable("TIMELINE", userTimeline);
        userTimelineArgs.putBoolean("MINIMIZED", false);
        userTimelineFragment.setArguments(userTimelineArgs);
        adapter.add(getString(R.string.title_tab_tweets), userTimelineFragment);

        followingFragment = new UserListFragment();
        Bundle followingArgs = new Bundle();
        followingArgs.putSerializable("USERS", following);
        followingArgs.putInt("TYPE", User.TYPE_FOLLOWING);
        followingFragment.setArguments(followingArgs);
        adapter.add(getString(R.string.title_tab_following), followingFragment);

        followersFragment = new UserListFragment();
        Bundle followersArgs = new Bundle();
        followersArgs.putSerializable("USERS", followers);
        followersArgs.putInt("TYPE", User.TYPE_FOLLOWER);
        followersFragment.setArguments(followersArgs);
        adapter.add(getString(R.string.title_tab_followers), followersFragment);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void fetchUsername() {
        username = getIntent().getStringExtra("USERNAME");
    }


    private void loadUser() {
        Call<User> userCall = RequestUtil.getApiInstance()
                .getUser(RequestUtil.getOAuthParams(this), username);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body() != null) {
                    applyUserInfo(response.body());

                    loadTimeline();
                    loadFollowingList();
                    loadFollowerList();
                } else {
                    onFailure();
                }
            }


            private void onFailure() {
                Snackbar.make(coordinator, R.string.text_error_failed_to_fetch_user,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadUser();
                            }
                        })
                        .show();
            }


            @Override
            public void onFailure(Call<User> call, Throwable t) {
                onFailure();
            }
        });
    }


    private void applyUserInfo(User user) {
        setTitle(user.nickname);
        Picasso.with(this)
                .load(Uri.parse(ImageUtil.getInstance(UserActivity.this).parseImageUrl(user.avatarUrl)))
                .placeholder(R.color.grey_300)
                .into(avatar, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        BitmapDrawable drawable = (BitmapDrawable) avatar.getDrawable();
                        applyThemeColors(drawable.getBitmap());
                    }

                    @Override
                    public void onError() {
                        // Ignore
                    }
                });
        introduction.setText(user.introduction);

        if (user.followed) {
            followMenuItem.setIcon(R.drawable.ic_person_delete_white_24dp);
            followMenuItem.setTitle(R.string.title_action_unfollow);
        } else {
            followMenuItem.setIcon(R.drawable.ic_person_add_white_24dp);
            followMenuItem.setTitle(R.string.title_action_follow);
        }
    }


    private void applyThemeColors(Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();

        int primaryColor = palette.getMutedColor(getResources().getColor(R.color.light_blue_500));
        collapsingToolbarLayout.setBackgroundColor(primaryColor);
        collapsingToolbarLayout.setContentScrimColor(primaryColor);
        tabLayout.setBackgroundColor(primaryColor);

        int primaryDarkColor = palette.getDarkMutedColor(getResources().getColor(R.color.light_blue_700));
        getWindow().setStatusBarColor(primaryDarkColor);
    }


    private void loadTimeline() {
        Call<Timeline> timelineCall = RequestUtil.getApiInstance()
                .getLatestUserTimeline(RequestUtil.getOAuthParams(this), username);
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                if (response.body() != null) {
                    Timeline timeline = response.body();
                    userTimeline.tweets.clear();
                    userTimeline.tweets.addAll(timeline.tweets);
                    userTimeline.users.putAll(timeline.users);
                    userTimelineFragment.refreshTweetList();
                } else {
                    onFailure();
                }
            }


            private void onFailure() {
                Snackbar.make(coordinator, R.string.text_error_failed_to_fetch_timeline,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadTimeline();
                            }
                        })
                        .show();
            }


            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                onFailure();
            }
        });
    }


    private void loadFollowingList() {
        Call<UserList> followingCall = RequestUtil.getApiInstance()
                .getFollowingList(RequestUtil.getOAuthParams(this), username);
        followingCall.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, Response<UserList> response) {
                if (response.body() != null) {
                    following.clear();
                    following.addAll(response.body().users);

                    followingFragment.refreshList();
                }
            }


            @Override
            public void onFailure(Call<UserList> call, Throwable t) {
                // Ignore
            }
        });
    }


    private void loadFollowerList() {
        Call<UserList> followerCall = RequestUtil.getApiInstance()
                .getFollowerList(RequestUtil.getOAuthParams(this), username);
        followerCall.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, Response<UserList> response) {
                if (response.body() != null) {
                    followers.clear();
                    followers.addAll(response.body().users);

                    followersFragment.refreshList();
                }
            }


            @Override
            public void onFailure(Call<UserList> call, Throwable t) {
                // Ignore
            }
        });
    }


    @Subscribe
    public void onLoadMoreTweets(LoadMoreTweetsEvent event) {
        Call<Timeline> timelineCall = RequestUtil.getApiInstance()
                .getMoreUserTimeline(RequestUtil.getOAuthParams(this), username,
                        userTimeline.tweets.get(userTimeline.tweets.size() - 1).id,
                        userTimeline.tweets.get(userTimeline.tweets.size() - 1).timestamp);
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                userTimelineFragment.completeLoadingMore();

                if (response.body() != null) {
                    Timeline timeline = response.body();

                    if (timeline.tweets.isEmpty()) {
                        Snackbar.make(coordinator, R.string.text_info_no_more_tweets,
                                Snackbar.LENGTH_SHORT).show();
                    } else {
                        userTimeline.tweets.addAll(timeline.tweets);
                        userTimeline.users.putAll(timeline.users);
                    }
                } else {
                    Snackbar.make(coordinator, R.string.text_info_no_more_tweets,
                            Snackbar.LENGTH_SHORT).show();
                }

                userTimelineFragment.refreshTweetList();
            }


            private void onFailure() {
                userTimelineFragment.completeLoadingMore();
                userTimelineFragment.refreshTweetList();

                Snackbar.make(coordinator, R.string.text_error_failed_to_fetch_timeline,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadTimeline();
                            }
                        })
                        .show();
            }


            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                onFailure();
            }
        });
    }


    @Subscribe
    public void onLoadMoreFollowingOrFollowers(final LoadMoreUsersEvent event) {
        if (event.type == User.TYPE_FOLLOWING) {
            Call<UserList> followingCall = RequestUtil.getApiInstance()
                    .getMoreFollowingList(RequestUtil.getOAuthParams(this), username, following.size());
            followingCall.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(Call<UserList> call, Response<UserList> response) {
                    followingFragment.completeLoadingMore();
                    if (response.body() != null && response.body().users != null) {
                        following.addAll(response.body().users);
                    } else {
                        Snackbar.make(coordinator, R.string.text_info_no_more_following,
                                Snackbar.LENGTH_SHORT).show();
                    }
                    followingFragment.refreshList();
                }


                @Override
                public void onFailure(Call<UserList> call, Throwable t) {
                    followingFragment.completeLoadingMore();
                    followingFragment.refreshList();
                    Snackbar.make(coordinator, R.string.text_error_failed_to_fetch_following,
                            Snackbar.LENGTH_SHORT)
                            .setAction(R.string.title_action_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onLoadMoreFollowingOrFollowers(event);
                                }
                            })
                            .show();
                }
            });
        } else if (event.type == User.TYPE_FOLLOWER) {
            Call<UserList> followersCall = RequestUtil.getApiInstance()
                    .getMoreFollowerList(RequestUtil.getOAuthParams(this), username, followers.size());
            followersCall.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(Call<UserList> call, Response<UserList> response) {
                    followersFragment.completeLoadingMore();
                    if (response.body() != null && response.body().users != null) {
                        followers.addAll(response.body().users);
                    } else {
                        Snackbar.make(coordinator, R.string.text_info_no_more_followers,
                                Snackbar.LENGTH_SHORT).show();
                    }
                    followersFragment.refreshList();
                }

                @Override
                public void onFailure(Call<UserList> call, Throwable t) {
                    followersFragment.completeLoadingMore();
                    followersFragment.refreshList();
                    Snackbar.make(coordinator, R.string.text_error_failed_to_fetch_followers,
                            Snackbar.LENGTH_SHORT)
                            .setAction(R.string.title_action_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onLoadMoreFollowingOrFollowers(event);
                                }
                            })
                            .show();
                }
            });
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
    public void viewUser(ViewUserEvent event) {
        if (!event.username.equals(username)) {
            Intent intent = new Intent(this, UserActivity.class);
            intent.putExtra("USERNAME", event.username);
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

}
