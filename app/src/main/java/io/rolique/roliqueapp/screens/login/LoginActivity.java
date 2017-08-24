package io.rolique.roliqueapp.screens.login;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.navigation.chat.ChatsActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoginContract.View {

    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private final String DEFAULT_MAIL = "@rolique.io";

    @Inject LoginPresenter mPresenter;
    @Inject RoliqueApplicationPreferences mPreferences;

    // UI references.
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.button_sign_in_switcher) Button mSignInSwitcherButton;
    @BindView(R.id.button_sign_up_switcher) Button mSignUpSwitcherButton;

    @BindView(R.id.layout_form_sign_in) LinearLayout mSignInFormLayout;
    @BindView(R.id.edit_text_email_sign_in) EditText mEmailSignInEditText;
    @BindView(R.id.edit_text_password_sign_in) EditText mPasswordSignInEditText;

    @BindView(R.id.layout_form_sign_up) ScrollView mSignUpFormLayout;
    @BindView(R.id.text_view_user_image) TextView mUserImageTextView;
    @BindView(R.id.edit_text_email_sign_up) EditText mEmailSignUpEditText;
    @BindView(R.id.edit_text_first_name_sign_up) EditText mFirstNameSignUpEditText;
    @BindView(R.id.edit_text_last_name_sign_up) EditText mLastNameSignUpEditText;
    @BindView(R.id.edit_text_password_sign_up) EditText mPasswordSignUpEditText;
    @BindView(R.id.edit_text_confirm_password_sign_up) EditText mConfirmPasswordSignUpEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setSignInForm();

        mPasswordSignInEditText.setOnEditorActionListener(mOnEditorActionListener);
        mConfirmPasswordSignUpEditText.setOnEditorActionListener(mOnEditorActionListener);

        mEmailSignInEditText.setOnFocusChangeListener(mOnFocusChangeListener);
        mEmailSignUpEditText.setOnFocusChangeListener(mOnFocusChangeListener);

        mLastNameSignUpEditText.addTextChangedListener(mOnNameEditorActionListener);
        mFirstNameSignUpEditText.addTextChangedListener(mOnNameEditorActionListener);
    }

    @Override
    protected void inject() {
        DaggerLoginComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .loginPresenterModule(new LoginPresenterModule(LoginActivity.this))
                .build()
                .inject(LoginActivity.this);
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
                if (editText.equals(mEmailSignInEditText)) {
                    mEmailSignInEditText.addTextChangedListener(mOnEmailSignInEditorActionListener);
                } else {
                    mEmailSignUpEditText.addTextChangedListener(mOnEmailSignUpEditorActionListener);
                }
                String s = getInputText(editText);
                if (!s.isEmpty() && s.contains("@")) {
                    s = s.substring(0, s.indexOf("@"));
                    editText.setText(s);
                }
            } else {
                if (editText.equals(mEmailSignInEditText)) {
                    mEmailSignInEditText.removeTextChangedListener(mOnEmailSignInEditorActionListener);
                } else {
                    mEmailSignUpEditText.removeTextChangedListener(mOnEmailSignUpEditorActionListener);
                }
            }
        }
    };

    TextWatcher mOnEmailSignInEditorActionListener = new TextWatcher() {
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
                mPasswordSignInEditText.requestFocus();
                mEmailSignInEditText.setText(transformString(s));
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
        boolean isSignIn = mSignInFormLayout.getVisibility() == View.VISIBLE;
        showProgress(true);
        if(!isFieldsValid(isSignIn)) {
            showProgress(false);
            return;
        }

        if (isSignIn) {
            mPresenter.signIn(getInputText(mEmailSignInEditText), getInputText(mPasswordSignInEditText));
        } else {
            mPresenter.uploadImage(getImageBitmap(),
                    getInputText(mEmailSignUpEditText),
                    getInputText(mPasswordSignUpEditText),
                    getInputText(mFirstNameSignUpEditText),
                    getInputText(mLastNameSignUpEditText));
        }
    }

    private Bitmap getImageBitmap() {
        mUserImageTextView.setDrawingCacheEnabled(true);
        mUserImageTextView.destroyDrawingCache();
        mUserImageTextView.buildDrawingCache();
        return mUserImageTextView.getDrawingCache();
    }

    private boolean isFieldsValid(boolean isSignIn) {
        if (isSignIn) {
            return lackEmail(mEmailSignInEditText) &&
                    lackPassword(mPasswordSignInEditText);
        } else {
            return lackEmail(mEmailSignUpEditText) &&
                    lackName(mFirstNameSignUpEditText) &&
                    lackName(mLastNameSignUpEditText) &&
                    lackPassword(mPasswordSignUpEditText) &&
                    lackPassword(mConfirmPasswordSignUpEditText) &&
                    lackConfirmPassword(mPasswordSignUpEditText, mConfirmPasswordSignUpEditText);
        }
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
        findViewById(R.id.progress_bar).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_progress).setVisibility(show ? View.VISIBLE : View.GONE);
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

    @OnClick({R.id.button_sign_in, R.id.button_sign_up})
    void onSignClick() {
        hideKeyboard();
        attemptLogin();
    }

    @OnClick(R.id.button_sign_in_switcher)
    void onSignInClicked() {
        setSignInForm();
    }

    private void setSignInForm() {
        mToolbar.setTitle(R.string.activity_login_button_sign_in);
        mSignUpSwitcherButton.setBackground(ContextCompat.getDrawable(LoginActivity.this, R.drawable.shape_sign_up_button));
        mSignUpSwitcherButton.setClickable(true);
        mSignInSwitcherButton.setBackground(ContextCompat.getDrawable(LoginActivity.this, R.drawable.shape_sign_in_button_selected));
        mSignInSwitcherButton.setClickable(false);
        mEmailSignUpEditText.getText().clear();
        mFirstNameSignUpEditText.getText().clear();
        mLastNameSignUpEditText.getText().clear();
        mPasswordSignUpEditText.getText().clear();
        mConfirmPasswordSignUpEditText.getText().clear();
        mSignUpFormLayout.setVisibility(View.GONE);
        mSignInFormLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.button_sign_up_switcher)
    void onSignUpClicked() {
        mToolbar.setTitle(R.string.activity_login_button_sign_up);
        mSignInSwitcherButton.setBackground(ContextCompat.getDrawable(LoginActivity.this,  R.drawable.shape_sign_in_button));
        mSignInSwitcherButton.setClickable(true);
        mSignUpSwitcherButton.setBackground(ContextCompat.getDrawable(LoginActivity.this, R.drawable.shape_sign_up_button_selected));
        mSignUpSwitcherButton.setClickable(false);
        mEmailSignInEditText.getText().clear();
        mPasswordSignInEditText.getText().clear();
        mSignUpFormLayout.setVisibility(View.VISIBLE);
        mSignInFormLayout.setVisibility(View.GONE);
    }

    @Override
    public void showLoginInView() {
        showProgress(false);
        startActivity(ChatsActivity.startIntent(LoginActivity.this));
        finish();
    }

    @Override
    public void showLoginError() {
        showProgress(false);
        showSnackbar("Login Failed");
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
}

