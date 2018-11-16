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
package com.android.settings.fuelgauge;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.UserManager;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.android.settings.TestConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.MockitoJUnit;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class BatteryEntryTest {

    private static final int APP_UID = 123;
    private static final String APP_DEFAULT_PACKAGE_NAME = "com.android.test";
    private static final String APP_LABEL = "Test App Name";
    private static final String HIGH_DRAIN_PACKAGE = "com.android.test.screen";

    @Rule public MockitoRule mocks = MockitoJUnit.rule();

    @Mock private Context mockContext;
    @Mock private Handler mockHandler;
    @Mock private PackageManager mockPackageManager;
    @Mock private UserManager mockUserManager;

    @Before
    public void stubContextToReturnMockPackageManager() {
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);
    }

    @Before
    public void stubPackageManagerToReturnAppPackageAndName() throws NameNotFoundException {
        when(mockPackageManager.getPackagesForUid(APP_UID)).thenReturn(
            new String[]{APP_DEFAULT_PACKAGE_NAME});

        ApplicationInfo appInfo = mock(ApplicationInfo.class);
        when(mockPackageManager.getApplicationInfo(APP_DEFAULT_PACKAGE_NAME, 0 /* no flags */))
            .thenReturn(appInfo);
        when(mockPackageManager.getApplicationLabel(appInfo)).thenReturn(APP_LABEL);
    }

    private BatteryEntry createBatteryEntryForApp() {
        return new BatteryEntry(mockContext, mockHandler, mockUserManager, createSipperForApp());
    }

    private BatterySipper createSipperForApp() {
        BatterySipper sipper =
            new BatterySipper(DrainType.APP, new FakeUid(APP_UID), 0 /* power use */);
        sipper.packageWithHighestDrain = HIGH_DRAIN_PACKAGE;
        return sipper;
    }

    @Test
    public void batteryEntryForApp_shouldSetDefaultPackageNameAndLabel() throws Exception {
        BatteryEntry entry = createBatteryEntryForApp();

        assertThat(entry.defaultPackageName).isEqualTo(APP_DEFAULT_PACKAGE_NAME);
        assertThat(entry.getLabel()).isEqualTo(APP_LABEL);
    }

    @Test
    public void batteryEntryForApp_shouldSetLabelAsPackageName_whenPackageCannotBeFound()
        throws Exception {
      when(mockPackageManager.getApplicationInfo(APP_DEFAULT_PACKAGE_NAME, 0 /* no flags */))
          .thenThrow(new NameNotFoundException());

      BatteryEntry entry = createBatteryEntryForApp();

      assertThat(entry.getLabel()).isEqualTo(APP_DEFAULT_PACKAGE_NAME);
    }

    @Test
    public void batteryEntryForApp_shouldSetHighestDrainPackage_whenPackagesCannotBeFoundForUid() {
        when(mockPackageManager.getPackagesForUid(APP_UID)).thenReturn(null);

        BatteryEntry entry = createBatteryEntryForApp();

        assertThat(entry.getLabel()).isEqualTo(HIGH_DRAIN_PACKAGE);
    }

    @Test
    public void batteryEntryForApp_shouldSetHighestDrainPackage_whenMultiplePackagesFoundForUid() {
        when(mockPackageManager.getPackagesForUid(APP_UID)).thenReturn(
            new String[]{APP_DEFAULT_PACKAGE_NAME, "package2", "package3"});

        BatteryEntry entry = createBatteryEntryForApp();
        
        assertThat(entry.getLabel()).isEqualTo(HIGH_DRAIN_PACKAGE);
    }
}
