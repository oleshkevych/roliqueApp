package io.rolique.cameralibrary.screens.imageViewer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.screens.camera.CameraBaseActivity;
import io.rolique.cameralibrary.widget.CustomViewPager;

/**
 * Created by Volodymyr Oleshkevych on 5/18/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_MEDIAS = "IMAGE_PATH";
    private static final String EXTRA_SELECTED_POSITION = "SELECTED_POSITION";
    private static final String STATE_SELECTED_POSITION = "STATE_SELECTED_POSITION";
    private static final String EXTRA_IS_SHOW_DELETING = "IS_SHOW_DELETING";

    public static Intent getStartIntent(Activity activity, List<MediaContent> mediaContents, int position) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_MEDIAS, new ArrayList<MediaContent>(mediaContents));
        intent.putExtra(EXTRA_SELECTED_POSITION, position);
        intent.putExtra(EXTRA_IS_SHOW_DELETING, activity instanceof CameraBaseActivity);
        return intent;
    }

    List<MediaContent> mMediaContents;
    FragmentViewPagerAdapter mFragmentViewPagerAdapter;
    CustomViewPager mViewPager;
    Toolbar mToolbar;
    ImageButton mImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer_library);
        mToolbar = findViewById(R.id.toolbar);
        mImageButton = findViewById(R.id.image_button_delete);
        mMediaContents = getIntent().getParcelableArrayListExtra(EXTRA_MEDIAS);
        int startPosition = getIntent().getIntExtra(EXTRA_SELECTED_POSITION, 0);
        if (savedInstanceState != null)
            startPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        boolean isShowDeleting = getIntent().getBooleanExtra(EXTRA_IS_SHOW_DELETING, false);
        setUpToolbar(isShowDeleting);
        setUpViewPager(startPosition);
    }

    private void setUpToolbar(boolean isShowDeleting) {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (isShowDeleting)
            mImageButton.setOnClickListener(mOnDeleteClickListener);
        else
            mImageButton.setVisibility(View.GONE);
    }

    View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog
                    .Builder(ImageViewerActivity.this)
                    .setTitle(R.string.activity_image_viewer_alert_title)
                    .setMessage(R.string.activity_image_viewer_alert_body)
                    .setCancelable(true)
                    .setNegativeButton(R.string.activity_image_viewer_negative_button, null)
                    .setPositiveButton(R.string.activity_image_viewer_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int index = mViewPager.getCurrentItem();
                            new File(mMediaContents.get(index).getImage()).delete();
                            mMediaContents.remove(index);
                            if (mFragmentViewPagerAdapter.getCount() == 1) onBackPressed();
                            mFragmentViewPagerAdapter.removeFragment(index);
                            mFragmentViewPagerAdapter.notifyDataSetChanged();
                        }
                    })
                    .show();
        }
    };

    private void setUpViewPager(int startPosition) {
        mFragmentViewPagerAdapter = new FragmentViewPagerAdapter(getFragmentManager());
        int imagesCount = -1;
        for (MediaContent mediaContent : mMediaContents) {
            if (mediaContent.isImage()) {
                ImageFragment fragment = ImageFragment.newInstance(mediaContent);
                mFragmentViewPagerAdapter.addFragment(fragment);
                imagesCount++;
            }
            if (mMediaContents.indexOf(mediaContent) == startPosition)
                startPosition = imagesCount;
        }
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(mFragmentViewPagerAdapter);
        mViewPager.setCurrentItem(startPosition);
        mViewPager.setOnTouchListener(null);
    }

    private class FragmentViewPagerAdapter extends FragmentStatePagerAdapter {

        public List<Fragment> getFragments() {
            return mFragments;
        }

        private final List<Fragment> mFragments = new ArrayList<>();

        FragmentViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }

        void removeFragment(int index) {
            mFragments.remove(index);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            int index = mFragments.indexOf(object);

            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_SELECTED_POSITION, mViewPager.getCurrentItem());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(getString(R.string.extra_camera_images), new ArrayList<Parcelable>(mMediaContents));
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}
