/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.view.Menu;
import com.android.settings.testutils.FakeFeatureFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SettingsActivityTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Context mContext;

    @Mock
    private FragmentManager mFragmentManager;
    @Mock
    private ActivityManager.TaskDescription mTaskDescription;
    @Mock
    private Bitmap mBitmap;
    private SettingsActivity mActivity;

    private FakeFeatureFactory mFeatureFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        FakeFeatureFactory.setupForTest(mContext);
        mFeatureFactory = (FakeFeatureFactory) FakeFeatureFactory.getFactory(mContext);

        mActivity = spy(new SettingsActivity());
        doReturn(mBitmap).when(mActivity).getBitmapFromXmlResource(anyInt());
    }

    @Test
    public void launchSettingFragment_nullExtraShowFragment_shouldNotCrash()
            throws ClassNotFoundException {
        when(mActivity.getFragmentManager()).thenReturn(mFragmentManager);
        when(mFragmentManager.beginTransaction()).thenReturn(mock(FragmentTransaction.class));

        doReturn(RuntimeEnvironment.application.getClassLoader()).when(mActivity).getClassLoader();

        mActivity.launchSettingFragment(null, true, mock(Intent.class));
    }

    @Test
    public void testSetTaskDescription_IconChanged() {
        mActivity.setTaskDescription(mTaskDescription);

        verify(mTaskDescription).setIcon(any());
    }

    @Test
    public void testCreateOptionsMenu_setsUpSearch() {
        ReflectionHelpers.setField(mActivity, "mSearchFeatureProvider",
                mFeatureFactory.getSearchFeatureProvider());
        mActivity.mDisplaySearch = true;
        mActivity.onCreateOptionsMenu(null);

        verify(mFeatureFactory.getSearchFeatureProvider()).setUpSearchMenu(any(Menu.class),
                any(Activity.class));
    }

    @Test
    public void testSaveState_DisplaySearchSaved() {
        mActivity.mDisplaySearch = true;
        Bundle bundle = new Bundle();
        mActivity.saveState(bundle);

        assertThat((boolean) bundle.get(SettingsActivity.SAVE_KEY_SHOW_SEARCH)).isTrue();
    }

    @Test
    public void testSaveState_EnabledHomeSaved() {
        mActivity.mDisplayHomeAsUpEnabled = true;
        Bundle bundle = new Bundle();
        mActivity.saveState(bundle);

        assertThat((boolean) bundle.get(SettingsActivity.SAVE_KEY_SHOW_HOME_AS_UP)).isTrue();
    }

    @Test
    public void testRestoreState_DisplaySearchRestored() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SettingsActivity.SAVE_KEY_SHOW_SEARCH, true);
        mActivity.onRestoreInstanceState(bundle);

        assertThat(mActivity.mDisplaySearch).isTrue();
    }

    @Test
    public void testRestoreState_EnabledHomeRestored() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SettingsActivity.SAVE_KEY_SHOW_SEARCH, true);
        mActivity.onRestoreInstanceState(bundle);

        assertThat(mActivity.mDisplaySearch).isTrue();
    }
}
