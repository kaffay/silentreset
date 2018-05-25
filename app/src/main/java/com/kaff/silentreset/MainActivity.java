package com.kaff.silentreset;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        private SwitchPreference mSwitch;
        private ListPreference mResetTimer;
        private EditTextPreference mCustomResetTimer;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);
            initPreferencesItem();
            setupPreferenceItem();

            //检查标志，如果没有计划的任务，则触发一次
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean pendingReset = sp.getBoolean("pending_reset", false);
            if (!pendingReset) sendSettingsChangedBroadcast();
        }

        private void initPreferencesItem() {
            mSwitch = (SwitchPreference)findPreference("enable_auto_reset");
            mSwitch.setOnPreferenceChangeListener(this);
            mResetTimer = (ListPreference)findPreference("reset_after_minutes");
            mResetTimer.setOnPreferenceChangeListener(this);
            mCustomResetTimer = (EditTextPreference)findPreference("reset_after_minutes_custom");
            mCustomResetTimer.setOnPreferenceChangeListener(this);
        }

        private void setupPreferenceItem() {
            boolean enableTimer = mSwitch.isChecked();
            boolean customTimer = "0".equals(mResetTimer.getValue());

            mResetTimer.setEnabled(enableTimer);
            mResetTimer.setSummary("");
            if (enableTimer) {
                mResetTimer.setSummary(mResetTimer.getValue() + " 分钟");
                if (customTimer) mResetTimer.setSummary("自定义");
            }

            mCustomResetTimer.setEnabled(enableTimer && customTimer);
            mCustomResetTimer.setSummary("");
            if (enableTimer && customTimer) mCustomResetTimer.setSummary(mCustomResetTimer.getText() + " 分钟");
        }

        private void sendSettingsChangedBroadcast() {
            Intent i = new Intent("com.kaff.TIMER_SETTINGS");
            getActivity().sendBroadcast(i);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mSwitch) {
                boolean enable = (boolean)newValue;
                mSwitch.setChecked(enable);
            } else if (preference == mResetTimer) {
                mResetTimer.setValue(newValue.toString());
            } else if (preference == mCustomResetTimer) {
                String newVal = newValue.toString();
                if ("0".equals(newVal)) newVal = "1";
                mCustomResetTimer.setText(newVal);
            }
            setupPreferenceItem();
            sendSettingsChangedBroadcast();
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);
            if (preference == mCustomResetTimer) {
                Editable editable = mCustomResetTimer.getEditText().getText();
                Selection.selectAll(editable);
                mCustomResetTimer.getEditText().requestFocus();
            }
            return true;
        }
    }
}
