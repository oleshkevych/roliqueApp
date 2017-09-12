package io.rolique.roliqueapp.screens.navigation.contacts;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.editChat.DaggerChatEditorComponent;
import io.rolique.roliqueapp.screens.navigation.contacts.adapter.UsersAdapter;

public class ContactsFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new ContactsFragment();
    }

    UsersAdapter mUsersAdapter;

    @Inject RoliqueAppUsers mRoliqueAppUsers;

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
            showSnackbar(getView(), user.getFirstName());
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) return;
        if (mRoliqueAppUsers.getUsers().isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mUsersAdapter.setUsers(mRoliqueAppUsers.getUsers());
                }
            }, 500);
        } else {
            mUsersAdapter.setUsers(mRoliqueAppUsers.getUsers());
        }
    }
}
