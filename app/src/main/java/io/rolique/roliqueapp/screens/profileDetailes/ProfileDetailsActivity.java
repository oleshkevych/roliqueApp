package io.rolique.roliqueapp.screens.profileDetailes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.util.ui.UiUtil;
import io.rolique.roliqueapp.widget.FloatingActionMenu;
import io.rolique.roliqueapp.widget.profileCategoryCard.ProfileCategoryCard;
import timber.log.Timber;

public class ProfileDetailsActivity extends BaseActivity implements ProfileDetailsContract.View {

    private static String EXTRA_USER = "USER";

    public static Intent startIntent(Context context, User user) {
        Intent intent = new Intent(context, ProfileDetailsActivity.class);
        intent.putExtra(EXTRA_USER, user);
        return intent;
    }

    @BindViews({R.id.profile_card_0,
            R.id.profile_card_1,
            R.id.profile_card_2,
            R.id.profile_card_3,
            R.id.profile_card_4,
            R.id.profile_card_5}) List<ProfileCategoryCard> mProfileCategoryCards;
    @BindArray(R.array.user_data_categories) String[] mCategoriesArray;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.content_profile_header) AppBarLayout mAppBarLayout;

    @BindView(R.id.text_view_user_name) EditText mUserNameTextView;
    @BindView(R.id.image_view) ImageView mImageView;
    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.floating_action_menu) FloatingActionMenu mFloatingActionMenu;

    List<String> mCategories;
    User mUser;
    boolean mIsEditMode;
    float mImageOriginalHeight;
    float mImageOriginalMargin;
    float mImageOriginalWidth;
    float mNameOriginalHeight;
    float mNameOriginalWidth;
    float mContainerOriginalWidth;

    @Inject RoliqueApplicationPreferences mPreferences;
    @Inject ProfileDetailsPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        mUser = getIntent().getParcelableExtra(EXTRA_USER);
        mCategories = new ArrayList<>(Arrays.asList(mCategoriesArray));

        setUpContent(mUser, false);

        for (final ProfileCategoryCard profileCategoryCard : mProfileCategoryCards) {
            profileCategoryCard.setOnActionClickListener(mOnActionClickListener);
            profileCategoryCard.setOnValueChangeListener(new ProfileCategoryCard.OnValueChangeListener() {
                @Override
                public void onValueChanged(String key, String value) {
                    //TODO: update field
                    Timber.d(key + " " + value);
                }
            });
        }

        mPresenter.getUserData(mUser);
        mFloatingActionMenu.setOnItemClickListener(new FloatingActionMenu.OnItemClickListener() {
            @Override
            public void onItemClick(String category) {
                //TODO: create new field in bottom fragment
                showSnackbar(mCoordinatorLayout, category);
            }
        });
        setUpAppBarLayout();
    }

    @Override
    protected void inject() {
        DaggerProfileDetailsComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .profileDetailsPresenterModule(new ProfileDetailsPresenterModule(ProfileDetailsActivity.this))
                .build()
                .inject(ProfileDetailsActivity.this);
    }

    private void setUpContent(final User user, boolean isEditMode) {
        mIsEditMode = isEditMode;
        hideKeyboard();
        mToolbar.setTitle(mIsEditMode ? R.string.activity_profile_details_title_edit : R.string.activity_profile_details_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setUpHeader(mIsEditMode, user);
        for (ProfileCategoryCard card : mProfileCategoryCards)
            card.setIsEditable(mIsEditMode);
        findViewById(R.id.button_edit).setVisibility(mIsEditMode ? View.GONE : View.VISIBLE);
        mFloatingActionMenu.setVisibility(mIsEditMode ? View.VISIBLE : View.GONE);
    }

    private void setUpHeader(boolean isEditMode, final User user) {
        UiUtil.setImage(mImageView, user.getImageUrl());
        setUpUserNameView(mUserNameTextView, user, isEditMode);
    }

    private void setUpUserNameView(final EditText editText, final User user, boolean isEditMode) {
        if (isEditMode) {
            final String userNames = UiUtil.getUserNameForView(user);
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            editText.setTextColor(ContextCompat.getColor(editText.getContext(), R.color.black));
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    String userNames = editText.getText().toString();
                    String firstName = userNames.substring(0, userNames.indexOf(" ")).trim();
                    String lastName = userNames.substring(userNames.indexOf(" "), userNames.length()).trim();
                    if (!b && !(user.getFirstName().equals(firstName) && user.getLastName().equals(lastName))) {
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        //TODO: update user
                        showSnackbar(mCoordinatorLayout, firstName + " " + lastName);
                    }
                }
            });
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editText.setCursorVisible(true);
                }
            });
        } else {
            editText.setTextColor(ContextCompat.getColor(editText.getContext(), R.color.indigo_accent_700));
            editText.setFocusable(false);
            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    editText.selectAll();
                    mOnActionClickListener.onTextSelected(editText.getText().toString(), editText);
                    return true;
                }
            });
        }
        editText.setText(UiUtil.getUserNameForView(user));
        editText.setBackgroundColor(ContextCompat.getColor(ProfileDetailsActivity.this, R.color.transparent));
    }

    private void setUpAppBarLayout() {
        mUserNameTextView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        setUpDimensions();
                        mUserNameTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Timber.e("Offset " + verticalOffset);
                float moving = verticalOffset / mMaxOffsetHeight;
                if (mImageOriginalHeight != 0) {
                    ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
                    layoutParams.height = Math.round((1 - moving) * (mImageOriginalHeight - mNameOriginalHeight) + mNameOriginalHeight);
                    layoutParams.width = Math.round((1 - moving) * (mImageOriginalWidth - mNameOriginalHeight) + mNameOriginalHeight);
                    mImageView.setLayoutParams(layoutParams);
                }

                mImageView.setTranslationY((moving) * mNameOriginalHeight + (mImageOriginalHeight - mImageView.getHeight()) / 2);
                mImageView.setTranslationX(mContainerOriginalWidth / 2 - mImageOriginalWidth / 2 - moving * (mContainerOriginalWidth / 2 - mImageOriginalWidth / 2 + (mImageOriginalWidth - mImageView.getWidth())));

                mUserNameTextView.setTranslationX(mContainerOriginalWidth / 2 - mUserNameTextView.getWidth() / 2 - moving * (mContainerOriginalWidth / 2 - mUserNameTextView.getWidth() / 2 - (mImageView.getWidth() + mImageOriginalMargin)));
            }
        });
    }

    float mMaxOffsetHeight = 1;

    private void setUpDimensions() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        mImageOriginalMargin = params.topMargin;
        mImageOriginalHeight = mImageView.getMeasuredHeight();
        mImageOriginalWidth = mImageView.getMeasuredWidth();
        mNameOriginalHeight = mUserNameTextView.getMeasuredHeight();
        mNameOriginalWidth = mUserNameTextView.getMeasuredWidth();
        mContainerOriginalWidth = mAppBarLayout.getMeasuredWidth();
        mMaxOffsetHeight = findViewById(R.id.container_collapsed_layout).getMinimumHeight() - findViewById(R.id.container_collapsed_layout).getMeasuredHeight();
    }

    ProfileCategoryCard.OnActionClickListener mOnActionClickListener = new ProfileCategoryCard.OnActionClickListener() {
        @Override
        public void onCall(String number) {
            //TODO: add intent call
            showSnackbar(mCoordinatorLayout, number);
//                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
//                    startActivity(intent);
        }

        @Override
        public void onLinkOpen(String url) {
            //TODO: add intent open link
            showSnackbar(mCoordinatorLayout, url);
        }

        @Override
        public void onTextSelected(String text, final EditText editText) {
            //TODO: add copy/share bottom fragment
            showSnackbar(mCoordinatorLayout, text);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    editText.setSelection(0, 0);
                }
            }, 1000);
        }
    };

    @OnClick(R.id.button_edit)
    void onEditClick() {
        setUpContent(mUser, true);
    }

    @Override
    public void showValuesInView(String category, List<Pair<String, String>> pairs) {
        int index = mCategories.indexOf(category);
        mProfileCategoryCards.get(index).setValues(pairs);
    }

    @Override
    public void showErrorInView(String message) {
        showSnackbar(message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    protected void onStop() {
        mPresenter.stop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mIsEditMode) {
            setUpContent(mUser, false);
        } else {
            super.onBackPressed();
        }
    }
}
