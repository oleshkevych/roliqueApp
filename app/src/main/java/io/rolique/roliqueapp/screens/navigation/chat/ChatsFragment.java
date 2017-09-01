package io.rolique.roliqueapp.screens.navigation.chat;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.Chat;

public class ChatsFragment extends BaseFragment implements ChatsContract.View {

    public static Fragment newInstance() {
        return new ChatsFragment();
    }

    @BindView(R.id.recycler_view_chats) RecyclerView mRecyclerView;

    @Inject ChatsPresenter mPresenter;
    @Inject RoliqueApplicationPreferences mPreferences;
    @Inject RoliqueAppUsers mUsers;

    ChatsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmnet_chats, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpChatsRecycler();
    }

    @Override
    protected void inject() {
        DaggerChatsComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getActivity().getApplication()).getRepositoryComponent())
                .chatsPresenterModule(new ChatsPresenterModule(ChatsFragment.this))
                .build()
                .inject(ChatsFragment.this);
    }

    private void setUpChatsRecycler() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mAdapter = new ChatsAdapter(mRecyclerView.getContext(), mUsers);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(mItemClickListener);
        mPresenter.setUpChatsListener();
    }

    ChatsAdapter.OnItemClickListener mItemClickListener = new ChatsAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(Chat chat) {
            showSnackbar(getView(), chat.getId());
        }
    };

    @Override
    public void showAddedChatInView(Chat chat) {
        mAdapter.addChat(chat);
    }

    @Override
    public void showChangedChatInView(Chat chat) {
        mAdapter.changeChat(chat);
    }

    @Override
    public void showRemovedChatInView(Chat chat) {
        mAdapter.removeChat(chat);
    }

    @Override
    public void showErrorInView(String error) {
        showSnackbar(getView(), error);
    }

    @Override
    public void onDestroyView() {
        mPresenter.stop();
        super.onDestroyView();
    }
}
