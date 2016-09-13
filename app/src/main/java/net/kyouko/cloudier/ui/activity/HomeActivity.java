package net.kyouko.cloudier.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import net.kyouko.cloudier.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.srl) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler) RecyclerView recyclerView;


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
        fetchHomeTimeline();
    }


    private void initView() {
        initToolbar();
        initSwipeRefreshLayout();
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
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


    private void fetchHomeTimeline() {
        // TODO
    }

}
