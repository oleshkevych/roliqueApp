package io.rolique.roliqueapp.screens.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.screens.imageViewer.ImageViewerActivity;
import io.rolique.cameralibrary.screens.videoViewer.VideoViewerActivity;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.chat.adapters.MessagesAdapter;
import io.rolique.roliqueapp.screens.chat.adapters.PreviewAdapter;
import io.rolique.roliqueapp.screens.editChat.ChatEditorActivity;
import io.rolique.roliqueapp.util.DateUtil;
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
    @BindView(R.id.recycler_view_image_preview) RecyclerView mPreviewRecyclerView;
    @BindView(R.id.edit_text_message) EditText mMessageEditText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChat = getIntent().getParcelableExtra(EXTRA_CHAT);
        setUpToolbar(mChat);
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
        else
            imageButton.setImageResource(chat.getOwnerId().equals(mPreferences.getId()) ? R.drawable.ic_edit_white_24dp : R.drawable.ic_move_out_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpMediaLib() {
        mMediaLib = new MediaLib(ChatActivity.this, new MediaLib.MediaLibListener() {
            @Override
            public void onSuccess(List<MediaContent> mediaContents) {
                Timber.d(mediaContents.toString());
                List<Media> messageMedias = new ArrayList<>();
                for (MediaContent mediaContent : mediaContents)
                    messageMedias.add(new Media
                            .Builder()
                            .setHeight(mediaContent.getHeight())
                            .setWidth(mediaContent.getWidth())
                            .setImageUrl(mediaContent.getImage().getAbsolutePath())
                            .setVideoUrl(mediaContent.isVideo() ? mediaContent.getVideo().getAbsolutePath() : null)
                            .setMediaType(mediaContent.isImage() ? Media.CATEGORY_IMAGE : Media.CATEGORY_VIDEO)
                            .create());
                mChangeableMessage = getMediaMessage(mMessageEditText.getText().toString(), messageMedias);
                mIsEditing = true;
                showPreview(messageMedias);
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onError(Exception e) {

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
        RecyclerView messagesRecyclerView = getViewById(R.id.recycler_view_messages);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MessagesAdapter(ChatActivity.this, mPreferences.getId(), mRoliqueAppUsers.getUsers());
        messagesRecyclerView.setAdapter(mAdapter);
        mAdapter.setActionListener(mActionListener);
        messagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (linearLayoutManager.findFirstVisibleItemPosition() == 3 && mIsFetchMessageEnabled) {
                    mIsFetchMessageEnabled = false;
                    mPresenter.getTopMessages(mAdapter.getFirstMessageId(), mChat);
                }
            }
        });

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
                    startActivity(ImageViewerActivity.getStartIntent(ChatActivity.this, createMediaContentList(mChangeableMessage.getMedias()), position));
                }
            }

            @Override
            public void onRemoveClick(int position) {
                new File(mChangeableMessage.getMedias().get(position).getImageUrl()).delete();
                if (mChangeableMessage.getMedias().get(position).isVideo())
                    new File(mChangeableMessage.getMedias().get(position).getVideoUrl()).delete();
                mChangeableMessage.getMedias().remove(position);
                mPreviewAdapter.removeItem(position);
                if (mChangeableMessage.getMedias().size() == 0) resetPreview();
            }
        });
    }

    private List<MediaContent> createMediaContentList(List<Media> medias) {
        List<MediaContent> mediaContents = new ArrayList<>(medias.size());
        for (Media media: medias)
            if (!media.isVideo())
                mediaContents.add(new MediaContent.Builder().
                        setHeight(media.getHeight()).
                        setWidth(media.getWidth()).
                        setMediaType(MediaContent.CATEGORY_IMAGE).
                        setImage(new File(media.getImageUrl())).
                        create());
        return mediaContents;
    }

    protected void resetPreview() {
        mPreviewRecyclerView.setVisibility(View.GONE);
        mPreviewAdapter.clearItems();
    }

    MessagesAdapter.OnMessageActionListener mActionListener = new MessagesAdapter.OnMessageActionListener() {
        @Override
        public void onMessageEdit(Message message) {
            mChangeableMessage = message;
            mIsEditing = true;
            mChangeableMessage.setEdited(true);
            mMessageEditText.setText(message.getText());
        }

        @Override
        public void onMessageRemove(Message message) {
            mPresenter.removeMessage(message, mChat);
        }
    };

    @OnClick(R.id.button_add_image)
    void onAddPhotoClick() {
        mMediaLib.startCamera();
    }

    @OnClick(R.id.button_send)
    void onSendClick() {
        String text = mMessageEditText.getText().toString();
        if (text.trim().isEmpty()) return;
        Message message;
        if (mIsEditing) {
            if (text.equals(mChangeableMessage.getText())) {
                mIsEditing = false;
                return;
            }
            mChangeableMessage.setText(text);
            message = mChangeableMessage;
        } else {
            message = getMessage(text);
        }
        mMessageEditText.getText().clear();
        resetPreview();
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
        if (mChat.getOwnerId().equals(mPreferences.getId())) {
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
        super.onBackPressed();
    }
}
