package io.rolique.roliqueapp.screens.navigation.contacts;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnTextChanged;
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.navigation.contacts.adapter.UsersAdapter;
import io.rolique.roliqueapp.screens.profile.ProfileActivity;

public class ContactsFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new ContactsFragment();
    }

    UsersAdapter mUsersAdapter;

    @Inject RoliqueAppUsers mRoliqueAppUsers;

    @BindView(R.id.edit_text_search) EditText mSearchEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    protected void inject() {
        DaggerContactsComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getActivity().getApplication()).getRepositoryComponent())
                .build()
                .inject(ContactsFragment.this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpRecyclersView(view);
        setUpSearchEditText();
    }

    private void setUpRecyclersView(final View view) {
        RecyclerView usersRecyclerView = view.findViewById(R.id.recycler_view_users);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(usersRecyclerView.getContext()));
        mUsersAdapter = new UsersAdapter(usersRecyclerView.getContext());
        usersRecyclerView.setAdapter(mUsersAdapter);
        mUsersAdapter.setOnItemClickListener(mOnItemClickListener);
    }

    UsersAdapter.OnItemClickListener mOnItemClickListener = new UsersAdapter.OnItemClickListener() {
        @Override
        public void onUserClicked(User user) {
            startActivity(ProfileActivity.startIntent(getActivity(), user));
        }
    };

    private void updateUsersInView() {
        if (mRoliqueAppUsers.getUsers().isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateUsersInView();
                }
            }, 500);
        } else {
            mUsersAdapter.setUsers(mRoliqueAppUsers.getUsers());
        }
    }

    private void setUpSearchEditText() {
        Drawable searchDrawable = AppCompatResources.getDrawable(mSearchEditText.getContext(), R.drawable.ic_search_black_24dp);
        mSearchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(searchDrawable, null, null, null);
        mSearchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_END = 2;

                if (mSearchEditText.getCompoundDrawables()[DRAWABLE_END] != null
                        && event.getAction() == MotionEvent.ACTION_UP) {
                    int drawableWidth = mSearchEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width();
                    float drawableCoordinate = mSearchEditText.getRight() - drawableWidth;
                    if (event.getRawX() >= drawableCoordinate) {
                        mSearchEditText.getText().clear();
                    }
                }
                return false;
            }
        });
    }

    @OnTextChanged(value = R.id.edit_text_search)
    public void onSearchTextChanged(CharSequence text) {
        toggleSearchEditTextDrawables(text);
        filterUsers(text.toString(), mRoliqueAppUsers.getUsers());
    }

    private void toggleSearchEditTextDrawables(CharSequence text) {
        final int DRAWABLE_END = 2;
        Drawable[] drawables = mSearchEditText.getCompoundDrawables();
        Drawable searchDrawable = AppCompatResources.getDrawable(mSearchEditText.getContext(), R.drawable.ic_search_black_24dp);
        if (text.length() == 0 && drawables[DRAWABLE_END] != null) {
            mSearchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(searchDrawable, null, null, null);
        } else if (drawables[DRAWABLE_END] == null) {
            Drawable clearDrawable = AppCompatResources.getDrawable(mSearchEditText.getContext(), R.drawable.ic_clear_black_24dp);
            mSearchEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(searchDrawable, null, clearDrawable, null);
        }
    }

    private void filterUsers(String text, List<User> users) {
        if (text.isEmpty()) {
            mUsersAdapter.setUsers(users);
            return;
        }
        List<User> filteredUsers = new ArrayList<>();
        for (User user: users) {
            if (String.format("%s %s", user.getFirstName(), user.getLastName()).toLowerCase().contains(text.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        mUsersAdapter.setUsers(filteredUsers);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUsersInView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            updateUsersInView();
        else hideKeyboard();
    }
}
