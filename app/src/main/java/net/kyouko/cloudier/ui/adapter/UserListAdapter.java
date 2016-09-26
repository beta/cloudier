package net.kyouko.cloudier.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import net.kyouko.cloudier.CloudierApplication;
import net.kyouko.cloudier.R;
import net.kyouko.cloudier.event.LoadMoreUsersEvent;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.util.ImageUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class for {@link RecyclerView} to display a list of tweets.
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.BaseViewHolder> {

    private final static int ITEM_TYPE_USER = 0;
    private final static int ITEM_TYPE_LOAD_MORE = 99;


    private Context context;
    private List<User> users;

    private int userType = User.TYPE_FOLLOWER;

    private LoadMoreViewHolder loadMoreViewHolder;
    private int shortAnimationDuration;


    public UserListAdapter(Context context, List<User> users, int userType) {
        this.context = context;
        this.users = users;
        this.userType = userType;

        shortAnimationDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.template_user_card, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == ITEM_TYPE_LOAD_MORE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.template_load_more, parent, false);
            return new LoadMoreViewHolder(view);
        }

        return null;
    }


    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ITEM_TYPE_LOAD_MORE:
                bindLoadMoreViewHolder((LoadMoreViewHolder) holder);
                break;
            case ITEM_TYPE_USER:
            default:
                User user = users.get(position);
                bindUserViewHolder((UserViewHolder) holder, user);
                break;
        }
    }


    private void bindUserViewHolder(UserViewHolder holder, User user) {
        holder.avatar.setImageURI(
                Uri.parse(ImageUtil.getInstance(context).parseImageUrl(user.avatarUrl)));
        holder.nickname.setText(user.nickname);
        holder.username.setText(user.username);
        holder.introduction.setText((user.introduction == null) ? "" : user.introduction);
    }


    private void bindLoadMoreViewHolder(final LoadMoreViewHolder holder) {
        loadMoreViewHolder = holder;

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMore(holder);
            }
        });
    }


    private void loadMore(final LoadMoreViewHolder holder) {
        holder.button.setClickable(false);

        holder.progress.setAlpha(0f);
        holder.progress.setVisibility(View.VISIBLE);

        holder.progress.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);

        holder.button.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        holder.button.setVisibility(View.GONE);
                        holder.button.setClickable(true);

                        CloudierApplication.getBus().post(new LoadMoreUsersEvent(userType));
                    }
                });
    }


    public void completeLoadingMore() {
        if (loadMoreViewHolder != null) {
            loadMoreViewHolder.progress.setVisibility(View.GONE);

            loadMoreViewHolder.button.setAlpha(1f);
            loadMoreViewHolder.button.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemViewType(int position) {
        boolean showLoadMore = !users.isEmpty();

        if (showLoadMore && position == (getItemCount() - 1)) {
            return ITEM_TYPE_LOAD_MORE;
        } else {
            return ITEM_TYPE_USER;
        }
    }


    @Override
    public int getItemCount() {
        return (users.size() + (!users.isEmpty() ? 1 : 0));
    }


    abstract class BaseViewHolder extends RecyclerView.ViewHolder {

        BaseViewHolder(View itemView) {
            super(itemView);
        }

    }


    class UserViewHolder extends BaseViewHolder {

        @BindView(R.id.card) CardView card;
        @BindView(R.id.wrapper) View wrapper;
        @BindView(R.id.avatar) SimpleDraweeView avatar;
        @BindView(R.id.nickname) TextView nickname;
        @BindView(R.id.username) TextView username;
        @BindView(R.id.introduction) TextView introduction;


        UserViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

    }


    class LoadMoreViewHolder extends BaseViewHolder {

        @BindView(R.id.button) View button;
        @BindView(R.id.progress) ProgressBar progress;


        LoadMoreViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

    }

}
