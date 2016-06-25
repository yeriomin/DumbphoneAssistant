package com.github.yeriomin.dumbphoneassistant;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DumbphoneAssistantPreferenceActivity extends PreferenceActivity {

    public static final String PREFERENCE_TRANSLITERATE = "prefTransliterate";
    public static final String PREFERENCE_ADD_TYPE_SUFFIX = "prefAddTypeSuffix";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}