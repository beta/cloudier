package net.kyouko.cloudier.ui.activity;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.squareup.otto.Subscribe;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.event.CommentTweetEvent;
import net.kyouko.cloudier.event.LoadMoreTweetsEvent;
import net.kyouko.cloudier.event.ShareTweetEvent;
import net.kyouko.cloudier.event.ViewImageEvent;
import net.kyouko.cloudier.event.ViewTweetEvent;
import net.kyouko.cloudier.event.ViewUserEvent;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Update;
import net.kyouko.cloudier.util.RequestUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends TimelineActivity {

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_notifications;
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }


    @Override
    protected void initToolbar() {
        super.initToolbar();
        textTitle.setText(R.string.title_activity_notifications);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    @Override
    protected void loadLatestTimeline() {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getLatestNotificationTimeline(RequestUtil.getOAuthParams(this));
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                if (response.body() != null && !response.body().tweets.isEmpty()) {
                    mergeLatestTimeline(response.body());
                } else {
                    onNoNewTweets(R.string.text_info_no_new_tweets);
                }

                clearMentionsUpdate();
            }


            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);

                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_notifications,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadLatestTimeline();
                            }
                        })
                        .show();
            }
        });
    }


    private void clearMentionsUpdate() {
        Call<Update> clearUpdateCall = RequestUtil.getApiInstance().clearMentionsUpdate(
                RequestUtil.getOAuthParams(this));
        clearUpdateCall.enqueue(new Callback<Update>() {
            @Override
            public void onResponse(Call<Update> call, Response<Update> response) {
                swipeRefreshLayout.setRefreshing(false);
                setResult(RESULT_OK);
            }

            @Override
            public void onFailure(Call<Update> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    @Override
    @Subscribe
    public void loadMoreTimeline(LoadMoreTweetsEvent event) {
        TencentWeiboApi api = RequestUtil.getApiInstance();
        Call<Timeline> timelineCall = api.getMoreNotificationsTimeline(RequestUtil.getOAuthParams(this),
                timeline.tweets.get(timeline.tweets.size() - 1).id,
                timeline.tweets.get(timeline.tweets.size() - 1).timestamp);
        timelineCall.enqueue(new Callback<Timeline>() {
            @Override
            public void onResponse(Call<Timeline> call, Response<Timeline> response) {
                adapter.completeLoadingMore();

                if (response.body() != null && !response.body().tweets.isEmpty()) {
                    timeline.tweets.addAll(response.body().tweets);
                    timeline.users.putAll(response.body().users);
                    timeline.hasMoreTweetsFlag = response.body().hasMoreTweetsFlag;

                    adapter.notifyDataSetChanged();
                } else {
                    onNoMoreTweets(R.string.text_info_no_more_notifications);
                }
            }


            @Override
            public void onFailure(Call<Timeline> call, Throwable t) {
                adapter.completeLoadingMore();
                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_fetch_notifications,
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


    @Subscribe
    public void onViewUser(ViewUserEvent event) {
        viewUser(event);
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


    @Subscribe
    public void onShareTweet(ShareTweetEvent event) {
        shareTweet(event);
    }

}
