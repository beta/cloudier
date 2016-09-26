package net.kyouko.cloudier.ui.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.User;
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
        adapter.add(getString(R.string.title_tab_following), followersFragment);

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

}
