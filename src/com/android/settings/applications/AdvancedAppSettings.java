/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.SearchIndexableResource;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.ApplicationsState.AppEntry;
import com.android.settings.applications.ApplicationsState.Callbacks;
import com.android.settings.applications.ApplicationsState.Session;
import com.android.settings.applications.PermissionsInfo.Callback;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;

public class AdvancedAppSettings extends SettingsPreferenceFragment implements Callbacks, Callback,
        Indexable {

    static final String TAG = "AdvancedAppSettings";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final String KEY_APP_PERM = "manage_perms";
    private static final String KEY_APP_DOMAIN_URLS = "domain_urls";
    private static final String KEY_DEFAULT_EMERGENCY_APP = "default_emergency_app";

    private ApplicationsState mApplicationsState;
    private Session mSession;
    private Preference mAppPermsPreference;
    private Preference mAppDomainURLsPreference;
    private PermissionsInfo mPermissionsInfo;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.advanced_apps);

        mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        mSession = mApplicationsState.newSession(this);

        mAppPermsPreference = findPreference(KEY_APP_PERM);
        mAppDomainURLsPreference = findPreference(KEY_APP_DOMAIN_URLS);
        updateUI();

        if (!DefaultEmergencyPreference.isAvailable(getActivity())) {
            removePreference(KEY_DEFAULT_EMERGENCY_APP);
        }
    }

    private void updateUI() {
        ArrayList<AppEntry> allApps = mSession.getAllApps();

        int countAppWithDomainURLs = 0;
        for (AppEntry entry : allApps) {
            boolean hasDomainURLs =
                    (entry.info.privateFlags & ApplicationInfo.PRIVATE_FLAG_HAS_DOMAIN_URLS) != 0;
            if (hasDomainURLs) countAppWithDomainURLs++;
        }
        String summary = getResources().getQuantityString(
                R.plurals.domain_urls_apps_summary, countAppWithDomainURLs, countAppWithDomainURLs);
        mAppDomainURLsPreference.setSummary(summary);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.APPLICATIONS_ADVANCED;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPermissionsInfo = new PermissionsInfo(getActivity(), this);
    }

    @Override
    public void onRunningStateChanged(boolean running) {
        // No-op.
    }

    @Override
    public void onPackageListChanged() {
        updateUI();
    }

    @Override
    public void onRebuildComplete(ArrayList<AppEntry> apps) {
        // No-op.
    }

    @Override
    public void onPackageIconChanged() {
        // No-op.
    }

    @Override
    public void onPackageSizeChanged(String packageName) {
        // No-op.
    }

    @Override
    public void onAllSizesComputed() {
        // No-op.
    }

    @Override
    public void onLauncherInfoChanged() {
        // No-op.
    }

    @Override
    public void onLoadEntriesCompleted() {
        // No-op.
    }

    @Override
    public void onPermissionLoadComplete() {
        Activity activity = getActivity();
        if (activity == null) return;
        mAppPermsPreference.setSummary(activity.getString(R.string.app_permissions_summary,
                mPermissionsInfo.getRuntimePermAppsGrantedCount(),
                mPermissionsInfo.getRuntimePermAppsCount()));
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList<>(1);
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.advanced_apps;
            result.add(sir);
            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList<>(1);
            if (!DefaultEmergencyPreference.isAvailable(context)) {
                result.add(KEY_DEFAULT_EMERGENCY_APP);
            }
            return result;
        }
    };
}