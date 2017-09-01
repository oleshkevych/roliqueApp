package io.rolique.roliqueapp.screens.newChat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.newChat.adapters.ImageDecoration;
import io.rolique.roliqueapp.screens.newChat.adapters.MembersAdapter;
import io.rolique.roliqueapp.screens.newChat.adapters.UsersAdapter;

public class NewChatActivity extends BaseActivity implements NewChatContract.View {

    public static Intent startIntent(Context context) {
        return new Intent(context, NewChatActivity.class);
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.view_switcher) ViewSwitcher mViewSwitcher;
    @BindView(R.id.text_view_user_image) TextView mUserImageTextView;
    @BindView(R.id.edit_text_chat_name) EditText mChatNameEditText;

    @Inject NewChatPresenter mPresenter;
    @Inject RoliqueAppUsers mRoliqueAppUsers;
    @Inject RoliqueApplicationPreferences mPreferences;

    MembersAdapter mMembersAdapter;
    UsersAdapter mUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        setUpToolbar();
        setUpHeader();
        setUpRecyclersView();
    }

    @Override
    protected void inject() {
        DaggerNewChatComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .newChatPresenterModule(new NewChatPresenterModule(NewChatActivity.this))
                .build()
                .inject(NewChatActivity.this);
    }

    private void setUpToolbar() {
        mToolbar.setTitle(R.string.activity_new_chat_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpHeader() {
        mChatNameEditText.addTextChangedListener(mOnNameEditorActionListener);
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
            String s = editable.toString().trim();
            String text = "";
            if (!s.isEmpty()) {
                if (editable.toString().trim().contains(" ")) {
                    String[] letters = s.split(" ");
                    int i = 0;
                    while (text.length() < 2 && letters.length > i) {
                        if (!letters[i].trim().isEmpty())
                            text += letters[i].trim().substring(0, 1)
                                    .toUpperCase();
                        i++;
                    }
                } else {
                    text += s.substring(0, 1)
                            .toUpperCase();
                }
            }
            mUserImageTextView.setText(text);
        }
    };

    private void setUpRecyclersView() {
        RecyclerView usersRecyclerView = getViewById(R.id.recycler_view_users);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(NewChatActivity.this));
        mUsersAdapter = new UsersAdapter(NewChatActivity.this, getUsers(mRoliqueAppUsers.getUsers()));
        usersRecyclerView.setAdapter(mUsersAdapter);
        mUsersAdapter.setOnItemClickListener(mOnItemClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NewChatActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        RecyclerView membersRecyclerView = getViewById(R.id.recycler_view_members);
        membersRecyclerView.setLayoutManager(linearLayoutManager);
        membersRecyclerView.addItemDecoration(new ImageDecoration(getResources().getDimensionPixelSize(R.dimen.members_recycler_padding)));
        mMembersAdapter = new MembersAdapter(NewChatActivity.this);
        membersRecyclerView.setAdapter(mMembersAdapter);
    }

    private List<User> getUsers(List<User> users) {
        List<User> userList = new ArrayList<>(users.size() - 1);
        for (User user : users)
            if (!user.getId().equals(mPreferences.getId())) userList.add(user);
        return userList;
    }

    UsersAdapter.OnItemClickListener mOnItemClickListener = new UsersAdapter.OnItemClickListener() {
        @Override
        public void onUserSelected(User user) {
            mMembersAdapter.addMember(user.getImageUrl());
        }

        @Override
        public void onUserUnselected(User user) {
            mMembersAdapter.removeMember(user.getImageUrl());
        }
    };

    @OnClick(R.id.button_save)
    void onSaveClick() {
        String ids = "";
        for (String s : mUsersAdapter.getSelectedUserIds())
            ids += s;
        showSnackbar(ids);
    }

    @Override
    public void showSavedInView() {
        finish();
    }

    @Override
    public void setProgressIndicator(boolean active) {
        mViewSwitcher.setDisplayedChild(active ? 1 : 0);
        getViewById(R.id.layout_progress).setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showErrorInView(String message) {
        showSnackbar(message);
    }
}
