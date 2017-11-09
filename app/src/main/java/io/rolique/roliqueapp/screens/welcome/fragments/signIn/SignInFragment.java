package io.rolique.roliqueapp.screens.welcome.fragments.signIn;

import android.app.Fragment;
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
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;

public class SignInFragment extends BaseFragment implements SignInContract.View {

    public static Fragment newInstance() {
        return new SignInFragment();
    }

    private final String DEFAULT_MAIL = "@rolique.io";

    @Inject SignInPresenter mPresenter;

    // UI references.
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.edit_text_email_sign_in) EditText mEmailSignInEditText;
    @BindView(R.id.edit_text_password_sign_in) EditText mPasswordSignInEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();

        mPasswordSignInEditText.setOnEditorActionListener(mOnEditorActionListener);
        mEmailSignInEditText.setOnFocusChangeListener(mOnFocusChangeListener);
    }

    @Override
    protected void inject() {
        DaggerSignInComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getActivity().getApplication()).getRepositoryComponent())
                .signInPresenterModule(new SignInPresenterModule(SignInFragment.this))
                .build()
                .inject(SignInFragment.this);
    }

    private void setUpToolbar() {
        mToolbar.setTitle(R.string.activity_sign_in_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
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
                mEmailSignInEditText.addTextChangedListener(mOnEmailSignInEditorActionListener);
                String s = getInputText(editText);
                if (!s.isEmpty() && s.contains("@")) {
                    s = s.substring(0, s.indexOf("@"));
                    editText.setText(s);
                }
            } else {
                if(mEmailSignInEditText == null) return;
                mEmailSignInEditText.removeTextChangedListener(mOnEmailSignInEditorActionListener);
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

    private void attemptLogin() {
        showProgress(true);
        if (lacksFieldValues()) {
            showProgress(false);
            return;
        }
        mPresenter.signIn(getInputText(mEmailSignInEditText),
                getInputText(mPasswordSignInEditText),
                getActivity());
    }

    private boolean lacksFieldValues() {
        return lacksEmail(mEmailSignInEditText) ||
                lacksPassword(mPasswordSignInEditText);
    }

    private boolean lacksEmail(EditText emailView) {
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

    private boolean lacksPassword(EditText passwordView) {
        String password = getInputText(passwordView);
        if (TextUtils.isEmpty(password) || password.length() <= 6) {
            passwordView.setError(getString(R.string.activity_login_error_invalid_password));
            passwordView.requestFocus();
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

    @OnClick(R.id.button_sign_in)
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