package io.rolique.roliqueapp.screens.editChat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.editChat.adapters.ImageDecoration;
import io.rolique.roliqueapp.screens.editChat.adapters.MembersAdapter;
import io.rolique.roliqueapp.screens.editChat.adapters.UsersAdapter;
import io.rolique.roliqueapp.util.ui.UiUtil;

public class ChatEditorActivity extends BaseActivity implements ChatEditorContract.View {

    private static final String EXTRA_CHAT = "CHAT";

    public static Intent startIntent(Context context, Chat chat) {
        Intent intent = new Intent(context, ChatEditorActivity.class);
        intent.putExtra(EXTRA_CHAT, chat);
        return intent;
    }

    public static Intent startIntent(Context context) {
        return new Intent(context, ChatEditorActivity.class);
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.view_switcher_progress) ViewSwitcher mProgressViewSwitcher;
    @BindView(R.id.view_switcher) ViewSwitcher mImageViewSwitcher;
    @BindView(R.id.text_view_image) TextView mImageTextView;
    @BindView(R.id.edit_text_chat_name) EditText mChatNameEditText;

    @Inject ChatEditorPresenter mPresenter;
    @Inject RoliqueAppUsers mRoliqueAppUsers;
    @Inject RoliqueApplicationPreferences mPreferences;

    MembersAdapter mMembersAdapter;
    UsersAdapter mUsersAdapter;
    Chat mChat;
    MediaLib mMediaLib;
    String mImagePath = "";
    boolean mIsEditingMode;
    boolean mIsDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_editor);

        if (getIntent().hasExtra(EXTRA_CHAT)) {
            mIsEditingMode = true;
            mChat = getIntent().getParcelableExtra(EXTRA_CHAT);
            findViewById(R.id.button_delete).setVisibility(View.VISIBLE);
            mImagePath = mChat.getImageUrl();
        }

        setUpMediaLib();
        setUpToolbar();
        setUpHeader();
        setUpRecyclersView();
    }

    @Override
    protected void inject() {
        DaggerChatEditorComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .chatEditorPresenterModule(new ChatEditorPresenterModule(ChatEditorActivity.this))
                .build()
                .inject(ChatEditorActivity.this);
    }

    private void setUpMediaLib() {
        mMediaLib = new MediaLib(ChatEditorActivity.this, mMediaLibListener);
        mMediaLib.setFrontCamera(true);
        mMediaLib.setRotation(true);
        mMediaLib.setSinglePhoto(true);
    }

    MediaLib.MediaLibListener mMediaLibListener = new MediaLib.MediaLibListener() {
        @Override
        public void onSuccess(List<MediaContent> mediaContents) {
            mImagePath = mediaContents.get(0).getImage();
            UiUtil.updateImageIfExists(mImageViewSwitcher, mImagePath, "");
        }

        @Override
        public void onEmpty() {

        }

        @Override
        public void onError(Exception e) {

        }
    };

    private void setUpToolbar() {
        mToolbar.setTitle(mIsEditingMode ? mChat.getTitle() : getString(R.string.activity_new_chat_title));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpHeader() {
        mChatNameEditText.addTextChangedListener(mOnNameEditorActionListener);
        if (mIsEditingMode) {
            mChatNameEditText.setText(mChat.getTitle());
        }
        UiUtil.setImageIfExists(mImageViewSwitcher, mImagePath, mChat == null ? "" : mChat.getTitle(), 88);
    }

    TextWatcher mOnNameEditorActionListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mImagePath.isEmpty())
                UiUtil.updateImageIfExists(mImageViewSwitcher, mImagePath, editable.toString());
        }
    };

    private void setUpRecyclersView() {
        if (mRoliqueAppUsers.getUsers().isEmpty()) {
            setProgressIndicator(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setUpRecyclersView();
                }
            }, 500);
        } else {
            setProgressIndicator(false);
            RecyclerView usersRecyclerView = getViewById(R.id.recycler_view_users);
            usersRecyclerView.setLayoutManager(new LinearLayoutManager(ChatEditorActivity.this));
            mUsersAdapter = new UsersAdapter(ChatEditorActivity.this, getUsers(mRoliqueAppUsers.getUsers()));
            usersRecyclerView.setAdapter(mUsersAdapter);
            mUsersAdapter.setOnItemClickListener(mOnItemClickListener);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatEditorActivity.this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            RecyclerView membersRecyclerView = getViewById(R.id.recycler_view_members);
            membersRecyclerView.setLayoutManager(linearLayoutManager);
            membersRecyclerView.addItemDecoration(new ImageDecoration(getResources().getDimensionPixelSize(R.dimen.members_recycler_padding)));
            mMembersAdapter = new MembersAdapter(ChatEditorActivity.this);
            membersRecyclerView.setAdapter(mMembersAdapter);
            if (mIsEditingMode) setMembersInView();
        }
    }

    private List<User> getUsers(List<User> users) {
        List<User> userList = new ArrayList<>(users.size() - 1);
        for (User user : users)
            if (!user.getId().equals(mPreferences.getId())) userList.add(user);
        return userList;
    }

    private void setMembersInView() {
        List<String> ids = new ArrayList<>();
        for (String memberId : mChat.getMemberIds())
            for (User user : mRoliqueAppUsers.getUsers())
                if (memberId.equals(user.getId()) && !memberId.equals(mPreferences.getId())) {
                    mMembersAdapter.addMember(getPairForImage(user));
                    ids.add(memberId);
                    break;
                }
        mUsersAdapter.setMemberIds(ids);
    }

    private Pair<String, String> getPairForImage(User user) {
        return new Pair<>(user.getImageUrl(), String.format("%s %s", user.getFirstName(), user.getLastName()));
    }

    UsersAdapter.OnItemClickListener mOnItemClickListener = new UsersAdapter.OnItemClickListener() {
        @Override
        public void onUserSelected(User user) {
            mMembersAdapter.addMember(getPairForImage(user));
        }

        @Override
        public void onUserUnselected(User user) {
            mMembersAdapter.removeMember(getPairForImage(user));
        }
    };

    @OnClick(R.id.view_switcher)
    void onImageClick() {
        mMediaLib.startCamera();
    }

    @OnClick(R.id.button_save)
    void onSaveClick() {
        if (lackTitle()) return;
        Chat chat = new Chat();
        chat.setImageUrl(mImagePath);
        chat.setMemberIds(getMemberIds());
        chat.setOwnerId(mPreferences.getId());
        chat.setTitle(mChatNameEditText.getText().toString());
        if (mIsEditingMode) {
            chat.setId(mChat.getId());
            mPresenter.editChat(chat, mChat);
        } else {
            mPresenter.saveNewChat(chat);
        }
    }

    private boolean lackTitle() {
        if (mChatNameEditText.getText().toString().trim().isEmpty()) {
            showSnackbar(R.string.activity_new_chat_error_empty_title);
            return true;
        }
        return false;
    }

    private List<String> getMemberIds() {
        List<String> members = mUsersAdapter.getSelectedUserIds();
        members.add(mPreferences.getId());
        return members;
    }

    @OnClick(R.id.button_delete)
    void onDeleteClick() {
        mIsDeleted = true;
        mPresenter.deleteChat(mChat);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMediaLib.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showSavedInView(Chat chat) {
        Intent intent = new Intent();
        intent.putExtra(getString(R.string.extra_chat_from_edit), mIsDeleted);
        intent.putExtra(getString(R.string.extra_chat_from_editor), chat);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void setProgressIndicator(boolean active) {
        mProgressViewSwitcher.setDisplayedChild(active ? 1 : 0);
        getViewById(R.id.layout_progress).setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @OnTouch(R.id.layout_progress)
    boolean onProgressLayoutTouch() {
        return true;
    }

    @Override
    public void showErrorInView(String message) {
        showSnackbar(message);
    }
}
