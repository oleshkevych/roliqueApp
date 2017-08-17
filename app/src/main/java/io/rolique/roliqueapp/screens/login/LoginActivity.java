package io.rolique.roliqueapp.screens.login;

import android.content.Context;
import android.content.Intent;

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
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.main.MainActivity;

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

    @BindView(R.id.layout_form_sign_up) LinearLayout mSignUpFormLayout;
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
            mPresenter.signUp(getInputText(mEmailSignUpEditText),
                    getInputText(mPasswordSignUpEditText),
                    getInputText(mFirstNameSignUpEditText),
                    getInputText(mLastNameSignUpEditText));
        }
    }

    private boolean isFieldsValid(boolean isSignIn) {
        if (isSignIn) {
            if(!isEmailValid(mEmailSignInEditText)) return false;
            if(!isPasswordValid(mPasswordSignInEditText)) return false;
        } else {
            if(!isEmailValid(mEmailSignUpEditText)) return false;
            if(!isNameValid(mFirstNameSignUpEditText)) return false;
            if(!isNameValid(mLastNameSignUpEditText)) return false;
            if(!isPasswordValid(mPasswordSignUpEditText)) return false;
            if(!isPasswordValid(mConfirmPasswordSignUpEditText)) return false;
            if(!isPasswordConfirmed(mPasswordSignUpEditText, mConfirmPasswordSignUpEditText)) return false;
        }
        return true;
    }

    private boolean isPasswordConfirmed(EditText passwordSignUpEditText, EditText confirmPasswordSignUpEditText) {
        String pass = getInputText(passwordSignUpEditText);
        String confirmPass = getInputText(confirmPasswordSignUpEditText);
        if (!pass.equals(confirmPass)) {
            confirmPasswordSignUpEditText.setError(getString(R.string.activity_login_error_invalid_confirm_password));
            confirmPasswordSignUpEditText.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isEmailValid(EditText emailView) {
        String email = getInputText(emailView);
        if (TextUtils.isEmpty(email) || countSymbols(email) != 1) {
            emailView.setError(getString(R.string.activity_login_error_invalid_email));
            emailView.requestFocus();
            return false;
        }
        return true;
    }

    private int countSymbols(String s) {
        int count = 0;
        char c = "@".charAt(0);
        for (int i = 0; i < s.length(); i++) {
            char c1 = s.charAt(i);
            if (c == c1) {
                count++;
            }
        }
        return count;
    }

    private boolean isPasswordValid(EditText passwordView) {
        String password = getInputText(passwordView);
        if (TextUtils.isEmpty(password) || password.length() <= 6) {
            passwordView.setError(getString(R.string.activity_login_error_invalid_password));
            passwordView.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isNameValid(EditText editText) {
        String string = getInputText(editText);
        if (TextUtils.isEmpty(string)) {
            editText.setError(getString(R.string.activity_login_error_field_required));
            editText.requestFocus();
            return false;
        }
        return true;
    }

    private String getInputText(EditText emailView) {
        return emailView.getText().toString();
    }

    private void showProgress(final boolean show) {
        findViewById(R.id.progress_bar).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.layout_progress).setVisibility(show ? View.VISIBLE : View.GONE);
    }

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
        mSignUpSwitcherButton.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.black_alpha_80));
        mSignUpSwitcherButton.setClickable(true);
        mSignInSwitcherButton.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.colorPrimaryDark));
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
        mSignInSwitcherButton.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.black_alpha_80));
        mSignInSwitcherButton.setClickable(true);
        mSignUpSwitcherButton.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.colorPrimaryDark));
        mSignUpSwitcherButton.setClickable(false);
        mEmailSignInEditText.getText().clear();
        mPasswordSignInEditText.getText().clear();
        mSignUpFormLayout.setVisibility(View.VISIBLE);
        mSignInFormLayout.setVisibility(View.GONE);
    }

    @Override
    public void showLoginInView() {
        showProgress(false);
        startActivity(MainActivity.startIntent(LoginActivity.this));
    }

    @Override
    public void showLoginError() {
        showProgress(false);
        showSnackbar("Login Failed");
    }
}

