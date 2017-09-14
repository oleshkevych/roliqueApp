package io.rolique.roliqueapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.screens.navigation.contacts.adapter.UsersAdapter;


/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@CoordinatorLayout.DefaultBehavior(FloatingActionMenu.Behavior.class)
public class FloatingActionMenu extends CoordinatorLayout {

    FloatingActionButton mOptionsToggleActionButton;
    FloatingActionButton mFirstOptionActionButton;
    FloatingActionButton mSecondOptionActionButton;
    FloatingActionButton mThirdOptionActionButton;
    FloatingActionButton mForthOptionActionButton;
    FloatingActionButton mFifthOptionActionButton;
    FloatingActionButton mSixthOptionActionButton;
    TextView mFirstOptionTextView;
    TextView mSecondOptionTextView;
    TextView mThirdOptionTextView;
    TextView mForthOptionTextView;
    TextView mFifthOptionTextView;
    TextView mSixthOptionTextView;
    boolean mIsOptionVisible;
    boolean mIsToggleDisable;

    public interface OnItemClickListener {
        void onItemClick(String category);
    }

    OnItemClickListener mOnItemClickListener;

    public FloatingActionMenu(Context context) {
        this(context, null);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.content_floating_action_menu, FloatingActionMenu.this);
        mOptionsToggleActionButton = (FloatingActionButton) findViewById(R.id.floating_button_toggle_options);
        mFirstOptionActionButton = (FloatingActionButton) findViewById(R.id.floating_button_first_option);
        mSecondOptionActionButton = (FloatingActionButton) findViewById(R.id.floating_button_second_option);
        mThirdOptionActionButton = (FloatingActionButton) findViewById(R.id.floating_button_third_option);
        mForthOptionActionButton = (FloatingActionButton) findViewById(R.id.floating_button_forth_option);
        mFifthOptionActionButton = (FloatingActionButton) findViewById(R.id.floating_button_fifth_option);
        mSixthOptionActionButton = (FloatingActionButton) findViewById(R.id.floating_button_sixth_option);

        mFirstOptionTextView = (TextView) findViewById(R.id.text_view_first_option);
        mSecondOptionTextView = (TextView) findViewById(R.id.text_view_second_option);
        mThirdOptionTextView = (TextView) findViewById(R.id.text_view_third_option);
        mForthOptionTextView = (TextView) findViewById(R.id.text_view_forth_option);
        mFifthOptionTextView = (TextView) findViewById(R.id.text_view_fifth_option);
        mSixthOptionTextView = (TextView) findViewById(R.id.text_view_sixth_option);

        final TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.FloatingActionMenu, defStyleAttr, defStyleRes);
        mIsToggleDisable = typedArray.getBoolean(R.styleable.FloatingActionMenu_fam_overrideDefaultActionOptionButton, false);
        if (mIsToggleDisable) {
            setToggleButtonIcon(typedArray.getResourceId(R.styleable.FloatingActionMenu_fam_optionButtonIcon, 0));
            mFirstOptionActionButton.setVisibility(GONE);
            mSecondOptionActionButton.setVisibility(GONE);
            mThirdOptionActionButton.setVisibility(GONE);
            mForthOptionActionButton.setVisibility(GONE);
            mFifthOptionActionButton.setVisibility(GONE);
            mSixthOptionActionButton.setVisibility(GONE);
        } else {
            mOptionsToggleActionButton.setOnClickListener(onOptionsButtonClick);
            setFirstOptionIcon(typedArray.getResourceId(R.styleable.FloatingActionMenu_fam_firstOptionIcon, 0));
            setSecondOptionIcon(typedArray.getResourceId(R.styleable.FloatingActionMenu_fam_secondOptionIcon, 0));
            setThirdOptionIcon(typedArray.getResourceId(R.styleable.FloatingActionMenu_fam_thirdOptionIcon, 0));
            setForthOptionIcon(typedArray.getResourceId(R.styleable.FloatingActionMenu_fam_forthOptionIcon, 0));
            setFifthOptionIcon(typedArray.getResourceId(R.styleable.FloatingActionMenu_fam_fifthOptionIcon, 0));
            setSixthOptionIcon(typedArray.getResourceId(R.styleable.FloatingActionMenu_fam_sixthOptionIcon, 0));
            setFirstOptionText(typedArray.getString(R.styleable.FloatingActionMenu_fam_firstOptionName));
            setSecondOptionText(typedArray.getString(R.styleable.FloatingActionMenu_fam_secondOptionName));
            setThiirdOptionText(typedArray.getString(R.styleable.FloatingActionMenu_fam_thirdOptionName));
            setForthOptionText(typedArray.getString(R.styleable.FloatingActionMenu_fam_forthOptionName));
            setFifthOptionText(typedArray.getString(R.styleable.FloatingActionMenu_fam_fifthOptionName));
            setSixthOptionText(typedArray.getString(R.styleable.FloatingActionMenu_fam_sixthOptionName));
        }
        typedArray.recycle();
    }

    void setToggleButtonIcon(@DrawableRes int resourceId) {
        if (resourceId > 0)
            mOptionsToggleActionButton.setImageDrawable(AppCompatResources.getDrawable(mOptionsToggleActionButton.getContext(), resourceId));
    }

    public void setFirstOptionIcon(@DrawableRes int resourceId) {
        if (resourceId > 0)
            mFirstOptionActionButton.setImageDrawable(AppCompatResources.getDrawable(mFirstOptionActionButton.getContext(), resourceId));
    }

    public void setSecondOptionIcon(@DrawableRes int resourceId) {
        if (resourceId > 0)
            mSecondOptionActionButton.setImageDrawable(AppCompatResources.getDrawable(mSecondOptionActionButton.getContext(), resourceId));
    }

    public void setThirdOptionIcon(@DrawableRes int resourceId) {
        if (resourceId > 0)
            mThirdOptionActionButton.setImageDrawable(AppCompatResources.getDrawable(mThirdOptionActionButton.getContext(), resourceId));
    }

    public void setForthOptionIcon(@DrawableRes int resourceId) {
        if (resourceId > 0)
            mForthOptionActionButton.setImageDrawable(AppCompatResources.getDrawable(mSecondOptionActionButton.getContext(), resourceId));
    }

    public void setFifthOptionIcon(@DrawableRes int resourceId) {
        if (resourceId > 0)
            mFifthOptionActionButton.setImageDrawable(AppCompatResources.getDrawable(mSecondOptionActionButton.getContext(), resourceId));
    }

    public void setSixthOptionIcon(@DrawableRes int resourceId) {
        if (resourceId > 0)
            mSixthOptionActionButton.setImageDrawable(AppCompatResources.getDrawable(mSecondOptionActionButton.getContext(), resourceId));
    }

    public void setFirstOptionText(String text) {
        if (text == null) {
            mFirstOptionTextView.setVisibility(GONE);
            mFirstOptionActionButton.setVisibility(GONE);
        } else {
            TextView textView = (TextView) findViewById(R.id.text_view_first_option);
            textView.setText(text);
        }
    }

    public void setSecondOptionText(String text) {
        if (text == null) {
            mSecondOptionTextView.setVisibility(GONE);
            mSecondOptionActionButton.setVisibility(GONE);
        } else {
            TextView textView = (TextView) findViewById(R.id.text_view_second_option);
            textView.setText(text);
        }
    }

    public void setThiirdOptionText(String text) {
        if (text == null) {
            mThirdOptionTextView.setVisibility(GONE);
            mThirdOptionActionButton.setVisibility(GONE);
        } else {
            TextView textView = (TextView) findViewById(R.id.text_view_third_option);
            textView.setText(text);
        }
    }

    public void setForthOptionText(String text) {
        if (text == null) {
            mForthOptionTextView.setVisibility(GONE);
            mForthOptionActionButton.setVisibility(GONE);
        } else {
            TextView textView = (TextView) findViewById(R.id.text_view_forth_option);
            textView.setText(text);
        }
    }

    public void setFifthOptionText(String text) {
        if (text == null) {
            mFifthOptionTextView.setVisibility(GONE);
            mFifthOptionActionButton.setVisibility(GONE);
        } else {
            TextView textView = (TextView) findViewById(R.id.text_view_fifth_option);
            textView.setText(text);
        }
    }

    public void setSixthOptionText(String text) {
        if (text == null) {
            mSixthOptionTextView.setVisibility(GONE);
            mSixthOptionActionButton.setVisibility(GONE);
        } else {
            TextView textView = (TextView) findViewById(R.id.text_view_sixth_option);
            textView.setText(text);
        }
    }

    public void setOnFirstOptionClickListener(OnClickListener onClickListener) {
        mFirstOptionActionButton.setOnClickListener(onClickListener);
    }

    public void setOnSecondOptionClickListener(OnClickListener onClickListener) {
        mSecondOptionActionButton.setOnClickListener(onClickListener);
    }

    public void setOnThirdOptionClickListener(OnClickListener onClickListener) {
        mThirdOptionActionButton.setOnClickListener(onClickListener);
    }

    public void setOnForthOptionClickListener(OnClickListener onClickListener) {
        mForthOptionActionButton.setOnClickListener(onClickListener);
    }

    public void setOnFifthOptionClickListener(OnClickListener onClickListener) {
        mFifthOptionActionButton.setOnClickListener(onClickListener);
    }

    public void setOnSixthOptionClickListener(OnClickListener onClickListener) {
        mSixthOptionActionButton.setOnClickListener(onClickListener);
    }

    public void setEnabled(boolean isButtonEnable){
        mOptionsToggleActionButton.setEnabled(isButtonEnable);
        mOptionsToggleActionButton.setAlpha(isButtonEnable ? 1f : 0.5f);
    }

    OnClickListener onOptionsButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleOptions();
        }
    };

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mFirstOptionActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(mFirstOptionTextView.getText().toString());
                hideOptions();
            }
        });
        mSecondOptionActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(mSecondOptionTextView.getText().toString());
                hideOptions();
            }
        });
        mThirdOptionActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(mThirdOptionTextView.getText().toString());
                hideOptions();
            }
        });
        mForthOptionActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(mForthOptionTextView.getText().toString());
                hideOptions();
            }
        });
        mFifthOptionActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(mFifthOptionTextView.getText().toString());
                hideOptions();
            }
        });
        mSixthOptionActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(mSixthOptionTextView.getText().toString());
                hideOptions();
            }
        });
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void setVisibility(int visibility) {
        mOptionsToggleActionButton.setVisibility(visibility);
        mFirstOptionActionButton.setVisibility(visibility);
        mSecondOptionActionButton.setVisibility(visibility);
        mThirdOptionActionButton.setVisibility(visibility);
        mForthOptionActionButton.setVisibility(visibility);
        mFifthOptionActionButton.setVisibility(visibility);
        mSixthOptionActionButton.setVisibility(visibility);
    }

    private void toggleOptions() {
        if (mIsOptionVisible) {
            hideOptions();
        } else {
            showOptions();
        }
    }

    private void showOptions() {
        mIsOptionVisible = true;
        mOptionsToggleActionButton.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.ic_clear_white_16dp));
        animateView(mFirstOptionTextView, R.anim.text_view_first_option_show, 1.5);
        animateView(mFirstOptionActionButton, R.anim.fab_first_option_show, 1.5);
        animateView(mSecondOptionTextView, R.anim.text_view_second_option_show, 2.8);
        animateView(mSecondOptionActionButton, R.anim.fab_second_option_show, 2.8);
        animateView(mThirdOptionTextView, R.anim.text_view_third_option_show, 4.3);
        animateView(mThirdOptionActionButton, R.anim.fab_third_option_show, 4.3);
        animateView(mForthOptionTextView, R.anim.text_view_forth_option_show, 5.8);
        animateView(mForthOptionActionButton, R.anim.fab_forth_option_show, 5.8);
        animateView(mFifthOptionTextView, R.anim.text_view_fifth_option_show, 7.3);
        animateView(mFifthOptionActionButton, R.anim.fab_fifth_option_show, 7.3);
        animateView(mSixthOptionTextView, R.anim.text_view_sixth_option_show, 8.8);
        animateView(mSixthOptionActionButton, R.anim.fab_sixth_option_show, 8.8);
    }

    public void hideOptions() {
        if (mIsOptionVisible) {
            mIsOptionVisible = false;
            mOptionsToggleActionButton.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.ic_add_white_16dp));
            animateView(mFirstOptionTextView, R.anim.text_view_first_option_hide, 1.5);
            animateView(mFirstOptionActionButton, R.anim.fab_first_option_hide, 1.5);
            animateView(mSecondOptionTextView, R.anim.text_view_second_option_hide, 2.8);
            animateView(mSecondOptionActionButton, R.anim.fab_second_option_hide, 2.8);
            animateView(mThirdOptionTextView, R.anim.text_view_third_option_hide, 4.3);
            animateView(mThirdOptionActionButton, R.anim.fab_third_option_hide, 4.3);
            animateView(mForthOptionTextView, R.anim.text_view_forth_option_hide, 5.8);
            animateView(mForthOptionActionButton, R.anim.fab_forth_option_hide, 5.8);
            animateView(mFifthOptionTextView, R.anim.text_view_fifth_option_hide, 7.3);
            animateView(mFifthOptionActionButton, R.anim.fab_fifth_option_hide, 7.3);
            animateView(mSixthOptionTextView, R.anim.text_view_sixth_option_hide, 8.8);
            animateView(mSixthOptionActionButton, R.anim.fab_sixth_option_hide, 8.8);
        }
    }

    private void animateView(View view, @AnimRes int res, double offsetMultiplier) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), res);
        if (view instanceof FloatingActionButton) {
            CoordinatorLayout.LayoutParams secondOptionLayoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            int offset = (int) (view.getHeight() * offsetMultiplier);
            secondOptionLayoutParams.bottomMargin += mIsOptionVisible ? offset : -1 * offset;
            view.setLayoutParams(secondOptionLayoutParams);
        }
        view.startAnimation(animation);
    }



    public FloatingActionButton getOptionsToggleActionButton() {
        return mOptionsToggleActionButton;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        mOptionsToggleActionButton.setOnClickListener(listener);
    }

    public static class Behavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {

        private boolean mIsPivotInited;
        private boolean mIsAnchoredToAppBarLayout;

        public Behavior() {
            super();
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
            if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
                // If the developer hasn't set dodgeInsetEdges, lets set it to BOTTOM so that
                // we dodge any Snackbars
                lp.dodgeInsetEdges = Gravity.BOTTOM;
            }
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionMenu child,
                                     int layoutDirection) {
            // Now let the CoordinatorLayout lay out the FAB
            parent.onLayoutChild(child, layoutDirection);
            return true;
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout || dependency instanceof AppBarLayout;
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child,
                                   View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//            if (dyConsumed > 0) {
//                child.hideOptions();
//                child.getOptionsToggleActionButton().hide();
//            } else if (dyConsumed < 0) {
//                child.getOptionsToggleActionButton().show();
//            }
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionMenu child,
                                           View directTargetChild, View target, int nestedScrollAxes) {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL && !mIsAnchoredToAppBarLayout;
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
//            if (dependency instanceof Snackbar.SnackbarLayout) {
//                updateFabTranslationForSnackbar(child, dependency);
//                return false;
//            }
//            if (dependency instanceof AppBarLayout) {
//                mIsAnchoredToAppBarLayout = true;
//                updateFabVisibility(parent, (AppBarLayout) dependency, child);
//            }
            return false;
        }

//        private void updateFabTranslationForSnackbar(FloatingActionMenu child, View dependency) {
//            float height = dependency.getHeight();
//            float translationY = Math.min(0, height);
//            ViewCompat.setTranslationY(child, translationY);
//        }

//        private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, FloatingActionMenu child) {
//
//            if(!mIsPivotInited)
//                initPivots(child);
//
//            try {
//                float appBarLayoutHeight = appBarLayout.getMeasuredHeight();
//                float appBarLayoutTop = appBarLayout.getTop();
//
//                if (appBarLayoutHeight != Math.abs(appBarLayoutTop)) {
//                    child.animate()
//                            .scaleX((appBarLayoutHeight - Math.abs(appBarLayoutTop))/appBarLayoutHeight)
//                            .scaleY((appBarLayoutHeight - Math.abs(appBarLayoutTop))/appBarLayoutHeight)
//                            .setDuration(0)
//                            .start();
//                }
//                if (appBarLayoutHeight == Math.abs(appBarLayoutTop)) {
//                    child.hideOptions();
//                    child.getOptionsToggleActionButton().hide();
//                } else if (Math.abs(appBarLayoutTop) < appBarLayoutHeight) {
//                    child.getOptionsToggleActionButton().show();
//                }
//                return false;
//            } catch (Exception e) {
//                return false;
//            }
//        }

//        private void initPivots(FloatingActionMenu actionMenu) {
//            mIsPivotInited = true;
//            int mHeight = actionMenu.getMeasuredHeight();
//            int mWidth = actionMenu.getMeasuredWidth();
//            View child = actionMenu.getChildAt(actionMenu.getChildCount() - 1);
//            int margins = mHeight - child.getBottom();
//            int childHeight = child.getMeasuredHeight();
//            int childWidth = child.getMeasuredWidth();
//            actionMenu.setPivotX(mWidth - margins - childWidth/2);
//            actionMenu.setPivotY(mHeight - margins - childHeight/2);
//        }
    }
}
