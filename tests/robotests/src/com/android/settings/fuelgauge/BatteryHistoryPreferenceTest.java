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
package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.PowerManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsRobolectricTestRunner;
import com.android.settings.TestConfig;
import com.android.settingslib.BatteryInfo;
import com.android.settingslib.graph.UsageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class BatteryHistoryPreferenceTest {
    @Mock
    private PreferenceViewHolder mViewHolder;
    @Mock
    private BatteryInfo mBatteryInfo;
    @Mock
    private TextView mTextView;
    @Mock
    private View mItemView;
    @Mock
    private UsageView mUsageView;
    @Mock
    private View mLabelView;
    private BatteryHistoryPreference mBatteryHistoryPreference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final Context context = RuntimeEnvironment.application;
        final View itemView = LayoutInflater.from(context).inflate(R.layout.battery_usage_graph,
                null);

        mBatteryHistoryPreference = new BatteryHistoryPreference(context, null);
        mBatteryHistoryPreference.mBatteryInfo = mBatteryInfo;
        mViewHolder = spy(PreferenceViewHolder.createInstanceForTests(itemView));
        when(mViewHolder.findViewById(R.id.battery_usage)).thenReturn(mUsageView);
        when(mViewHolder.findViewById(R.id.charge)).thenReturn(mTextView);
        when(mUsageView.findViewById(anyInt())).thenReturn(mLabelView);
    }

    @Test
    public void testOnBindViewHolder_updateBatteryUsage() {
        mBatteryHistoryPreference.onBindViewHolder(mViewHolder);

        verify(mViewHolder).findViewById(R.id.battery_usage);
        verify(mTextView).setText(anyString());
        verify(mBatteryInfo).bindHistory(mUsageView);
    }
}
