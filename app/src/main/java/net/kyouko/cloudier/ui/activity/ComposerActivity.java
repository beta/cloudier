package net.kyouko.cloudier.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.SourceTweet;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.TweetResult;
import net.kyouko.cloudier.ui.widget.listener.TweetTextCountWatcher;
import net.kyouko.cloudier.util.AuthUtil;
import net.kyouko.cloudier.util.DateTimeUtil;
import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.RequestUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComposerActivity extends AppCompatActivity {

    public final static int TYPE_NEW = 0;
    public final static int TYPE_COMMENT = 1;
    public final static int TYPE_RETWEET = 2;


    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.background) View background;
    @BindView(R.id.composer) CardView cardView;
    @BindView(R.id.avatar) SimpleDraweeView avatar;
    @BindView(R.id.nickname) TextView nickname;
    @BindView(R.id.username) TextView username;
    @BindView(R.id.content) EditText content;
    @BindView(R.id.source_card) CardView sourceCard;
    @BindView(R.id.source_nickname) TextView sourceNickname;
    @BindView(R.id.source_time) TextView sourceTime;
    @BindView(R.id.source_content) TextView sourceContent;
    @BindView(R.id.word_count) TextView wordCount;

    private Account account;

    private int composerType = TYPE_NEW;
    private SourceTweet sourceTweet;

    private MenuItem sendMenuItem;
    private ImageView sendButton;
    private ProgressBar progressBar;

    private int shortAnimationDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composer);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        ButterKnife.bind(this);

        fetchComposerType();

        initView();
    }


    private void fetchComposerType() {
        composerType = getIntent().getIntExtra("TYPE", TYPE_NEW);
        switch (composerType) {
            case TYPE_NEW:
            default:
                setTitle(R.string.title_activity_composer);
                break;
            case TYPE_COMMENT:
                setTitle(R.string.title_activity_composer_comment);
                break;
            case TYPE_RETWEET:
                setTitle(R.string.title_activity_composer_retweet);
                break;
        }
    }


    private void initView() {
        initToolbar();
        initComposer();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelComposing();
            }
        });
    }


    private void initComposer() {
        account = AuthUtil.readAccount(this);

        avatar.setImageURI(Uri.parse(ImageUtil.getInstance(this).parseImageUrl(account.avatarUrl)));
        nickname.setText(account.nickname);
        username.setText(getString(R.string.text_pattern_username, account.username));

        switch (composerType) {
            case TYPE_NEW:
            default:
                content.setHint(R.string.text_hint_composer_content);
                break;
            case TYPE_COMMENT:
            case TYPE_RETWEET:
                content.setHint(R.string.text_hint_composer_content_comment);
                break;
        }

        TweetTextCountWatcher watcher = new TweetTextCountWatcher(wordCount);
        content.addTextChangedListener(watcher);
        watcher.applyWordCountAvailable(content.getText().toString());
        content.setText(getIntent().getStringExtra("CONTENT"));

        if (composerType == TYPE_COMMENT || composerType == TYPE_RETWEET) {
            sourceTweet = (SourceTweet) getIntent().getSerializableExtra("TWEET");
            sourceCard.setVisibility(View.VISIBLE);

            sourceNickname.setText(sourceTweet.nickname);
            sourceTime.setText(DateTimeUtil.getDateTimeDescription(this, sourceTweet.timestamp));

            String sourceTweetContent = getIntent().getStringExtra("SOURCE_CONTENT");
            if (sourceTweetContent.length() > 0) {
                sourceContent.setText(sourceTweetContent);
                sourceContent.setVisibility(View.VISIBLE);
            } else {
                sourceContent.setVisibility(View.GONE);
            }
        } else {
            sourceCard.setVisibility(View.GONE);
        }

        if (content.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            content.setSelection(0, 0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_composer, menu);

        sendMenuItem = menu.findItem(R.id.action_send);

        sendButton = (ImageView) menu.findItem(R.id.action_send).getActionView();
        if (sendButton != null) {
            int sendImageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    getResources().getDisplayMetrics());
            sendButton.setLayoutParams(new ViewGroup.LayoutParams(sendImageSize, sendImageSize));

            int progressBarSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24,
                    getResources().getDisplayMetrics());
            int progressBarPaddingEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    16, getResources().getDisplayMetrics());
            progressBar = new ProgressBar(this);
            progressBar.setLayoutParams(new ViewGroup.LayoutParams(
                    progressBarSize + progressBarPaddingEnd, progressBarSize));
            progressBar.setPadding(0, 0, progressBarPaddingEnd, 0);

            progressBar.setIndeterminate(true);
            progressBar.getIndeterminateDrawable().setColorFilter(
                    getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);


            sendButton.setScaleType(ImageView.ScaleType.CENTER);

            int[] attrs = new int[]{android.R.attr.actionBarItemBackground};
            TypedArray typedArray = obtainStyledAttributes(attrs);
            sendButton.setBackground(typedArray.getDrawable(0));
            typedArray.recycle();

            sendButton.setImageResource(R.drawable.ic_send_white_24dp);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (composerType) {
                        case TYPE_NEW:
                        default:
                            sendTweet();
                            break;
                        case TYPE_COMMENT:
                            comment();
                            break;
                        case TYPE_RETWEET:
                            retweet();
                            break;
                    }
                }
            });
        }
        return true;
    }


    private void playSendingTweetAnimation() {
        sendButton.setOnClickListener(null);

        final float sendButtonX = sendButton.getX();

        ViewAnimator
                .animate(sendButton)
                .translationX(sendButton.getWidth())
                .interpolator(new AccelerateInterpolator())
                .duration(350)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        sendMenuItem.setActionView(progressBar);
                        sendButton.setX(sendButtonX);
                        ViewAnimator
                                .animate(progressBar)
                                .scale(1)
                                .duration(shortAnimationDuration)
                                .start();
                    }
                })
                .start();
    }


    private void playSendingFailedAnimation() {
        final float sendButtonX = sendButton.getX();
        ViewAnimator
                .animate(progressBar)
                .duration(shortAnimationDuration)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        sendMenuItem.setActionView(sendButton);
                        ViewAnimator
                                .animate(sendButton)
                                .translationX(sendButtonX - sendButton.getWidth(), sendButtonX)
                                .interpolator(new DecelerateInterpolator())
                                .duration(350)
                                .onStop(new AnimationListener.Stop() {
                                    @Override
                                    public void onStop() {
                                        sendButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                sendTweet();
                                            }
                                        });
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }


    private void sendTweet() {
        content.setEnabled(false);

        playSendingTweetAnimation();

        Call<TweetResult> sendTweetCall = RequestUtil.getApiInstance().postTweet(
                RequestUtil.getConstantParams(), RequestUtil.getOAuthParams(this),
                content.getText().toString());
        sendTweetCall.enqueue(new Callback<TweetResult>() {
            @Override
            public void onResponse(Call<TweetResult> call, Response<TweetResult> response) {
                if (response.body() != null) {
                    getTweet(response.body().tweetId);
                } else {
                    onFailure();
                }
            }

            @Override
            public void onFailure(Call<TweetResult> call, Throwable t) {
                onFailure();
            }


            private void onFailure() {
                content.setEnabled(true);

                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_send_tweet,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendTweet();
                                    }
                                })
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSendingFailedAnimation();
                    }
                }, 1000);
            }
        });
    }


    private void comment() {
        content.setEnabled(false);

        playSendingTweetAnimation();

        Call<TweetResult> commentCall = RequestUtil.getApiInstance().comment(
                RequestUtil.getConstantParams(), RequestUtil.getOAuthParams(this),
                sourceTweet.id, content.getText().toString());
        commentCall.enqueue(new Callback<TweetResult>() {
            @Override
            public void onResponse(Call<TweetResult> call, Response<TweetResult> response) {
                if (response.body() != null) {
                    getTweet(response.body().tweetId);
                } else {
                    onFailure();
                }
            }

            @Override
            public void onFailure(Call<TweetResult> call, Throwable t) {
                onFailure();
            }


            private void onFailure() {
                content.setEnabled(true);

                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_comment,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendTweet();
                                    }
                                })
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSendingFailedAnimation();
                    }
                }, 1000);
            }
        });
    }


    private void retweet() {
        content.setEnabled(false);

        playSendingTweetAnimation();

        Call<TweetResult> retweetCall = RequestUtil.getApiInstance().retweet(
                RequestUtil.getConstantParams(), RequestUtil.getOAuthParams(this),
                sourceTweet.id, content.getText().toString());
        retweetCall.enqueue(new Callback<TweetResult>() {
            @Override
            public void onResponse(Call<TweetResult> call, Response<TweetResult> response) {
                if (response.body() != null) {
                    getTweet(response.body().tweetId);
                } else {
                    onFailure();
                }
            }

            @Override
            public void onFailure(Call<TweetResult> call, Throwable t) {
                Log.e("error", t.getLocalizedMessage());
                onFailure();
            }


            private void onFailure() {
                content.setEnabled(true);

                Snackbar.make(coordinatorLayout, R.string.text_error_failed_to_retweet,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.title_action_retry,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sendTweet();
                                    }
                                })
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSendingFailedAnimation();
                    }
                }, 1000);
            }
        });
    }


    private void getTweet(String tweetId) {
        Call<Tweet> tweetCall = RequestUtil.getApiInstance().getTweet(
                RequestUtil.getOAuthParams(this), tweetId);
        tweetCall.enqueue(new Callback<Tweet>() {
            @Override
            public void onResponse(Call<Tweet> call, Response<Tweet> response) {
                finishComposing(response.body());
            }

            @Override
            public void onFailure(Call<Tweet> call, Throwable t) {
                finishComposing(null);
            }
        });
    }


    @Override
    public void onBackPressed() {
        cancelComposing();
    }


    private void cancelComposing() {
        Intent result = new Intent();
        result.putExtra("CONTENT", content.getText().toString());
        setResult(RESULT_CANCELED, result);
        finish();
    }


    private void finishComposing(@Nullable Tweet tweet) {
        Intent result = new Intent();
        result.putExtra("CONTENT", content.getText().toString());
        if (tweet != null) {
            result.putExtra("TWEET", tweet);
        }
        setResult(RESULT_OK, result);
        finish();
    }

}