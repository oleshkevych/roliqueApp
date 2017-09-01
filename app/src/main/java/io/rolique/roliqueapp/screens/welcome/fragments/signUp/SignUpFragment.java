package io.rolique.roliqueapp.screens.welcome.fragments.signUp;

import android.app.Fragment;
import android.graphics.Bitmap;
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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;
import io.rolique.roliqueapp.BaseFragment;

public class SignUpFragment extends BaseFragment implements SignUpContract.View {

    public static Fragment newInstance() {
        return new SignUpFragment();
    }


    private final String DEFAULT_MAIL = "@rolique.io";

    @Inject SignUpPresenter mPresenter;

    // UI references.
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @BindView(R.id.text_view_user_image) TextView mUserImageTextView;
    @BindView(R.id.edit_text_email_sign_up) EditText mEmailSignUpEditText;
    @BindView(R.id.edit_text_first_name_sign_up) EditText mFirstNameSignUpEditText;
    @BindView(R.id.edit_text_last_name_sign_up) EditText mLastNameSignUpEditText;
    @BindView(R.id.edit_text_password_sign_up) EditText mPasswordSignUpEditText;
    @BindView(R.id.edit_text_confirm_password_sign_up) EditText mConfirmPasswordSignUpEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();

        mConfirmPasswordSignUpEditText.setOnEditorActionListener(mOnEditorActionListener);
        mEmailSignUpEditText.setOnFocusChangeListener(mOnFocusChangeListener);

        mLastNameSignUpEditText.addTextChangedListener(mOnNameEditorActionListener);
        mFirstNameSignUpEditText.addTextChangedListener(mOnNameEditorActionListener);
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
                mFirstNameSignUpEditText.requestFocus();
                mEmailSignUpEditText.setText(transformString(s));
            }
        }
    };

    private void attemptLogin() {
        showProgress(true);
        if (laksFieldValues()) {
            showProgress(false);
            return;
        }
        mPresenter.uploadImage(getImageBitmap(),
                getInputText(mEmailSignUpEditText),
                getInputText(mPasswordSignUpEditText),
                getInputText(mFirstNameSignUpEditText),
                getInputText(mLastNameSignUpEditText),
                getActivity());

    }

    private Bitmap getImageBitmap() {
        mUserImageTextView.setDrawingCacheEnabled(true);
        mUserImageTextView.destroyDrawingCache();
        mUserImageTextView.buildDrawingCache();
        return mUserImageTextView.getDrawingCache();
    }

    private boolean laksFieldValues() {
        return lackEmail(mEmailSignUpEditText) ||
                lackName(mFirstNameSignUpEditText) ||
                lackName(mLastNameSignUpEditText) ||
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

    TextWatcher mOnNameEditorActionListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String firstName = mFirstNameSignUpEditText.getText().toString();
            String lastName = mLastNameSignUpEditText.getText().toString();
            String text = "";
            if (!firstName.isEmpty())
                text += firstName.substring(0, 1).toUpperCase();
            if (!lastName.isEmpty())
                text += lastName.substring(0, 1).toUpperCase();
            mUserImageTextView.setText(text);
        }
    };

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
