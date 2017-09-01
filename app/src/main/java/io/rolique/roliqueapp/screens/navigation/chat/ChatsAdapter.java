package io.rolique.roliqueapp.screens.navigation.chat;

import android.content.Context;
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
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 8/28/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    private final LayoutInflater mInflater;

    private List<Chat> mChats;
    private RoliqueAppUsers mRoliqueAppUsers;

    public interface OnItemClickListener {
        void onItemClick(Chat chat);
    }

    private OnItemClickListener mOnItemClickListener;

    ChatsAdapter(Context context, RoliqueAppUsers roliqueAppUsers) {
        mInflater = LayoutInflater.from(context);
        mRoliqueAppUsers = roliqueAppUsers;
        mChats = new ArrayList<>();
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        holder.bindChat(mChats.get(position));
    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    void setChats(List<Chat> chats) {
        mChats.clear();
        mChats.addAll(0, chats);
        notifyDataSetChanged();
    }

    void cleanChats() {
        mChats.clear();
        notifyDataSetChanged();
    }

    void addChat(Chat chat) {
        mChats.add(chat);
        notifyItemInserted(mChats.size() - 1);
    }


    void changeChat(Chat chat) {
        for (int i = 0; i < mChats.size(); i++)
            if (chat.getId().equals(mChats.get(i).getId())) {
                mChats.set(i, chat);
                notifyItemChanged(i);
                break;
            }
    }

    void removeChat(Chat chat) {
        for (int i = 0; i < mChats.size(); i++)
            if (chat.getId().equals(mChats.get(i).getId())) {
                mChats.remove(i);
                notifyItemRemoved(i);
                break;
            }
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_view) ImageView mImageView;
        @BindView(R.id.text_view_title) TextView mTitleTextView;
        @BindView(R.id.text_view_last_message) TextView mLastMessageTextView;
        @BindView(R.id.text_view_date) TextView mDateTextView;

        Chat mChat;

        ChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(ChatViewHolder.this, itemView);
            itemView.setOnClickListener(mOnClickListener);
        }

        final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(mChat);
            }
        };

        void bindChat(Chat chat) {
            mChat = chat;
            mTitleTextView.setText(mChat.getTitle());
            mLastMessageTextView.setText(String.format("%s - %s", UiUtil.getUserNameForView(mChat.getSenderId(), mRoliqueAppUsers.getUsers()), mChat.getLastMessage()));
            mDateTextView.setText(UiUtil.getStringTimeForView(mChat.getTimeStamp()));
            UiUtil.setImage(mImageView, mChat.getImageUrl());
        }
    }
}
