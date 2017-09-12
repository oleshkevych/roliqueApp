package io.rolique.roliqueapp.screens.navigation.contacts.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final LayoutInflater mInflater;
    private List<User> mUsers;

    public interface OnItemClickListener {
        void onUserClicked(User user);
    }

    private OnItemClickListener mOnItemClickListener;

    public UsersAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mUsers = new ArrayList<>();
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

    public void setUsers(List<User> users) {
        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_view) ImageView mImageView;
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
                mOnItemClickListener.onUserClicked(mUser);
            }
        };

        void bindChat(User user) {
            mUser = user;
            mNameTextView.setText(UiUtil.getUserNameForView(mUser));
            UiUtil.setImage(mImageView, mUser.getImageUrl());
        }
    }
}
