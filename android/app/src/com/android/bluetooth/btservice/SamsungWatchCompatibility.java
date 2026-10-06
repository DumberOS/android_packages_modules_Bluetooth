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

import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** DumberOS compatibility for Samsung's signed apps using the China Wear OS companion. */
final class SamsungWatchCompatibility {
    private static final ParcelUuid CHINA_UUID =
            ParcelUuid.fromString("1e4d4a34-70a0-0708-6aac-2343a45596ed");

    private SamsungWatchCompatibility() {}

    static List<ParcelUuid> getRemoteUuids(
            String[] callerPackages, String deviceName, ParcelUuid[] uuids) {
        // Preserve missing/unfinished discovery so callers still wait for real services.
        if (uuids == null) {
            return null;
        }
        List<ParcelUuid> result = Arrays.asList(uuids);
        if (uuids.length == 0
                || deviceName == null
                || !deviceName.startsWith("Galaxy Watch")
                || !isSamsungWearCompanion(callerPackages)
                || result.contains(CHINA_UUID)) {
            return result;
        }
        result = new ArrayList<>(result);
        result.add(CHINA_UUID);
        return result;
    }

    private static boolean isSamsungWearCompanion(String[] packages) {
        if (packages != null) {
            for (String name : packages) {
                if ("com.samsung.android.app.watchmanager".equals(name)
                        || "com.samsung.android.waterplugin".equals(name) // Watch4
                        || "com.samsung.wearable.watch5plugin".equals(name)
                        || "com.samsung.wearable.watch6plugin".equals(name)
                        || "com.samsung.wearable.watch7plugin".equals(name)
                        || "com.samsung.wearable.watch8plugin".equals(name)
                        // Reserved for Watch9 if Samsung retains this package naming scheme.
                        || "com.samsung.wearable.watch9plugin".equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
