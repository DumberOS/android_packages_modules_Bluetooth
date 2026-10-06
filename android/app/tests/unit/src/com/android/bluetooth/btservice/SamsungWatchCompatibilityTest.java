/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.bluetooth.btservice;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.os.ParcelUuid;

import org.junit.Test;

import java.util.List;

public class SamsungWatchCompatibilityTest {
    private static final ParcelUuid CHINA =
            ParcelUuid.fromString("1e4d4a34-70a0-0708-6aac-2343a45596ed");
    private static final ParcelUuid HANDSFREE =
            ParcelUuid.fromString("0000111e-0000-1000-8000-00805f9b34fb");
    private static final String[] WEARABLE = {"com.samsung.android.app.watchmanager"};

    @Test
    public void samsungAppsGetChinaUuidWithoutChangingStoredServices() {
        ParcelUuid[] original = {HANDSFREE};
        String[] plugins = {
            "com.samsung.android.waterplugin",
            "com.samsung.wearable.watch5plugin",
            "com.samsung.wearable.watch6plugin",
            "com.samsung.wearable.watch7plugin",
            "com.samsung.wearable.watch8plugin",
            "com.samsung.wearable.watch9plugin"
        };
        for (int generation = 4; generation <= 9; generation++) {
            for (String name : new String[] {WEARABLE[0], plugins[generation - 4]}) {
                for (String suffix : new String[] {" (0TVX)", " Classic (0TVX)", " Pro (0TVX)"}) {
                    List<ParcelUuid> result =
                            SamsungWatchCompatibility.getRemoteUuids(
                                    new String[] {name},
                                    "Galaxy Watch" + generation + suffix,
                                    original);
                    assertEquals(List.of(HANDSFREE, CHINA), result);
                    result.set(0, CHINA);
                    assertArrayEquals(new ParcelUuid[] {HANDSFREE}, original);
                }
            }
        }
    }

    @Test
    public void unlistedPluginsAndSimilarPackageNamesKeepOriginalServices() {
        for (String name :
                new String[] {
                    "com.samsung.wearable.watch3plugin",
                    "com.samsung.wearable.watch10plugin",
                    "com.samsung.wearable.watch5plugin.other",
                    "com.samsung.wearable.budsplugin"
                }) {
            assertEquals(
                    List.of(HANDSFREE),
                    SamsungWatchCompatibility.getRemoteUuids(
                            new String[] {name}, "Galaxy Watch5", new ParcelUuid[] {HANDSFREE}));
        }
    }

    @Test
    public void otherAppsAndOtherDevicesKeepOriginalServices() {
        ParcelUuid[] original = {HANDSFREE};
        assertEquals(
                List.of(HANDSFREE),
                SamsungWatchCompatibility.getRemoteUuids(
                        new String[] {"com.example.other"}, "Galaxy Watch7 (0TVX)", original));
        assertEquals(
                List.of(HANDSFREE),
                SamsungWatchCompatibility.getRemoteUuids(null, "Galaxy Watch7 (0TVX)", original));
        assertEquals(
                List.of(HANDSFREE),
                SamsungWatchCompatibility.getRemoteUuids(WEARABLE, "Headphones", original));
        assertEquals(
                List.of(HANDSFREE),
                SamsungWatchCompatibility.getRemoteUuids(WEARABLE, null, original));
    }

    @Test
    public void preservesMissingDiscoveryAndDoesNotDuplicateChinaUuid() {
        assertNull(SamsungWatchCompatibility.getRemoteUuids(WEARABLE, "Galaxy Watch7", null));
        assertTrue(
                SamsungWatchCompatibility.getRemoteUuids(
                                WEARABLE, "Galaxy Watch7", new ParcelUuid[0])
                        .isEmpty());
        assertEquals(
                List.of(HANDSFREE, CHINA),
                SamsungWatchCompatibility.getRemoteUuids(
                        WEARABLE, "Galaxy Watch7", new ParcelUuid[] {HANDSFREE, CHINA}));
    }
}
