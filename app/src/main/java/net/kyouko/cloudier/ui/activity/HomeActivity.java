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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.adapter.TimelineAdapter;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.util.AuthUtil;
import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.RequestUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements
        TimelineAdapter.OnViewImagesListener, TimelineAdapter.OnViewTweetListener {

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
                fetchHomeTimeline();
            }
        });
    }


    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TimelineAdapter(this, timeline);
        adapter.setOnViewTweetListener(this);
        adapter.setOnViewImagesListener(this);
        recyclerView.setAdapter(adapter);
    }


    private void checkAuthorization() {
        if (AuthUtil.hasAuthorized(this)) {
            getAccountInfo();
        } else {
            AuthUtil.startAuth(this);
        }
    }


    private void getAccountInfo() {
        Account account = AuthUtil.readAccount(this);

        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<User> userCall = api.getUser(RequestUtil.createOAuthParams(this), account.username);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                currentUser = response.body();
                updateAccountInfo();
                fetchHomeTimeline();
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


    private void fetchHomeTimeline() {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getHomeLatestTimeline(RequestUtil.createOAuthParams(this));
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                timeline.tweets.clear();
                timeline.tweets.addAll(response.body().tweets);

                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                Log.e("error", t.getLocalizedMessage());
            }
        });
    }


    @Override
    public void onViewTweet(Tweet tweet, View card) {
        Intent intent = new Intent(this, TweetDetailActivity.class);
        intent.putExtra("TWEET", tweet);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, card, "card");
        startActivity(intent, options.toBundle());
    }


    @Override
    public void onViewImages(List<String> imageUrls) {
        new ImageViewer.Builder(this, (ArrayList<String>) imageUrls).show();
    }

}
