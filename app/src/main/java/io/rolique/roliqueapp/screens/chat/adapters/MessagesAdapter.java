package io.rolique.roliqueapp.screens.chat.adapters;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 9/1/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TOP_MESSAGE = 1;
    private static final int CENTER_MESSAGE = 2;
    private static final int BOTTOM_MESSAGE = 3;
    private static final int SINGLE_MESSAGE = 4;

    private final LayoutInflater mInflater;
    private List<Message> mMessages;
    private List<User> mUsers;
    private Pair<List<Boolean>, List<Integer>> mIsItemMessage;
    private String mCurrentUserId;

    public MessagesAdapter(Context context, String currentUserId, List<User> users) {
        mInflater = LayoutInflater.from(context);
        mMessages = new ArrayList<>();
        mIsItemMessage = new Pair<>((List<Boolean>) new ArrayList<Boolean>(), (List<Integer>) new ArrayList<Integer>());
        mCurrentUserId = currentUserId;
        mUsers = users;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(viewType, parent, false);

        switch (viewType) {
            case R.layout.item_message:
                return new MessageViewHolder(v);
            case R.layout.item_date:
                return new DateViewHolder(v);
            default:
                return new MessageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case R.layout.item_message:
                ((MessageViewHolder) holder).bindMessage(mMessages.get(mIsItemMessage.second.get(position)), calculateMessagePosition(position));
                break;
            case R.layout.item_date:
                ((DateViewHolder) holder).bindDateByLastMessage(mMessages.get(mIsItemMessage.second.get(position + 1)));
                break;
        }
    }

    private int calculateMessagePosition(int position) {
        if (mIsItemMessage.first.size() > 2 && position > 0) {
            if (mIsItemMessage.first.size() - 1 == position) {
                if (mIsItemMessage.first.get(position - 1)) {
                    if (mMessages.get(mIsItemMessage.second.get(position - 1)).getSenderId().equals(mMessages.get(mIsItemMessage.second.get(position)).getSenderId())) {
                        return BOTTOM_MESSAGE;
                    } else {
                        return SINGLE_MESSAGE;
                    }
                }
                return SINGLE_MESSAGE;
            }
            if (mIsItemMessage.first.get(position - 1) && mIsItemMessage.first.get(position + 1)) {
                if (mMessages.get(mIsItemMessage.second.get(position - 1)).getSenderId().equals(mMessages.get(mIsItemMessage.second.get(position)).getSenderId())) {
                    if (mMessages.get(mIsItemMessage.second.get(position + 1)).getSenderId().equals(mMessages.get(mIsItemMessage.second.get(position)).getSenderId())) {
                        return CENTER_MESSAGE;
                    } else {
                        return BOTTOM_MESSAGE;
                    }
                } else if (mMessages.get(mIsItemMessage.second.get(position + 1)).getSenderId().equals(mMessages.get(mIsItemMessage.second.get(position)).getSenderId())) {
                    return TOP_MESSAGE;
                } else {
                    return SINGLE_MESSAGE;
                }
            }
            if (mIsItemMessage.first.get(position - 1) && !mIsItemMessage.first.get(position + 1)) {
                if (mMessages.get(mIsItemMessage.second.get(position - 1)).getSenderId().equals(mMessages.get(mIsItemMessage.second.get(position)).getSenderId())) {
                    return BOTTOM_MESSAGE;
                } else {
                    return SINGLE_MESSAGE;
                }
            }
            if (!mIsItemMessage.first.get(position - 1) && mIsItemMessage.first.get(position + 1)) {
                if (mMessages.get(mIsItemMessage.second.get(position + 1)).getSenderId().equals(mMessages.get(mIsItemMessage.second.get(position)).getSenderId())) {
                    return TOP_MESSAGE;
                } else {
                    return SINGLE_MESSAGE;
                }
            }
        } else {
            return SINGLE_MESSAGE;
        }
        return SINGLE_MESSAGE;
    }

    @Override
    public int getItemViewType(int position) {
        if (mIsItemMessage.first.get(position)) return R.layout.item_message;
        else return R.layout.item_date;
    }

    @Override
    public int getItemCount() {
        return mIsItemMessage.first.size();
    }

    public void setMessages(List<Message> messages) {
        if (messages.isEmpty()) return;
        mIsItemMessage = calculateItemsCount(messages);
        mIsItemMessage.first.add(0, false);
        mIsItemMessage.second.add(0, 0);
        mMessages.clear();
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }

    public void addTopMessages(List<Message> messages) {
        if (messages.isEmpty()) return;
        Pair<List<Boolean>, List<Integer>> isNewItemsMessages = calculateItemsCount(messages);
        if (DateUtil.isSameDay(messages.get(messages.size() - 1).getTimeStamp(), mMessages.get(0).getTimeStamp())) {
            mIsItemMessage.first.remove(0);
            mIsItemMessage.second.remove(0);
            notifyItemRemoved(0);
        }
        for (int i = 0; i < mIsItemMessage.second.size(); i++)
            mIsItemMessage.second.set(i, mIsItemMessage.second.get(i) + messages.size());
        mIsItemMessage.first.addAll(0, isNewItemsMessages.first);
        mIsItemMessage.second.addAll(0, isNewItemsMessages.second);
        mMessages.addAll(0, messages);
        notifyItemRangeInserted(0, isNewItemsMessages.first.size());
        notifyItemRangeChanged(isNewItemsMessages.first.size() - 1, isNewItemsMessages.first.size() + 1);
    }

    public void addNewMessage(Message message) {
        if (mMessages.isEmpty()) {
            mIsItemMessage.first.add(false);
            mIsItemMessage.second.add(0);
        } else {
            if (!DateUtil.isSameDay(message.getTimeStamp(), mMessages.get(mMessages.size() - 1).getTimeStamp())) {
                mIsItemMessage.first.add(false);
                mIsItemMessage.second.add(0);
                notifyItemInserted(mIsItemMessage.first.size());
            }
        }
        mMessages.add(message);
        mIsItemMessage.first.add(true);
        mIsItemMessage.second.add(mMessages.size() - 1);
        if (mIsItemMessage.first.size() > 2) {
            notifyItemInserted(mIsItemMessage.first.size());
            notifyItemRangeChanged(mIsItemMessage.first.size() - 2, mIsItemMessage.first.size());
        }
        else notifyItemInserted(mIsItemMessage.first.size());
    }

    private Pair<List<Boolean>, List<Integer>> calculateItemsCount(List<Message> messages) {
        List<Boolean> isNewItemsMessages = new ArrayList<>();
        List<Integer> messageIndexes = new ArrayList<>();
        Message previousMessage = messages.get(0);
        isNewItemsMessages.add(false);
        messageIndexes.add(0);
        isNewItemsMessages.add(true);
        messageIndexes.add(0);
        for (int i = 1; i < messages.size(); i++) {
            if (!DateUtil.isSameDay(previousMessage.getTimeStamp(), messages.get(i).getTimeStamp())) {
                isNewItemsMessages.add(false);
                messageIndexes.add(0);
            }
            previousMessage = messages.get(i);
            isNewItemsMessages.add(true);
            messageIndexes.add(i);
        }
        return new Pair<>(isNewItemsMessages, messageIndexes);
    }

    public String getFirstMessageId() {
        return mMessages.isEmpty() ? "" : mMessages.get(0).getId();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_view_date) TextView mDateTextView;
        @BindView(R.id.text_view_user_name) TextView mUserNameTextView;
        @BindView(R.id.layout_message_container) LinearLayout mMessageContainerLayout;
        @BindView(R.id.image_view_user_image) ImageView mSenderImageView;
        @BindView(R.id.text_view_message) TextView mMessageTextView;

        Message mMessage;

        MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(MessageViewHolder.this, itemView);
            itemView.setOnClickListener(mOnClickListener);
        }

        final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDateTextView.setVisibility(mDateTextView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                mUserNameTextView.setVisibility(mUserNameTextView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        };

        void bindMessage(Message message, int messagePosition) {
            mMessage = message;
            boolean isCurrentUser = mMessage.getSenderId().equals(mCurrentUserId);
            setUpHeader(isCurrentUser);
            setUpImageView(messagePosition, isCurrentUser);
            setUpMessageInView(messagePosition, isCurrentUser);
        }

        private void setUpHeader(boolean isCurrentUser) {
            mDateTextView.setText(DateUtil.getDetailedStringMessageDate(mMessage.getTimeStamp()));
            mDateTextView.setVisibility(View.GONE);
            mUserNameTextView.setText(UiUtil.getUserNameForView(mMessage.getSenderId(), mUsers));
            mUserNameTextView.setVisibility(View.GONE);
            mUserNameTextView.setGravity(isCurrentUser ? Gravity.END : Gravity.START);
        }

        private void setUpImageView(int messagePosition, boolean isCurrentUser) {
            if (isCurrentUser) {
                mSenderImageView.setVisibility(View.GONE);
                return;
            }
            if (messagePosition == BOTTOM_MESSAGE || messagePosition == SINGLE_MESSAGE) {
                mSenderImageView.setVisibility(View.VISIBLE);
                UiUtil.setImage(mSenderImageView, getImageUrl(mMessage.getSenderId(), mUsers));
            } else {
                mSenderImageView.setVisibility(View.INVISIBLE);
            }
        }

        private String getImageUrl(String senderId, List<User> users) {
            for (User user : users)
                if (user.getId().equals(senderId)) return user.getImageUrl();
            return "";
        }

        private void setUpMessageInView(int messagePosition, boolean isCurrentUser) {
            @DrawableRes int background = getBackgroundDrawable(messagePosition, isCurrentUser);
            mMessageContainerLayout.setLayoutParams(getMessageParameters(messagePosition, isCurrentUser));
            mMessageTextView.setBackground(ContextCompat.getDrawable(mMessageTextView.getContext(), background));
            mMessageTextView.setText(mMessage.getText());
        }

        @NonNull
        private LinearLayout.LayoutParams getMessageParameters(int messagePosition, boolean isCurrentUser) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mMessageContainerLayout.getLayoutParams();
            params.gravity = isCurrentUser ? Gravity.END : Gravity.START;
            params.bottomMargin = 0;
            params.topMargin = messagePosition == TOP_MESSAGE ||messagePosition == SINGLE_MESSAGE ? 20 : 0;
            params.bottomMargin = (messagePosition == BOTTOM_MESSAGE || messagePosition == SINGLE_MESSAGE) ? 20 : 4;
            return params;
        }

        @DrawableRes
        private int getBackgroundDrawable(int messagePosition, boolean isCurrentUser) {
            if (isCurrentUser)
                switch (messagePosition) {
                    case TOP_MESSAGE:
                        return R.drawable.shape_text_view_message_user_top;
                    case CENTER_MESSAGE:
                        return R.drawable.shape_text_view_message_user_center;
                    case BOTTOM_MESSAGE:
                        return R.drawable.shape_text_view_message_user_bottom;
                    case SINGLE_MESSAGE:
                        return R.drawable.shape_text_view_message_user_single;
                }
                else
                switch (messagePosition) {
                    case TOP_MESSAGE:
                        return R.drawable.shape_text_view_message_alien_top;
                    case CENTER_MESSAGE:
                        return R.drawable.shape_text_view_message_alien_center;
                    case BOTTOM_MESSAGE:
                        return R.drawable.shape_text_view_message_alien_bottom;
                    case SINGLE_MESSAGE:
                        return R.drawable.shape_text_view_message_alien_single;
                }
            return 0;
        }
    }

    class DateViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_view_date) TextView mTextView;

        DateViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(DateViewHolder.this, itemView);
        }

        void bindDateByLastMessage(Message message) {
            mTextView.setText(DateUtil.getStringMessageDate(message.getTimeStamp()));
        }
    }
}
