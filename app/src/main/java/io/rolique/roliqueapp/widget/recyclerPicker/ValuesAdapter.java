package io.rolique.roliqueapp.widget.recyclerPicker;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 9/19/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class ValuesAdapter extends RecyclerView.Adapter<ValuesAdapter.UserViewHolder> {

    private final LayoutInflater mInflater;
    private String[] mValue;

    interface OnItemClickListener {
        void onValueClick(String value);
    }

    private OnItemClickListener mOnItemClickListener;

    ValuesAdapter(Context context, String[] values) {
        mInflater = LayoutInflater.from(context);
        mValue = values;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_text, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        holder.bindChat(mValue[position]);
    }

    @Override
    public int getItemCount() {
        return mValue.length;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_view) TextView mValueTextView;

        String mValue;

        UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(UserViewHolder.this, itemView);
            itemView.setOnClickListener(mOnClickListener);
        }

        final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onValueClick(mValue);
            }
        };

        void bindChat(String value) {
            mValue = value;
            mValueTextView.setText(mValue);
        }
    }
}
