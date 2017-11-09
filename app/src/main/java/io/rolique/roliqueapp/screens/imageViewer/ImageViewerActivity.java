package io.rolique.roliqueapp.screens.imageViewer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.widget.CustomViewPager;


/**
 * Created by Volodymyr Oleshkevych on 5/18/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_MEDIAS = "IMAGE_PATH";
    private static final String EXTRA_SELECTED_POSITION = "SELECTED_POSITION";

    public static Intent getStartIntent(Activity activity, List<Media> mediaContents, int position) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_MEDIAS, new ArrayList<Media>(mediaContents));
        intent.putExtra(EXTRA_SELECTED_POSITION, position);
        return intent;
    }

    List<Media> mMediaContents;
    FragmentViewPagerAdapter mFragmentViewPagerAdapter;
    CustomViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        mMediaContents = getIntent().getParcelableArrayListExtra(EXTRA_MEDIAS);
        int startPosition = getIntent().getIntExtra(EXTRA_SELECTED_POSITION, 0);
        setUpToolbar();
        setUpViewPager(startPosition);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setUpViewPager(int startPosition) {
        mFragmentViewPagerAdapter = new FragmentViewPagerAdapter(getFragmentManager());
        int imagesCount = -1;
        for (Media mediaContent : mMediaContents) {
            ImageFragment fragment = ImageFragment.newInstance(mediaContent);
            mFragmentViewPagerAdapter.addFragment(fragment);
            imagesCount++;
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


        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
