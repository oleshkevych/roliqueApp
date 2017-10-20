package io.rolique.roliqueapp.screens.welcome.fragments.signUp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;
import io.rolique.roliqueapp.util.ui.UiUtil;

public class SignUpFragment extends BaseFragment implements SignUpContract.View {

    public static Fragment newInstance() {
        return new SignUpFragment();
    }

    private final String DEFAULT_MAIL = "@rolique.io";

    @Inject SignUpPresenter mPresenter;

    // UI references.
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.view_switcher) ViewSwitcher mViewSwitcher;
    @BindView(R.id.edit_text_email_sign_up) EditText mEmailSignUpEditText;
    @BindView(R.id.edit_text_first_name_sign_up) EditText mFirstNameEditText;
    @BindView(R.id.edit_text_last_name_sign_up) EditText mLastNameEditText;
    @BindView(R.id.edit_text_password_sign_up) EditText mPasswordSignUpEditText;
    @BindView(R.id.edit_text_confirm_password_sign_up) EditText mConfirmPasswordSignUpEditText;

    MediaLib mMediaLib;
    String mImagePath = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMediaLib = new MediaLib(getActivity(), mMediaLibListener);
        mMediaLib.setFrontCamera(true);
        mMediaLib.setRotation(true);
        mMediaLib.setSinglePhoto(true);
        mMediaLib.setSelectableFlash(true);
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    MediaLib.MediaLibListener mMediaLibListener = new MediaLib.MediaLibListener() {
        @Override
        public void onSuccess(List<MediaContent> mediaContents) {
            mImagePath = mediaContents.get(0).getImage();
            UiUtil.setImageIfExists(mViewSwitcher, mImagePath, "", 88);
        }

        @Override
        public void onEmpty() {

        }

        @Override
        public void onError(Exception e) {

        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();
        UiUtil.setImageIfExists(mViewSwitcher, mImagePath, "", 88);
        mConfirmPasswordSignUpEditText.setOnEditorActionListener(mOnEditorActionListener);
        mEmailSignUpEditText.setOnFocusChangeListener(mOnFocusChangeListener);

        mLastNameEditText.addTextChangedListener(mOnNameEditorActionListener);
        mFirstNameEditText.addTextChangedListener(mOnNameEditorActionListener);
    }

    private void setUpToolbar() {
        mToolbar.setTitle(R.string.activity_sign_up_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    protected void inject() {
        DaggerSignUpComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getActivity().getApplication()).getRepositoryComponent())
                .signUpPresenterModule(new SignUpPresenterModule(SignUpFragment.this))
                .build()
                .inject(SignUpFragment.this);
    }

    TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        }
    };

    TextView.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            EditText editText = (EditText) view;
            if (b) {
                mEmailSignUpEditText.addTextChangedListener(mOnEmailSignUpEditorActionListener);
                String s = getInputText(editText);
                if (!s.isEmpty() && s.contains("@")) {
                    s = s.substring(0, s.indexOf("@"));
                    editText.setText(s);
                }
            } else {
                mEmailSignUpEditText.removeTextChangedListener(mOnEmailSignUpEditorActionListener);
            }
        }
    };

    TextWatcher mOnNameEditorActionListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mImagePath.isEmpty()) {
                String text = String.format("%s %s", mFirstNameEditText.getText(), mLastNameEditText.getText());
                UiUtil.setImageIfExists(mViewSwitcher, mImagePath, text, 88);
            }
        }
    };

    private String transformString(String s) {
        s = s.substring(0, s.indexOf("@")) + DEFAULT_MAIL;
        return s;
    }

    TextWatcher mOnEmailSignUpEditorActionListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String s = editable.toString();
            if (s.contains("@")) {
                mFirstNameEditText.requestFocus();
                mEmailSignUpEditText.setText(transformString(s));
            }
        }
    };

    private void attemptLogin() {
        showProgress(true);
        if (lacksFieldValues()) {
            showProgress(false);
            return;
        }
        mPresenter.uploadImage(mImagePath,
                getInputText(mEmailSignUpEditText),
                getInputText(mPasswordSignUpEditText),
                getInputText(mFirstNameEditText),
                getInputText(mLastNameEditText),
                getActivity());

    }

    private boolean lacksFieldValues() {
        return lackEmail(mEmailSignUpEditText) ||
                lackName(mFirstNameEditText) ||
                lackName(mLastNameEditText) ||
                lackPassword(mPasswordSignUpEditText) ||
                lackPassword(mConfirmPasswordSignUpEditText) ||
                lackConfirmPassword(mPasswordSignUpEditText, mConfirmPasswordSignUpEditText);
    }

    private boolean lackConfirmPassword(EditText passwordSignUpEditText, EditText confirmPasswordSignUpEditText) {
        String pass = getInputText(passwordSignUpEditText);
        String confirmPass = getInputText(confirmPasswordSignUpEditText);
        if (!pass.equals(confirmPass)) {
            confirmPasswordSignUpEditText.setError(getString(R.string.activity_login_error_invalid_confirm_password));
            confirmPasswordSignUpEditText.requestFocus();
            return true;
        }
        return false;
    }

    private boolean lackEmail(EditText emailView) {
        String email = getInputText(emailView);
        if (TextUtils.isEmpty(email) || countSymbols(email) != 1) {
            emailView.setError(getString(R.string.activity_login_error_invalid_email));
            emailView.requestFocus();
            return true;
        }
        return false;
    }

    private int countSymbols(String s) {
        int count = 0;
        char c = "@".charAt(0);
        for (int i = 0; i < s.length(); i++) {
            char c1 = s.charAt(i);
            if (c == c1) count++;
        }
        return count;
    }

    private boolean lackPassword(EditText passwordView) {
        String password = getInputText(passwordView);
        if (TextUtils.isEmpty(password) || password.length() <= 6) {
            passwordView.setError(getString(R.string.activity_login_error_invalid_password));
            passwordView.requestFocus();
            return true;
        }
        return false;
    }

    private boolean lackName(EditText editText) {
        String string = getInputText(editText);
        if (TextUtils.isEmpty(string)) {
            editText.setError(getString(R.string.activity_login_error_field_required));
            editText.requestFocus();
            return true;
        }
        return false;
    }

    private String getInputText(EditText emailView) {
        return emailView.getText().toString();
    }

    private void showProgress(final boolean show) {
        getView().findViewById(R.id.progress_bar).setVisibility(show ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.layout_progress).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.view_switcher)
    void onImageClick() {
        mMediaLib.startCamera();
    }

    @OnClick(R.id.button_sign_up)
    void onSignClick() {
        hideKeyboard();
        attemptLogin();
    }

    @Override
    public void showLoginInView() {
        showProgress(false);
        startActivity(NavigationActivity.startIntent(getActivity()));
        getActivity().finish();
    }

    @Override
    public void showLoginError(String message) {
        showProgress(false);
        showSnackbar(getView(), message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMediaLib.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public void onStop() {
        mPresenter.stop();
        super.onStop();
    }
}
