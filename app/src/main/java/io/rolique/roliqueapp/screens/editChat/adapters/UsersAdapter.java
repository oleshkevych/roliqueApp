package io.rolique.roliqueapp.screens.editChat.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 9/1/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final LayoutInflater mInflater;
    private List<User> mUsers;
    private List<String> mSelectedUserIds;
    private final boolean mIsView;

    public interface OnItemClickListener {
        void onUserClick(User user);
        void onUserSelected(User user);
        void onUserUnselected(User user);
    }

    private OnItemClickListener mOnItemClickListener;

    public UsersAdapter(Context context, List<User> users, boolean isView) {
        mInflater = LayoutInflater.from(context);
        mUsers = users;
        mSelectedUserIds = new ArrayList<>();
        mIsView = isView;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        holder.bindChat(mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public List<String> getSelectedUserIds() {
        return mSelectedUserIds;
    }

    public void setMemberIds(List<String> ids) {
        mSelectedUserIds.addAll(ids);
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.view_switcher) ViewSwitcher mViewSwitcher;
        @BindView(R.id.text_view_user_name) TextView mNameTextView;

        User mUser;

        UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(UserViewHolder.this, itemView);
            itemView.setOnClickListener(mOnClickListener);
        }

        final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsView) {
                    mOnItemClickListener.onUserClick(mUser);
                    return;
                }
                if (mSelectedUserIds.contains(mUser.getId())) {
                    mOnItemClickListener.onUserUnselected(mUser);
                    mSelectedUserIds.remove(mUser.getId());
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
                } else {
                    mOnItemClickListener.onUserSelected(mUser);
                    mSelectedUserIds.add(mUser.getId());
                    itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.black_alpha_80));
                }
            }
        };

        void bindChat(User user) {
            mUser = user;
            mNameTextView.setText(UiUtil.getUserNameForView(mUser));
            UiUtil.setImageIfExists(mViewSwitcher, mUser.getImageUrl(), UiUtil.getUserNameForView(mUser), 60);
            itemView.setBackgroundColor(ContextCompat
                    .getColor(itemView.getContext(), mSelectedUserIds.contains(mUser.getId()) ? R.color.black_alpha_80 : R.color.white));
        }
    }
}
