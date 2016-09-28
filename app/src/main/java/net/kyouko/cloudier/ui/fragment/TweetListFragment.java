package net.kyouko.cloudier.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.ui.adapter.TimelineAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment for displaying a list of tweets.
 *
 * @author beta
 */
public class TweetListFragment extends Fragment {

    @BindView(R.id.recycler) RecyclerView recyclerView;

    private Timeline timeline;

    private boolean minimized = true;

    private boolean hasTweetType = false;
    private int tweetType = Tweet.TYPE_ORIGINAL;

    private TimelineAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timeline = (Timeline) getArguments().getSerializable("TIMELINE");
        hasTweetType = getArguments().containsKey("TYPE");
        tweetType = getArguments().getInt("TYPE", Tweet.TYPE_ORIGINAL);
        minimized = getArguments().getBoolean("MINIMIZED", true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        ButterKnife.bind(this, view);

        initView();

        return view;
    }


    private void initView() {
        initRecyclerView();
    }


    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TimelineAdapter(getContext(), timeline, true, minimized);
        if (hasTweetType) {
            adapter.setTweetType(tweetType);
        }
        recyclerView.setAdapter(adapter);
    }


    public void refreshTweetList() {
        adapter.notifyDataSetChanged();
    }


    public void completeLoadingMore() {
        adapter.completeLoadingMore();
    }


    public void notifyItemInserted(int position) {
        adapter.notifyItemInserted(position);
    }


    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

}
