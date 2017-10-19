package io.rolique.roliqueapp.screens.chat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.Media;
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

    public interface OnMessageActionListener {
        void onMessageEdit(Message message);

        void onMessageRemove(Message message);
    }

    private final LayoutInflater mInflater;
    private List<Message> mMessages;
    private List<User> mUsers;
    private Pair<List<Boolean>, List<Integer>> mIsItemMessage;
    private String mCurrentUserId;
    private OnMessageActionListener mActionListener;

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

    public void setActionListener(OnMessageActionListener actionListener) {
        mActionListener = actionListener;
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
        } else notifyItemInserted(mIsItemMessage.first.size());
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

    public void updateMessage(Message message) {
        for (int i = 0; i < mMessages.size(); i++)
            if (mMessages.get(i).getId().equals(message.getId())) {
                mMessages.set(i, message);
                for (int j = 0; j < mIsItemMessage.second.size(); j++)
                    if (mIsItemMessage.second.get(j) == i) {
                        notifyItemChanged(j);
                        break;
                    }
                break;
            }
    }

    public void removeMessage(String messageId) {
        for (int i = 0; i < mMessages.size(); i++)
            if (mMessages.get(i).getId().equals(messageId)) {
                mMessages.remove(i);
                setMessages(new ArrayList<>(mMessages));
            }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_view_date) TextView mDateTextView;
        @BindView(R.id.text_view_user_name) TextView mUserNameTextView;
        @BindView(R.id.layout_message_container) LinearLayout mMessageContainerLayout;
        @BindView(R.id.layout_message_info) LinearLayout mMessageInfoContainerLayout;
        @BindView(R.id.layout_sender_image) FrameLayout mSenderImageLayout;
        @BindView(R.id.view_switcher) ViewSwitcher mViewSwitcher;
        @BindView(R.id.text_view_message) TextView mMessageTextView;
        @BindView(R.id.container_images) LinearLayout mImageMessagesLayout;
        @BindView(R.id.image_view_other_edited) ImageView mOtherEditedIndicatorImageView;
        @BindView(R.id.image_view_own_edited) ImageView mOwnEditedIndicatorImageView;

        Message mMessage;

        MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(MessageViewHolder.this, itemView);
        }

        void bindMessage(Message message, int messagePosition) {
            mMessage = message;
            itemView.setAlpha(1.0f);
            boolean isCurrentUser = mMessage.getSenderId().equals(mCurrentUserId);
            setUpHeader(isCurrentUser);
            setUpImageView(messagePosition, isCurrentUser);
            setUpMessageInView(messagePosition, isCurrentUser);
            setEditEvent(isCurrentUser);
            setUpEdited(isCurrentUser);
        }

        private void setUpHeader(boolean isCurrentUser) {
            mDateTextView.setText(DateUtil.getDetailedStringMessageDate(mMessage.getTimeStamp()));
            mUserNameTextView.setText(UiUtil.getUserNameForView(mMessage.getSenderId(), mUsers));
            mUserNameTextView.setVisibility(isCurrentUser ? View.GONE : View.VISIBLE);
            mMessageContainerLayout.setGravity(isCurrentUser ? Gravity.END : Gravity.START);
            mMessageInfoContainerLayout.setVisibility(View.GONE);
            mMessageContainerLayout.setOnClickListener(mOnClickListener);
        }

        final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageInfoContainerLayout.setVisibility(mMessageInfoContainerLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        };

        private void setUpImageView(int messagePosition, boolean isCurrentUser) {
            if (isCurrentUser) {
                mSenderImageLayout.setVisibility(View.GONE);
                return;
            }
            if (messagePosition == BOTTOM_MESSAGE || messagePosition == SINGLE_MESSAGE) {
                mSenderImageLayout.setVisibility(View.VISIBLE);
                UiUtil.setImageIfExists(mViewSwitcher, getUserImageUrl(mMessage.getSenderId(), mUsers), getUserName(mMessage.getSenderId(), mUsers), 40);
            } else {
                mSenderImageLayout.setVisibility(View.INVISIBLE);
            }
        }

        private String getUserImageUrl(String senderId, List<User> users) {
            for (User user : users)
                if (user.getId().equals(senderId)) return user.getImageUrl();
            return "";
        }

        private String getUserName(String senderId, List<User> users) {
            for (User user : users)
                if (user.getId().equals(senderId))
                    return String.format("%s %s", user.getFirstName(), user.getLastName());
            return "";
        }

        private void setUpMessageInView(int messagePosition, boolean isCurrentUser) {
            if (mMessage.isMedia()) {
                if (mMessage.getText().isEmpty()) {
                    mMessageTextView.setVisibility(View.GONE);
                } else {
                    if (messagePosition == SINGLE_MESSAGE)
                        messagePosition = TOP_MESSAGE;
                    @DrawableRes int background = getBackgroundDrawable(messagePosition, isCurrentUser);
                    mMessageTextView.setBackground(ContextCompat.getDrawable(mMessageTextView.getContext(), background));
                    mMessageTextView.setText(mMessage.getText());
                }
                mImageMessagesLayout.setVisibility(View.VISIBLE);
                mImageMessagesLayout.removeAllViews();
                for (Media media : mMessage.getMedias()) {
                    View layout = createImageLayout(media, isCurrentUser);
                    mImageMessagesLayout.addView(layout);
                }
            } else {
                mMessageTextView.setVisibility(View.VISIBLE);
                mImageMessagesLayout.setVisibility(View.GONE);
                @DrawableRes int background = getBackgroundDrawable(messagePosition, isCurrentUser);
                mMessageTextView.setBackground(ContextCompat.getDrawable(mMessageTextView.getContext(), background));
                mMessageTextView.setText(mMessage.getText());
            }
            mMessageContainerLayout.setLayoutParams(getMessageParameters(messagePosition, isCurrentUser));
        }

        private View createImageLayout(Media media, boolean isCurrentUser) {
            int height = media.getHeight();
            int width = media.getWidth();
            int baseDimen = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.message_image_view_base_width);
            int imageViewHeight = height >= width ? baseDimen * height / width : baseDimen;
            int imageViewWidth = height >= width ? baseDimen : baseDimen * width / height;

            final View imageLayout = mInflater.inflate(R.layout.content_message_image, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imageViewWidth, imageViewHeight);
            params.gravity = isCurrentUser ? Gravity.END : Gravity.START;
            imageLayout.setLayoutParams(params);

            ImageView playIcon = imageLayout.findViewById(R.id.image_view_play_icon);
            playIcon.setVisibility(media.isVideoType() &&
                    media.getImageUrl().startsWith("http") &&
                    media.getVideoUrl().startsWith("http") ? View.VISIBLE : View.GONE);

            ProgressBar progressBar = imageLayout.findViewById(R.id.progress_bar_media);
            progressBar.setVisibility((media.isVideoType() &&
                    media.getImageUrl().startsWith("http") &&
                    media.getVideoUrl().startsWith("http")) ||
                    (!media.isVideoType() &&
                    media.getImageUrl().startsWith("http"))
                    ? View.GONE : View.VISIBLE);

            ImageView imageView = imageLayout.findViewById(R.id.image_view_message_media);
            UiUtil.setImageWithRoundCorners(imageView, media.getImageUrl());
            return imageLayout;
        }

        @NonNull
        private LinearLayout.LayoutParams getMessageParameters(int messagePosition, boolean isCurrentUser) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mMessageContainerLayout.getLayoutParams();
            mMessageInfoContainerLayout.setGravity(isCurrentUser ? Gravity.END : Gravity.START);
            mMessageContainerLayout.setGravity(isCurrentUser ? Gravity.END : Gravity.START);
            params.bottomMargin = 0;
            params.topMargin = messagePosition == TOP_MESSAGE || messagePosition == SINGLE_MESSAGE ? 20 : 0;
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

        private void setEditEvent(boolean isCurrentUser) {
            if (!isCurrentUser) {
                mMessageContainerLayout.setOnLongClickListener(null);
                return;
            }
            mMessageContainerLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mMessageContainerLayout.setAlpha(0.6f);

                    final View popupView = mInflater.inflate(R.layout.content_message_popup, null);
                    final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.shape_text_view_message_alien_single));
                    popupWindow.setOutsideTouchable(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        popupWindow.setAttachedInDecor(true);
                    }
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popupWindow.setAnimationStyle(R.style.popupAnimation);
                    View editView = popupView.findViewById(R.id.image_view_edit);
                    editView.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            if (mActionListener == null) return;
                            mActionListener.onMessageEdit(mMessage);
                        }
                    });
                    View removeView = popupView.findViewById(R.id.image_view_remove);
                    removeView.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            if (mActionListener == null) return;
                            mActionListener.onMessageRemove(mMessage);
                        }
                    });
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            mMessageContainerLayout.setAlpha(1.0f);
                        }
                    });
                    int yOffset = (-1) * itemView.getMeasuredHeight() +
                            (mUserNameTextView.getVisibility() == View.VISIBLE ?
                                    mUserNameTextView.getHeight() : 0) +
                            (mUserNameTextView.getVisibility() == View.VISIBLE ?
                                    mDateTextView.getHeight() : 0) -
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, itemView.getContext().getResources().getDisplayMetrics());
                    popupWindow.showAsDropDown(itemView, 0, yOffset, Gravity.END);
                    return true;
                }
            });
        }

        private void setUpEdited(boolean isCurrentUser) {
            mOtherEditedIndicatorImageView.setVisibility(!isCurrentUser && mMessage.isEdited() ? View.VISIBLE : View.GONE);
            mOwnEditedIndicatorImageView.setVisibility(isCurrentUser && mMessage.isEdited() ? View.VISIBLE : View.GONE);
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
