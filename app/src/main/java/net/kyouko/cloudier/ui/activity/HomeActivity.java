package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.otto.Subscribe;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.ui.adapter.TimelineAdapter;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.event.LoadMoreEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.User;
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

    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.avatar) SimpleDraweeView draweeAvatar;
    @BindView(R.id.title) TextView textTitle;
    @BindView(R.id.srl) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler) RecyclerView recyclerView;

    private User currentUser;

    private Timeline timeline = new Timeline();
    private TimelineAdapter adapter;


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
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        setTitle(null);
    }


    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_500, R.color.light_blue_700);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadHomeTimeline();
            }
        });
    }


    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TimelineAdapter(this, timeline);
        recyclerView.setAdapter(adapter);
    }


    private void checkAuthorization() {
        if (AuthUtil.hasAuthorized(this)) {
            Account account = AuthUtil.readAccount(this);
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
        Call<User> userCall = api.getUser(RequestUtil.createOAuthParams(this), account.username);
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
        Call<Timeline> timelineCall = api.getHomeLatestTimeline(RequestUtil.createOAuthParams(this));
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
    public void loadMoreHomeTimeline(LoadMoreEvent event) {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getMoreHomeTimeline(RequestUtil.createOAuthParams(this),
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
                                loadMoreHomeTimeline(new LoadMoreEvent());
                            }
                        })
                        .show();
            }
        });
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
