package io.rolique.roliqueapp.screens.navigation.chats;

import android.content.Context;
import android.graphics.Typeface;
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
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.util.DateUtil;
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
        for (int i = 0; i < mChats.size(); i++)
            if (mChats.get(i).getId().equals(chat.getId())) {
                mChats.set(i, chat);
                notifyItemChanged(i);
                return;
            }
        for (int i = 0; i < mChats.size(); i++) {
            if (isEarlier(mChats.get(i), chat)) {
                mChats.add(i, chat);
                notifyItemInserted(i);
                return;
            }
        }
        mChats.add(chat);
        notifyItemInserted(mChats.size() - 1);
    }

    private boolean isEarlier(Chat o1, Chat o2) {
        return DateUtil.transformDate(o1.getLastMessage().getTimeStamp()).getTime() <
                DateUtil.transformDate(o2.getLastMessage().getTimeStamp()).getTime();
    }

    void changeChat(Chat chat) {
        for (int i = 0; i < mChats.size(); i++)
            if (chat.getId().equals(mChats.get(i).getId())) {
                mChats.remove(i);
//                notifyItemRemoved(i);
                mChats.add(0, chat);
                notifyItemMoved(i, 0);
                notifyItemChanged(0);
                break;
            }
    }

    void removeChat(String chatId) {
        for (int i = 0; i < mChats.size(); i++)
            if (chatId.equals(mChats.get(i).getId())) {
                mChats.remove(i);
                notifyItemRemoved(i);
                break;
            }
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.view_switcher) ViewSwitcher mViewSwitcher;
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
            setTextViewBold(mTitleTextView);
            mTitleTextView.setTextColor(ContextCompat.getColor(mTitleTextView.getContext(),
                    mChat.isHasNewMessages() ? R.color.amber_500 : R.color.black_alpha_50));
            if (mChat.getLastMessage().getSenderId() == null || mChat.getLastMessage().getSenderId().isEmpty()) {
                mLastMessageTextView.setText("");
                mLastMessageTextView.setVisibility(View.GONE);
            } else {
                mLastMessageTextView.setText(String.format("%s - %s",
                        UiUtil.getUserNameForView(mChat.getLastMessage().getSenderId(), mRoliqueAppUsers.getUsers()),
                        mChat.getLastMessage().getText().isEmpty() ?
                                mChat.getLastMessage().getMedias() == null || mChat.getLastMessage().getMedias().size() == 0 ?
                                        "" : mChat.getLastMessage().getMedias().get(0).getMediaType() :
                                mChat.getLastMessage().getText()
                ));
                mLastMessageTextView.setVisibility(View.VISIBLE);
            }
            setTextViewBold(mLastMessageTextView);
            mDateTextView.setText(DateUtil.getStringMessageDate(mChat.getLastMessage().getTimeStamp()));
            setTextViewBold(mDateTextView);
            UiUtil.setImageIfExists(mViewSwitcher, mChat.getImageUrl(), mChat.getTitle(), 64);
        }

        private void setTextViewBold(TextView textView) {
            textView.setTypeface(null, mChat.isHasNewMessages() ? Typeface.BOLD : Typeface.NORMAL);
        }
    }
}
