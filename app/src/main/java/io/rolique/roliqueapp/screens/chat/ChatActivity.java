package io.rolique.roliqueapp.screens.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.chat.adapters.MessagesAdapter;
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
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.edit_text_message) EditText mMessageEditText;

    @Inject ChatPresenter mPresenter;
    @Inject RoliqueAppUsers mRoliqueAppUsers;
    @Inject RoliqueApplicationPreferences mPreferences;

    private MessagesAdapter mAdapter;
    Chat mChat;
    private MediaLib mediaLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChat = getIntent().getParcelableExtra(EXTRA_CHAT);
        setUpToolbar(mChat);
        setUpMediaLib();
        setUpRecyclerView();
        setUpRefreshLayout();

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
        mediaLib = new MediaLib(ChatActivity.this, new MediaLib.MediaLibListener() {
            @Override
            public void onSuccess(List<MediaContent> mediaContents) {
                Timber.e("SUCCESS!!!");
                Timber.d(mediaContents.toString());
                List<Media> messageMedias = new ArrayList<>();
                for (MediaContent mediaContent : mediaContents)
                    messageMedias.add(new Media
                            .Builder()
                            .setHeight(mediaContent.getHeight())
                            .setWidth(mediaContent.getWidth())
                            .setImageUrl(mediaContent.getImage().getAbsolutePath())
                            .setMediaType(Media.CATEGORY_IMAGE)
                            .create());
                //TODO: add preview firstly
                mPresenter.addMediaMessage(getMediaMessage(mMessageEditText.getText().toString(), messageMedias), mChat);
            }

            @Override
            public void onError(Exception e) {

            }
        });
        mediaLib.setStorage(MediaLib.GLOBAL_MEDIA_DEFAULT_FOLDER);
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

    private void setUpRecyclerView() {
        RecyclerView messagesRecyclerView = getViewById(R.id.recycler_view_messages);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MessagesAdapter(ChatActivity.this, mPreferences.getId(), mRoliqueAppUsers.getUsers());
        messagesRecyclerView.setAdapter(mAdapter);
    }

    private void setUpRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.getTopMessages(mAdapter.getFirstMessageId(), mChat);
            }
        });
    }

    @OnClick(R.id.button_add_image)
    void onAddPhotoClick() {
        mediaLib.startCamera();
    }

    @OnClick(R.id.button_send)
    void onSendClick() {
        String text = mMessageEditText.getText().toString();
        if (text.trim().isEmpty()) return;
        Message message = getMessage(text);
        mPresenter.addMessage(message, mChat);
        mMessageEditText.getText().clear();
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
        if (mediaLib != null) mediaLib.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RC_CHAT_EDIT) {
            boolean isDeleted = data.getBooleanExtra(getString(R.string.extra_chat_from_edit), false);
            if (isDeleted) onBackPressed();
        }
    }

    @Override
    public void showLastMessagesView(List<Message> messages) {
        mAdapter.setMessages(messages);
    }

    @Override
    public void showTopMessagesView(List<Message> messages) {
        mAdapter.addTopMessages(messages);
    }

    @Override
    public void showNewMessageView(Message message) {
        mAdapter.addNewMessage(message);
        RecyclerView messagesRecyclerView = getViewById(R.id.recycler_view_messages);
        messagesRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void setProgressIndicator(boolean active) {
        mSwipeRefreshLayout.setRefreshing(active);
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
