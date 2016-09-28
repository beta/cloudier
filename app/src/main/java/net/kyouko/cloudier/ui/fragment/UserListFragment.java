package net.kyouko.cloudier.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.ui.adapter.UserListAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment for displaying a list of users.
 *
 * @author beta
 */
public class UserListFragment extends Fragment {

    @BindView(R.id.recycler) RecyclerView recyclerView;

    private ArrayList<User> users;
    private int userType = User.TYPE_FOLLOWING;

    private UserListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        users = (ArrayList<User>) getArguments().getSerializable("USERS");
        userType = getArguments().getInt("TYPE", User.TYPE_FOLLOWING);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        ButterKnife.bind(this, view);

        initView();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }


    private void initView() {
        initRecyclerView();
    }


    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UserListAdapter(getContext(), users, userType);
        recyclerView.setAdapter(adapter);
    }


    public void completeLoadingMore() {
        adapter.completeLoadingMore();
    }


    public void refreshList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    public void scrollToTop() {
        recyclerView.scrollToPosition(0);
    }

}
