/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.applications;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.Settings;
import com.android.settings.applications.AppStateInstallAppsBridge.InstallAppsState;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class ExternalSourcesDetails extends AppInfoWithHeader
        implements OnPreferenceChangeListener {

    private static final String KEY_EXTERNAL_SOURCE_SWITCH = "external_sources_settings_switch";

    private AppStateInstallAppsBridge mAppBridge;
    private AppOpsManager mAppOpsManager;
    private UserManager mUserManager;
    private RestrictedSwitchPreference mSwitchPref;
    private InstallAppsState mInstallAppsState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getActivity();
        mAppBridge = new AppStateInstallAppsBridge(context, mState, null);
        mAppOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        mUserManager = UserManager.get(context);

        addPreferencesFromResource(R.xml.external_sources_details);
        mSwitchPref = (RestrictedSwitchPreference) findPreference(KEY_EXTERNAL_SOURCE_SWITCH);
        mSwitchPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean checked = (Boolean) newValue;
        if (preference == mSwitchPref) {
            if (mInstallAppsState != null && checked != mInstallAppsState.canInstallApps()) {
                if (Settings.ManageAppExternalSourcesActivity.class.getName().equals(
                        getIntent().getComponent().getClassName())) {
                    setResult(checked ? RESULT_OK : RESULT_CANCELED);
                }
                setCanInstallApps(checked);
                refreshUi();
            }
            return true;
        }
        return false;
    }

    static CharSequence getPreferenceSummary(Context context, AppEntry entry) {
        final UserManager um = UserManager.get(context);
        final int userRestrictionSource = um.getUserRestrictionSource(
                UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
                UserHandle.getUserHandleForUid(entry.info.uid));
        switch (userRestrictionSource) {
            case UserManager.RESTRICTION_SOURCE_DEVICE_OWNER:
            case UserManager.RESTRICTION_SOURCE_PROFILE_OWNER:
                return context.getString(R.string.disabled_by_admin);
            case UserManager.RESTRICTION_SOURCE_SYSTEM:
                return context.getString(R.string.disabled);
        }

        final InstallAppsState appsState;
        if (entry.extraInfo instanceof InstallAppsState) {
            appsState = (InstallAppsState) entry.extraInfo;
        } else {
            appsState = new AppStateInstallAppsBridge(context, null, null)
                    .createInstallAppsStateFor(entry.info.packageName, entry.info.uid);
        }
        return context.getString(appsState.canInstallApps() ? R.string.external_source_trusted
                : R.string.external_source_untrusted);
    }

    private void setCanInstallApps(boolean newState) {
        mAppOpsManager.setMode(AppOpsManager.OP_REQUEST_INSTALL_PACKAGES,
                mPackageInfo.applicationInfo.uid, mPackageName,
                newState ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_ERRORED);
    }

    @Override
    protected boolean refreshUi() {
        if (mUserManager.hasBaseUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
                UserHandle.of(UserHandle.myUserId()))) {
            mSwitchPref.setChecked(false);
            mSwitchPref.setSummary(R.string.disabled);
            mSwitchPref.setEnabled(false);
            return true;
        }
        mSwitchPref.checkRestrictionAndSetDisabled(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
        if (mSwitchPref.isDisabledByAdmin()) {
            return true;
        }
        mInstallAppsState = mAppBridge.createInstallAppsStateFor(mPackageName,
                mPackageInfo.applicationInfo.uid);
        if (!mInstallAppsState.isPotentialAppSource()) {
            // Invalid app entry. Should not allow changing permission
            mSwitchPref.setEnabled(false);
            return true;
        }
        mSwitchPref.setChecked(mInstallAppsState.canInstallApps());
        return true;
    }

    @Override
    protected AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.MANAGE_EXTERNAL_SOURCES;
    }
}
