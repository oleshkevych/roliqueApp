package io.rolique.roliqueapp.screens.profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import io.rolique.roliqueapp.widget.AddPickerDialog;
import io.rolique.roliqueapp.widget.FloatingActionMenu;
import io.rolique.roliqueapp.widget.KeyboardEditText;
import io.rolique.roliqueapp.widget.SelectPickerDialog;
import io.rolique.roliqueapp.widget.ProfileCategoryCard;

public class ProfileActivity extends BaseActivity implements ProfileContract.View {

    private static String EXTRA_USER = "USER";

    public static Intent startIntent(Context context, User user) {
        Intent intent = new Intent(context, ProfileActivity.class);
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
    @BindView(R.id.layout_root) ViewGroup mRootViewGroup;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.content_profile_header) AppBarLayout mAppBarLayout;
    @BindView(R.id.text_view_user_name) KeyboardEditText mUserNameEditText;
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
    float mMaxOffsetHeight = 1;
    long mTimeShowOptionsStart;

    @Inject RoliqueApplicationPreferences mPreferences;
    @Inject ProfilePresenter mPresenter;

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
                public void onValueChanged(String category, String key, String value) {
                    mPresenter.setNewValue(mUser, category, key, value);
                }
            });
            profileCategoryCard.setOnKeyboardListener(mOnKeyboardListener);
        }

        mPresenter.getUserData(mUser);
        setUpFloatingActionMenu();
        setUpAppBarLayout();
    }

    @Override
    protected void inject() {
        DaggerProfileComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .profilePresenterModule(new ProfilePresenterModule(ProfileActivity.this))
                .build()
                .inject(ProfileActivity.this);
    }

    private void setUpContent(final User user, boolean isEditMode) {
        mIsEditMode = isEditMode;
        hideKeyboard();
        mToolbar.setTitle(mIsEditMode ? R.string.activity_profile_details_title_edit : R.string.activity_profile_details_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFloatingActionMenu.hideOptions();
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
        setUpUserNameView(mUserNameEditText, user, isEditMode);
    }

    private void setUpUserNameView(final KeyboardEditText editText, final User user, boolean isEditMode) {
        if (isEditMode) {
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
                    if (mOnKeyboardListener != null && b) mOnKeyboardListener.isKeyboardShown(true);
                    if (!b && !(user.getFirstName().equals(firstName) && user.getLastName().equals(lastName))) {
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        mPresenter.updateUser(user);
                    }
                }
            });
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnKeyboardListener != null)
                        mOnKeyboardListener.isKeyboardShown(true);
                    editText.setCursorVisible(true);
                }
            });
            editText.setOnKeyboardChangeListener(new KeyboardEditText.OnKeyboardChangeListener() {
                @Override
                public void onKeyboardStateChanged(boolean isVisible) {
                    if (mOnKeyboardListener != null)
                        mOnKeyboardListener.isKeyboardShown(isVisible);
                }
            });
        } else {
            editText.setTextColor(ContextCompat.getColor(editText.getContext(), R.color.indigo_accent_700));
            editText.setFocusable(false);
            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    editText.selectAll();
                    mOnActionClickListener.onTextSelected(editText.getText().toString(), editText, "");
                    return true;
                }
            });
        }
        editText.setText(UiUtil.getUserNameForView(user));
        editText.setBackgroundColor(ContextCompat.getColor(ProfileActivity.this, R.color.transparent));
    }

    private void setUpAppBarLayout() {
        mUserNameEditText.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        setUpDimensions();
                        mUserNameEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (mTimeShowOptionsStart + 600 <= new Date().getTime() && verticalOffset == 0)
                    mFloatingActionMenu.hideOptions();
                float moving = verticalOffset / mMaxOffsetHeight;
                if (mImageOriginalHeight != 0) {
                    ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
                    layoutParams.height = Math.round((1 - moving) * (mImageOriginalHeight - mNameOriginalHeight) + mNameOriginalHeight);
                    layoutParams.width = Math.round((1 - moving) * (mImageOriginalWidth - mNameOriginalHeight) + mNameOriginalHeight);
                    mImageView.setLayoutParams(layoutParams);
                }

                mImageView.setTranslationY((moving) * mNameOriginalHeight + (mImageOriginalHeight - mImageView.getHeight()) / 2);
                mImageView.setTranslationX(mContainerOriginalWidth / 2 - mImageOriginalWidth / 2 - moving * (mContainerOriginalWidth / 2 - mImageOriginalWidth / 2 + (mImageOriginalWidth - mImageView.getWidth())));

                mUserNameEditText.setTranslationX(mContainerOriginalWidth / 2 - mUserNameEditText.getWidth() / 2 - moving * (mContainerOriginalWidth / 2 - mUserNameEditText.getWidth() / 2 - (mImageView.getWidth() + mImageOriginalMargin)));
            }
        });
    }

    private void setUpDimensions() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mImageView.getLayoutParams();
        mImageOriginalMargin = params.topMargin;
        mImageOriginalHeight = mImageView.getMeasuredHeight();
        mImageOriginalWidth = mImageView.getMeasuredWidth();
        mNameOriginalHeight = mUserNameEditText.getMeasuredHeight();
        mNameOriginalWidth = mUserNameEditText.getMeasuredWidth();
        mContainerOriginalWidth = mAppBarLayout.getMeasuredWidth();
        mMaxOffsetHeight = findViewById(R.id.container_collapsed_layout).getMinimumHeight() - findViewById(R.id.container_collapsed_layout).getMeasuredHeight();
    }

    ProfileCategoryCard.OnActionClickListener mOnActionClickListener = new ProfileCategoryCard.OnActionClickListener() {
        @Override
        public void onCall(final String number) {
            SelectPickerDialog selectPickerDialog = SelectPickerDialog.newInstance(SelectPickerDialog.PHONE_NUMBER);
            selectPickerDialog.show(getSupportFragmentManager(), selectPickerDialog.getClass().getName());
            selectPickerDialog.setOnPickListener(new SelectPickerDialog.OnPickListener() {

                @Override
                public void onCallClick(SelectPickerDialog dialog) {
                    dialog.dismiss();
                    startCall(number);
                }

                @Override
                public void onOpenLinkClick(SelectPickerDialog dialog) {
                }

                @Override
                public void onCopyClick(SelectPickerDialog dialog) {
                }

                @Override
                public void onShareClick(SelectPickerDialog dialog) {
                }

                @Override
                public void onCancelSelection() {
                }
            });
        }

        @Override
        public void onLinkOpen(final String url) {
            SelectPickerDialog selectPickerDialog = SelectPickerDialog.newInstance(SelectPickerDialog.LINK);
            selectPickerDialog.show(getSupportFragmentManager(), selectPickerDialog.getClass().getName());
            selectPickerDialog.setOnPickListener(new SelectPickerDialog.OnPickListener() {

                @Override
                public void onCallClick(SelectPickerDialog dialog) {
                }

                @Override
                public void onOpenLinkClick(SelectPickerDialog dialog) {
                    dialog.dismiss();
                    startWebView(url);
                }

                @Override
                public void onCopyClick(SelectPickerDialog dialog) {
                }

                @Override
                public void onShareClick(SelectPickerDialog dialog) {
                }

                @Override
                public void onCancelSelection() {
                }
            });
        }

        @Override
        public void onTextSelected(final String text, final EditText editText, String category) {
            SelectPickerDialog selectPickerDialog = getPickerIntent(category);
            selectPickerDialog.show(getSupportFragmentManager(), selectPickerDialog.getClass().getName());
            selectPickerDialog.setOnPickListener(new SelectPickerDialog.OnPickListener() {

                @Override
                public void onCallClick(SelectPickerDialog dialog) {
                    dialog.dismiss();
                    startCall(text);
                }

                @Override
                public void onOpenLinkClick(SelectPickerDialog dialog) {
                    dialog.dismiss();
                    startWebView(text);
                }

                @Override
                public void onCopyClick(SelectPickerDialog dialog) {
                    dialog.dismiss();
                    copyToBuffer(text);
                }

                @Override
                public void onShareClick(SelectPickerDialog dialog) {
                    dialog.dismiss();
                    shareText(text);
                }

                @Override
                public void onCancelSelection() {
                    editText.setSelection(0, 0);
                }
            });
        }

        @Override
        public void onDeleteClick(String category, String key) {
            mPresenter.removeValue(mUser, category, key);
        }

        @Override
        public void onRemoveCategory(String category) {
            mPresenter.removeCategory(mUser, category);
        }
    };

    private SelectPickerDialog getPickerIntent(String category) {
        if (category.equals(mCategoriesArray[2]) || category.equals(mCategoriesArray[3]) || category.equals(mCategoriesArray[5]))
           return SelectPickerDialog.newInstance(SelectPickerDialog.LINK_AND_SELECTION);
        if (category.equals(mCategoriesArray[0]))
            return SelectPickerDialog.newInstance(SelectPickerDialog.PHONE_NUMBER_AND_SELECTION);
        return SelectPickerDialog.newInstance();
    }

    private void startCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
        startActivity(intent);
    }

    private void startWebView(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void copyToBuffer(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied text: ", text);
        clipboard.setPrimaryClip(clip);
        showSnackbar(mCoordinatorLayout, "Copied to clipboard");
    }

    private void shareText(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    ProfileCategoryCard.OnKeyboardChangeListener mOnKeyboardListener = new ProfileCategoryCard.OnKeyboardChangeListener() {
        @Override
        public void isKeyboardShown(boolean isShown) {
            if(isShown) onShowKeyboard();
            else onHideKeyboard();
        }
    };

    private void setUpFloatingActionMenu() {
        mFloatingActionMenu.setOnItemClickListener(new FloatingActionMenu.OnItemClickListener() {
            @Override
            public void onItemClick(String category) {
                AddPickerDialog addPickerDialog = AddPickerDialog.newInstance(category);
                addPickerDialog.show(getSupportFragmentManager(), addPickerDialog.getClass().getName());
                addPickerDialog.setOnPickListener(new AddPickerDialog.OnPickListener() {
                    @Override
                    public void onSaveClick(AddPickerDialog dialog, String category, String key, String value) {
                        dialog.dismiss();
                        mPresenter.setNewValue(mUser, category, key, value);
                    }

                    @Override
                    public void onCancelLinkClick(AddPickerDialog dialog) {
                        dialog.dismiss();
                        hideKeyboard();
                    }
                });
            }
        });
        mFloatingActionMenu.setOnToggleListener(new FloatingActionMenu.OnToggleListener() {
            @Override
            public void onToggleVisibility(boolean isOptionsVisible) {
                mTimeShowOptionsStart = new Date().getTime();
                mAppBarLayout.setExpanded(!isOptionsVisible, true);
            }
        });
    }

    private void onShowKeyboard() {
        mAppBarLayout.setExpanded(false, true);
        mFloatingActionMenu.hideOptions();
        mFloatingActionMenu.setEnabled(false);
    }
    private void onHideKeyboard() {
        mFloatingActionMenu.setEnabled(true);
    }

    @OnClick(R.id.button_edit)
    void onEditClick() {
        mFloatingActionMenu.setEnabled(true);
        setUpContent(mUser, true);
    }

    @Override
    public void showValuesInView(String category, List<Pair<String, String>> pairs) {
        int index = mCategories.indexOf(category);
        mProfileCategoryCards.get(index).setValues(pairs);
    }

    @Override
    public void showRemoveCategoryInView(String category) {
        int index = mCategories.indexOf(category);
        mProfileCategoryCards.get(index).cleanView();
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
        if (mFloatingActionMenu.isOptionVisible()) {
            mFloatingActionMenu.hideOptions();
        } else if (mIsEditMode) {
            setUpContent(mUser, false);
        } else {
            super.onBackPressed();
        }
    }
}
