package io.rolique.roliqueapp.screens.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.screens.videoViewer.VideoViewerActivity;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.chat.adapters.MessagesAdapter;
import io.rolique.roliqueapp.screens.chat.adapters.PreviewAdapter;
import io.rolique.roliqueapp.screens.chat.decorators.CustomInterpolator;
import io.rolique.roliqueapp.screens.editChat.ChatEditorActivity;
import io.rolique.roliqueapp.screens.imageViewer.ImageViewerActivity;
import io.rolique.roliqueapp.screens.profile.ProfileActivity;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.ui.UiUtil;
import timber.log.Timber;

public class ChatActivity extends BaseActivity implements ChatContract.View {

    public static String EXTRA_CHAT = "CHAT";
    public static int RC_CHAT_EDIT = 101;

    public static Intent startIntent(Context context, Chat chat) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT, chat);
        return intent;
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.button_add_image) ImageButton mAddImageButton;
    @BindView(R.id.container_media_buttons) LinearLayout mMediaButtonsLayout;
    @BindView(R.id.recycler_view_image_preview) RecyclerView mPreviewRecyclerView;
    @BindView(R.id.edit_text_message) EditText mMessageEditText;
    @BindView(R.id.button_cancel_edit) ImageButton mCancelEditButton;
    @BindView(R.id.button_send) ImageButton mSendButton;

    @Inject ChatPresenter mPresenter;
    @Inject RoliqueAppUsers mRoliqueAppUsers;
    @Inject RoliqueApplicationPreferences mPreferences;

    MessagesAdapter mAdapter;
    PreviewAdapter mPreviewAdapter;
    Chat mChat;
    MediaLib mMediaLib;
    Message mChangeableMessage;
    boolean mIsEditing;
    boolean mIsFetchMessageEnabled = true;
    boolean mIsKeyboardShown;
    float mStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChat = getIntent().getParcelableExtra(EXTRA_CHAT);
        setUpToolbar(mChat);
        setUpMessageEditText();
        setUpMediaLib();
        setUpRecyclerView();

        mPresenter.fetchLastMessages(mChat);
    }

    @Override
    protected void inject() {
        DaggerChatComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .chatPresenterModule(new ChatPresenterModule(ChatActivity.this))
                .build()
                .inject(ChatActivity.this);
    }

    private void setUpToolbar(Chat chat) {
        mToolbar.setTitle(chat.getTitle());
        ImageButton imageButton = findViewById(R.id.button_edit);
        if (chat.getId().equals("main")) imageButton.setVisibility(View.GONE);
        else if (chat.isSingle()) imageButton.setImageResource(R.drawable.ic_person_white_24dp);
        else
            imageButton.setImageResource(chat.getOwnerId().equals(mPreferences.getId()) ? R.drawable.ic_edit_white_24dp : R.drawable.ic_move_out_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpMessageEditText() {
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mMediaButtonsLayout.setVisibility(View.GONE);
                mAddImageButton.setVisibility(View.VISIBLE);
                mIsKeyboardShown = true;
                if (s.length() == 0 &&
                        mPreviewRecyclerView.getVisibility() != View.VISIBLE) {
                    setEnableSend(false);
                } else {
                    setEnableSend(true);
                }
            }
        });
        setEnableSend(false);
    }

    private void setUpMediaLib() {
        mMediaLib = new MediaLib(ChatActivity.this, new MediaLib.MediaLibListener() {
            @Override
            public void onSuccess(List<MediaContent> mediaContents) {
                Timber.d(mediaContents.toString());
                mMediaButtonsLayout.setVisibility(View.GONE);
                mAddImageButton.setVisibility(View.VISIBLE);
                List<Media> messageMedias = new ArrayList<>();
                for (MediaContent mediaContent : mediaContents) {
                    Media media = new Media
                            .Builder()
                            .setHeight(mediaContent.getHeight())
                            .setWidth(mediaContent.getWidth())
                            .setImageUrl(mediaContent.getImage())
                            .setVideoUrl(mediaContent.isVideo() ? mediaContent.getVideo() : null)
                            .setMediaType(mediaContent.isImage() ? Media.CATEGORY_IMAGE : Media.CATEGORY_VIDEO)
                            .create();
                    media.setImageUrl(UiUtil.resizeImage(ChatActivity.this, media.getImageUrl(), media.getWidth(), media.getHeight()));
                    messageMedias.add(media);
                }
                mChangeableMessage = getMediaMessage(mMessageEditText.getText().toString(), messageMedias);
                mIsEditing = true;
                setEnableSend(true);
                showPreview(messageMedias);
            }

            @Override
            public void onEmpty() {
                mMediaButtonsLayout.setVisibility(View.GONE);
                mAddImageButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                mMediaButtonsLayout.setVisibility(View.GONE);
                mAddImageButton.setVisibility(View.VISIBLE);
            }
        });
        mMediaLib.setStorage(MediaLib.GLOBAL_MEDIA_DEFAULT_FOLDER);
        mMediaLib.setSelectableFlash(true);
        mMediaLib.setRotation(true);
        mMediaLib.setFrontCamera(true);
        mMediaLib.setRecordVideo(true);
    }

    private Message getMediaMessage(String messageText, List<Media> medias) {
        Message.Builder builder = new Message.Builder();
        return builder.setChatId(mChat.getId())
                .setSenderId(mPreferences.getId())
                .setText(messageText)
                .setType("user")
                .setTimeStamp(DateUtil.getStringTime())
                .setMedias(medias)
                .create();
    }

    private void showPreview(List<Media> messageMedias) {
        mPreviewRecyclerView.setVisibility(View.VISIBLE);
        mPreviewAdapter.setMedias(messageMedias);
    }

    private void setUpRecyclerView() {
        final LinearLayoutManager previewLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        previewLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPreviewRecyclerView.setLayoutManager(previewLinearLayoutManager);
        mPreviewAdapter = new PreviewAdapter(ChatActivity.this);
        mPreviewRecyclerView.setAdapter(mPreviewAdapter);
        mPreviewAdapter.setOnItemClickListener(new PreviewAdapter.OnItemClickListener() {
            @Override
            public void onImageClick(int position) {
                if (mChangeableMessage.getMedias().get(position).isVideo()) {
                    startActivity(VideoViewerActivity.getStartIntent(ChatActivity.this,
                            mChangeableMessage.getMedias().get(position).getVideoUrl()));
                } else {
                    startActivity(ImageViewerActivity.getStartIntent(ChatActivity.this, mChangeableMessage.getMedias(), position));
                }
            }

            @Override
            public void onRemoveClick(int position) {
//                new File(mChangeableMessage.getMedias().get(position).getImageUrl()).delete();
//                if (mChangeableMessage.getMedias().get(position).isVideo())
//                    new File(mChangeableMessage.getMedias().get(position).getVideoUrl()).delete();
                mChangeableMessage.getMedias().remove(position);
                mPreviewAdapter.removeItem(position);
                if (mChangeableMessage.getMedias().size() == 0) resetPreview();
            }
        });

        final RecyclerView messagesRecyclerView = getViewById(R.id.recycler_view_messages);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MessagesAdapter(ChatActivity.this, mPreferences.getId(), mRoliqueAppUsers.getUsers());
        messagesRecyclerView.setAdapter(mAdapter);
        mAdapter.setActionListener(mActionListener);
//        final ItemDecorator itemDecorator = new ItemDecorator();
//        messagesRecyclerView.addItemDecoration(itemDecorator);
        setUpScrollListener(messagesRecyclerView, linearLayoutManager);
    }

    private void setUpScrollListener(RecyclerView messagesRecyclerView, final LinearLayoutManager linearLayoutManager) {
        messagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                if (mIsKeyboardShown) {
//                    mIsKeyboardShown = false;
//                    hideKeyboard();
//                }
                if (linearLayoutManager.findFirstVisibleItemPosition() == 4 && mIsFetchMessageEnabled) {
                    mIsFetchMessageEnabled = false;
                    mPresenter.getTopMessages(mAdapter.getFirstMessageId(), mChat);
                }
                if ((!recyclerView.canScrollVertically(-1) || !recyclerView.canScrollVertically(1)) && !mIsBoundsWorking) {
                    mIsBoundsWorking = true;
                    long animationDuration = Math.abs(mAverageSpeed) > 100 ? 1500 : 500;
                    mAverageSpeed = (Math.abs(mAverageSpeed) > 200 ? 200 :
                            Math.abs(mAverageSpeed) > 20 ? Math.abs(mAverageSpeed) : 20);
                    animationDuration = Math.abs(animationDuration);
                    AnimationSet as = new AnimationSet(true);
                    TranslateAnimation animationUp = new TranslateAnimation(0, 0, 0, (recyclerView.canScrollVertically(-1) ? (-2) : 2) * mAverageSpeed);
                    animationUp.setRepeatCount(0);
                    animationUp.setInterpolator(new CustomInterpolator());
                    animationUp.setDuration(animationDuration / 3);
                    animationUp.setFillAfter(true);
                    as.addAnimation(animationUp);

                    TranslateAnimation animationDown = new TranslateAnimation(0, 0, 0, (recyclerView.canScrollVertically(-1) ? 2 : (-2)) * mAverageSpeed);
                    animationDown.setRepeatCount(0);
                    animationDown.setDuration(animationDuration);
                    animationUp.setInterpolator(new CustomInterpolator());
                    animationDown.setStartOffset(animationDuration / 3);

                    animationDown.setFillAfter(true);
                    as.addAnimation(animationDown);
                    animationDown.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mIsBoundsWorking = false;
                            mAverageSpeed = 0;
                            mSpeedsCounted = 0;
                            mDy = 0;
                            setUpDrag(!recyclerView.canScrollVertically(-1),
                                    !recyclerView.canScrollVertically(1),
                                    recyclerView);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    recyclerView.startAnimation(as);
                }

                if (!mIsBoundsWorking)
                    setUpDrag(!recyclerView.canScrollVertically(-1),
                            !recyclerView.canScrollVertically(1),
                            recyclerView);
//                itemDecorator.setSpeed(dy);
                calculateScrollingSpeed(dy);
            }
        });
    }

// start scrolls/drug animations

    boolean mIsBoundsWorking;
    int mSpeedsCounted;
    int mAverageSpeed;
    boolean mIsSetDrug;
    float mDy;
    boolean mIsDrugging;

    private void calculateScrollingSpeed(int ySpeed) {
        if ((mAverageSpeed >= 0 && ySpeed <= 0) || (mAverageSpeed <= 0 && ySpeed >= 0)) {
            mSpeedsCounted = 0;
            mAverageSpeed = 0;
        }
        if (Math.abs(ySpeed) < 4 || mIsBoundsWorking) return;
        mSpeedsCounted++;
        mAverageSpeed = (ySpeed + mAverageSpeed) / mSpeedsCounted;
    }

    private void setUpDrag(final boolean isTopEndVisible, final boolean isBottomEndVisible, final View recyclerView) {
        if ((isTopEndVisible || isBottomEndVisible) && !mIsSetDrug) {
            mIsSetDrug = true;
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(final View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mStartY = event.getRawY();
                        mIsDrugging = true;
                        mDy = 0;
                        return false;
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float dy = event.getRawY() - mStartY;
                        if (mIsKeyboardShown && Math.abs(dy) > 200) {
                            mIsKeyboardShown = false;
                            hideKeyboard();
                        }
                        if (isBottomEndVisible && dy < 0 && mStartY != 0) {
                            if ((Math.abs(Math.abs(dy) - Math.abs(mDy * 2))) > 10) {
                                mDy = dy / 2;
                                v.setTranslationY(mDy);
                                mIsDrugging = mDy < 0;
                                return mIsDrugging;
                            }
                        } else if (isTopEndVisible && dy > 0 && mStartY != 0) {
                            if ((Math.abs(Math.abs(dy) - Math.abs(mDy * 2))) > 10) {
                                mDy = dy / 2;
                                v.setTranslationY(mDy);
                                mIsDrugging = mDy > 0;
                                return mIsDrugging;
                            }
                        } else if ((isBottomEndVisible && dy > 0) || (isTopEndVisible && dy < 0)) {
                            recyclerView.setTranslationY(0);
                            mDy = 0;
                            mStartY = 0;
                            mIsDrugging = false;
                            return false;
                        }
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        animateViewBack(mDy, recyclerView, isBottomEndVisible, isTopEndVisible);
                        mStartY = 0;
                        mDy = 0;
                        mIsDrugging = false;
                        return true;
                    }
                    return mIsDrugging;
                }
            });
        } else if (!(isTopEndVisible || isBottomEndVisible) && mIsSetDrug && !mIsDrugging) {
            recyclerView.setOnTouchListener(mOnTouchListener);
            mIsSetDrug = false;
            recyclerView.setTranslationY(0);
            mDy = 0;
        }
    }

    private void animateViewBack(float dy, final View view, final boolean isBottomVisible, final boolean isTopVisible) {
        if (view.getTranslationY() == 0) return;
        long animationDuration = Math.round(Math.abs(dy) * 2);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, isBottomVisible ? Math.abs(dy) : Math.abs(dy) * (-1));
        animation.setRepeatCount(0);
        animation.setInterpolator(new CustomInterpolator());
        animation.setDuration(animationDuration);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setOnTouchListener(mOnTouchListener);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setOnTouchListener(mOnTouchListener);
                mIsSetDrug = false;
                mStartY = 0;
                view.setTranslationY(0);
                mDy = 0;
                view.clearAnimation();
                setUpDrag(isTopVisible, isBottomVisible, view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mStartY = event.getRawY();
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dy = event.getRawY() - mStartY;
                if (mIsKeyboardShown && Math.abs(dy) > 200) {
                    mIsKeyboardShown = false;
                    hideKeyboard();
                }
            }
            return false;
        }
    };
// end scrolls/drug animations

    MessagesAdapter.OnMessageActionListener mActionListener = new MessagesAdapter.OnMessageActionListener() {
        @Override
        public void onMessageEdit(Message message) {
            mChangeableMessage = message;
            mIsEditing = true;
            mCancelEditButton.setVisibility(View.VISIBLE);
            mChangeableMessage.setEdited(true);
            mMessageEditText.setText(message.getText());
            mMessageEditText.setSelection(mMessageEditText.getText().length());
            mMessageEditText.requestFocus();
            mIsKeyboardShown = true;
            setEnableSend(true);
            if (mChangeableMessage.isMedia())
                showPreview(mChangeableMessage.getMedias());
        }

        @Override
        public void onMessageRemove(Message message, boolean isInLast20th) {
            mPresenter.removeMessage(message, mChat, isInLast20th);
        }

        @Override
        public void onMediaClick(Media media) {
            if (media.isVideo()) {
                startActivity(VideoViewerActivity.getStartIntent(ChatActivity.this, media.getVideoUrl()));
            } else {
                List<Media> mediaContents = new ArrayList<>();
                int position = 0;
                boolean isSelectedFound = false;
                for (Message message1 : mAdapter.getMessages())
                    if (message1.isMedia()) {
                        for (Media media1 : message1.getMedias()) {
                            if (!media1.isVideo()) {
                                if (media1.getImageUrl().equals(media.getImageUrl())) {
                                    isSelectedFound = true;
                                } else if (!isSelectedFound) {
                                    position++;
                                }
                                mediaContents.add(media1);
                            }
                        }
                    }
                startActivity(ImageViewerActivity.getStartIntent(ChatActivity.this, mediaContents, position));
            }
        }

        @Override
        public void onUserClick(User user) {
            startActivity(ProfileActivity.startIntent(ChatActivity.this, user));
        }
    };

    private void resetPreview() {
        mPreviewRecyclerView.setVisibility(View.GONE);
        mPreviewAdapter.clearItems();
        if (mMessageEditText.getText().toString().trim().isEmpty()) setEnableSend(false);
    }

    private void setEnableSend(boolean isEnable) {
        if (isEnable && mSendButton.getAlpha() == 1) return;
        mSendButton.setAlpha(isEnable ? 1.0f : 0.5f);
        mSendButton.setClickable(isEnable);
    }

    @OnClick(R.id.edit_text_message)
    void onMessageFieldClick() {
        mMediaButtonsLayout.setVisibility(View.GONE);
        mAddImageButton.setVisibility(View.VISIBLE);
        mIsKeyboardShown = true;
    }

    @OnClick(R.id.button_cancel_edit)
    void onCancelEditClick() {
        resetPreview();
        mMessageEditText.getText().clear();
        mChangeableMessage.setEdited(false);
        mChangeableMessage = null;
        mIsEditing = false;
        mCancelEditButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.button_add_image)
    void onAddPhotoClick() {
        mAddImageButton.setVisibility(View.GONE);
        mMediaButtonsLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.button_start_camera)
    void onStartCameraClick() {
        mMediaLib.startCamera();
    }

    @OnClick(R.id.button_start_gallery)
    void onStartGalleryClick() {
        mMediaLib.startGallery();
    }

    @OnClick(R.id.button_send)
    void onSendClick() {
        String text = mMessageEditText.getText().toString();
        if (text.trim().isEmpty()
                && (mChangeableMessage == null
                || mChangeableMessage.getMedias() == null
                || mChangeableMessage.getMedias().isEmpty())) return;
        Message message;
        if (mIsEditing) {
            mChangeableMessage.setText(text);
            message = mChangeableMessage;
            mIsEditing = false;
            mCancelEditButton.setVisibility(View.GONE);
        } else {
            message = getMessage(text);
        }
        mMessageEditText.getText().clear();
        resetPreview();
        setEnableSend(false);
        mAdapter.updateMessage(message);
        mPresenter.setMessage(message, mChat);
    }

    private Message getMessage(String messageText) {
        Message.Builder builder = new Message.Builder();
        return builder.setChatId(mChat.getId())
                .setSenderId(mPreferences.getId())
                .setText(messageText)
                .setType("user")
                .setTimeStamp(DateUtil.getStringTime())
                .create();
    }

    @OnClick(R.id.button_edit)
    void onEditClick() {
        if (mChat.isSingle()) {
            User user = null;
            for (String id : mChat.getMemberIds())
                if (!id.equals(mPreferences.getId()))
                    for (User user1 : mRoliqueAppUsers.getUsers())
                        if (user1.getId().equals(id)) {
                            user = user1;
                            break;
                        }
            startActivity(ProfileActivity.startIntent(ChatActivity.this, user));
        } else if (mChat.getOwnerId().equals(mPreferences.getId())) {
            startActivityForResult(ChatEditorActivity.startIntent(ChatActivity.this, mChat), RC_CHAT_EDIT);
        } else {
            mPresenter.leaveChat(mChat, mPreferences.getId());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mMediaLib != null) mMediaLib.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RC_CHAT_EDIT) {
            boolean isDeleted = data.getBooleanExtra(getString(R.string.extra_chat_from_edit), false);
            if (isDeleted) onBackPressed();
            mChat = data.getParcelableExtra(getString(R.string.extra_chat_from_editor));
            setUpToolbar(mChat);
        }
    }

    @Override
    public void showTopMessagesView(List<Message> messages, boolean isFetchMessageEnabled) {
        mIsFetchMessageEnabled = isFetchMessageEnabled;
        mAdapter.addTopMessages(messages);
    }

    @Override
    public void showNewMessageView(Message message) {
        mAdapter.addNewMessage(message);
        RecyclerView messagesRecyclerView = getViewById(R.id.recycler_view_messages);
        messagesRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void updateMessageView(Message message) {
        mAdapter.updateMessage(message);
    }

    @Override
    public void removedMessageView(String messageId) {
        mAdapter.removeMessage(messageId);
    }

    @Override
    public void showErrorInView(String message) {
        showSnackbar(message);
    }

    @Override
    public void showLeaveInView() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        mPresenter.stop();
        if (mCancelEditButton.getVisibility() == View.VISIBLE) {
            onCancelEditClick();
            return;
        }
        if (mIsKeyboardShown)
            mIsKeyboardShown = false;
        super.onBackPressed();
    }
}
