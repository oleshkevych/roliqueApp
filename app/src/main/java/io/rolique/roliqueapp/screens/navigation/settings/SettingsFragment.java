package io.rolique.roliqueapp.screens.navigation.settings;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.profile.ProfileActivity;
import io.rolique.roliqueapp.services.bootReceiver.SampleBootReceiver;
import io.rolique.roliqueapp.util.AlarmBuilder;
import io.rolique.roliqueapp.util.ui.UiUtil;

public class SettingsFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new SettingsFragment();
    }

    @Inject RoliqueApplicationPreferences mPreferences;

    @BindView(R.id.switch_alarm_time) Switch mNotificationsTimeSwitch;
    @BindView(R.id.button_start_set_dialog) Button mSetTimeButton;
    @BindView(R.id.switch_user_position) TextView mEditUserTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    protected void inject() {
        DaggerSettingsComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getActivity().getApplication()).getRepositoryComponent())
                .build()
                .inject(SettingsFragment.this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpTimeSwitch();
        setUpProfile();
    }

    protected void setUpTimeSwitch() {
        boolean isNotificationAllowed = mPreferences.isNotificationAllowed();
        mNotificationsTimeSwitch.setChecked(isNotificationAllowed);
        setTimeText(isNotificationAllowed);

        mNotificationsTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPreferences.setIsNotificationAllowed(isChecked);
                setTimeText(isChecked);
                AlarmBuilder.setAlarm(getActivity(), mPreferences.getNotificationTime(), false, !isChecked);
            }
        });
    }

    private void setTimeText(boolean isNotificationAllowed) {
        if (isNotificationAllowed) {
            String timeText = getString(R.string.fragment_settings_alarm_text);
            String time = mPreferences.getNotificationTime().replace(" ", ":");
            mNotificationsTimeSwitch.setText(String.format("%s %s", timeText, time));
        } else {
            mNotificationsTimeSwitch.setText(R.string.fragment_settings_alarm_text);
        }
        mSetTimeButton.setVisibility(isNotificationAllowed ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.button_start_set_dialog)
    void openTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.setTitle(R.string.fragment_settings_alarm_time_table_title);
        timePickerDialog.show();
    }

    TimePickerDialog.OnTimeSetListener onTimeSetListener
            = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hour = String.valueOf(hourOfDay);
            if (hourOfDay < 10) hour = "0" + hour;
            String minutes = String.valueOf(minute);
            if (minute < 10) minutes = "0" + minutes;
            mPreferences.setNotificationTime(String.format("%s %s", hour, minutes));
            setTimeText(true);
            AlarmBuilder.setAlarm(getActivity(), mPreferences.getNotificationTime(), false, false);
            Toast.makeText(getActivity(), getString(R.string.fragment_settings_alarm_confirm), Toast.LENGTH_SHORT).show();
        }
    };

    private void setUpProfile() {
        String text = String.format("%s %s %s", getString(R.string.fragment_settings_profile_edit), mPreferences.getFirstName(), mPreferences.getLastName());
        mEditUserTextView.setText(text);
    }

    @OnClick(R.id.switch_user_position)
    void onEditProfileClick() {
        User user = new User();
        user.setImageUrl(mPreferences.getImageUrl());
        user.setFirstName(mPreferences.getFirstName());
        user.setLastName(mPreferences.getLastName());
        user.setId(mPreferences.getId());
        user.setEmail("");
        startActivity(ProfileActivity.startIntent(getActivity(), user, true));
    }
}
