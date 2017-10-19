package io.rolique.cameralibrary.screens.videoViewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 10/10/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class VideoViewerActivity extends AppCompatActivity {

    private static final String EXTRA_VIDEOS = "VIDEOS";
    private static final String EXTRA_SELECTED_VIDEO_POSITION = "SELECTED_VIDEO_POSITION";

    public static Intent getStartIntent(Context context, String videoPath) {
        Intent intent = new Intent(context, VideoViewerActivity.class);
        intent.putStringArrayListExtra(EXTRA_VIDEOS, new ArrayList<>(Arrays.asList(new String[]{videoPath})));
        return intent;
    }

    public static Intent getStartIntent(Context context, List<String> videoPaths, int position) {
        Intent intent = new Intent(context, VideoViewerActivity.class);
        intent.putStringArrayListExtra(EXTRA_VIDEOS, new ArrayList<>(videoPaths));
        intent.putExtra(EXTRA_SELECTED_VIDEO_POSITION, position);
        return intent;
    }

    SimpleExoPlayerView mPlayerView;
    SimpleExoPlayer mPlayer;
    DefaultBandwidthMeter mBandWithMeter;

    int mPosition;
    boolean mIsTrackChanged;
    List<String> mPaths = new ArrayList<>();
    List<MediaSource> mPlaylist = new ArrayList<>();
    Point mDisplaySize;
    long mLastPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);
        mPlayerView = findViewById(R.id.player_view);
        mDisplaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
        ActivityCompat.postponeEnterTransition(VideoViewerActivity.this);
        mPaths = getIntent().getStringArrayListExtra(EXTRA_VIDEOS);
        mPosition = getIntent().getIntExtra(EXTRA_SELECTED_VIDEO_POSITION, 0);
        mBandWithMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(VideoViewerActivity.this,
                Util.getUserAgent(VideoViewerActivity.this, getString(R.string.app_name)));
        for (String path : mPaths) {
            Uri videoUri = Uri.parse(path);
            MediaSource mediaSource = new ExtractorMediaSource(videoUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);
            mPlaylist.add(mediaSource);
        }
        findViewById(R.id.image_view_play).setOnClickListener(mOnPlayClickListener);
    }

    View.OnClickListener mOnPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.setPlayWhenReady(true);
        }
    };

    private void initializePlayer() {
        AdaptiveTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(mBandWithMeter);
        mPlayer = ExoPlayerFactory.newSimpleInstance(
                VideoViewerActivity.this, new DefaultTrackSelector(videoTrackSelectionFactory));
        mPlayer.setVolume(1F);
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(mPlaylist.toArray(new MediaSource[mPlaylist.size()]));
        mPlayer.prepare(concatenatedSource);

        mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        mPlayer.seekToDefaultPosition(mPosition);
        mPlayer.addListener(mPlayerEventListener);
        mPlayerView.setPlayer(mPlayer);
    }

    private ExoPlayer.EventListener mPlayerEventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            mIsTrackChanged = true;
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            findViewById(R.id.image_view_play).setVisibility(playWhenReady ? View.GONE : View.VISIBLE);
            if (playbackState == ExoPlayer.STATE_ENDED) {
                mPlayer.stop();
                release();
                initializePlayer();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Timber.e(error);
        }

        @Override
        public void onPositionDiscontinuity() {
            if (!mIsTrackChanged || mPosition == mPlayer.getCurrentPeriodIndex()) return;
            mIsTrackChanged = false;
            mPosition = mPlayer.getCurrentPeriodIndex();
            mPlayer.setPlayWhenReady(false);
            mPlayer.stop();
            release();
            initializePlayer();
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
            initializePlayer();
            mPlayer.seekTo(mLastPosition);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
            mPlayer.seekTo(mLastPosition);
        }
    }

    @Override
    public void onPause() {
        if (Util.SDK_INT <= 23) {
            mLastPosition = mPlayer.getCurrentPosition();
            release();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (Util.SDK_INT > 23) {
            mLastPosition = mPlayer.getCurrentPosition();
            release();
        }
        super.onStop();
    }

    private void release() {
        mPlayer.removeListener(mPlayerEventListener);
        mPlayer.release();
    }
}
