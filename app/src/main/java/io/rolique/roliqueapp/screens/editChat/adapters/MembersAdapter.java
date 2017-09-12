package io.rolique.roliqueapp.screens.editChat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 9/1/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {

    private final LayoutInflater mInflater;

    private List<String> mImages;

    public MembersAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mImages = new ArrayList<>();
    }

    public void addMember(String image) {
        mImages.add(image);
        notifyItemInserted(mImages.size() - 1);
    }

    public void removeMember(String image) {
        for (int i = 0; i< mImages.size(); i++)
            if (mImages.get(i).equals(image)) {
                mImages.remove(i);
                notifyItemRemoved(i);
            }
    }

    @Override
    public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MemberViewHolder holder, int position) {
        holder.bindMember(mImages.get(position));
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_view) ImageView mImageView;

        MemberViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(MemberViewHolder.this, itemView);
        }

        void bindMember(String imageUrl) {
            UiUtil.setImage(mImageView, imageUrl);
        }
    }
}
