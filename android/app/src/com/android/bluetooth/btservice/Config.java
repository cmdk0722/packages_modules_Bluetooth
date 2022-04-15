/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.SystemConfigManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.sysprop.BluetoothProperties;
import android.text.TextUtils;
import android.util.Log;

import com.android.bluetooth.R;
import com.android.bluetooth.a2dp.A2dpService;
import com.android.bluetooth.a2dpsink.A2dpSinkService;
import com.android.bluetooth.avrcp.AvrcpTargetService;
import com.android.bluetooth.avrcpcontroller.AvrcpControllerService;
import com.android.bluetooth.bas.BatteryService;
import com.android.bluetooth.bass_client.BassClientService;
import com.android.bluetooth.csip.CsipSetCoordinatorService;
import com.android.bluetooth.gatt.GattService;
import com.android.bluetooth.hap.HapClientService;
import com.android.bluetooth.hearingaid.HearingAidService;
import com.android.bluetooth.hfp.HeadsetService;
import com.android.bluetooth.hfpclient.HeadsetClientService;
import com.android.bluetooth.hid.HidDeviceService;
import com.android.bluetooth.hid.HidHostService;
import com.android.bluetooth.le_audio.LeAudioService;
import com.android.bluetooth.map.BluetoothMapService;
import com.android.bluetooth.mapclient.MapClientService;
import com.android.bluetooth.mcp.McpService;
import com.android.bluetooth.opp.BluetoothOppService;
import com.android.bluetooth.pan.PanService;
import com.android.bluetooth.pbap.BluetoothPbapService;
import com.android.bluetooth.pbapclient.PbapClientService;
import com.android.bluetooth.sap.SapService;
import com.android.bluetooth.tbs.TbsService;
import com.android.bluetooth.vc.VolumeControlService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Config {
    private static final String TAG = "AdapterServiceConfig";

    private static final String FEATURE_HEARING_AID = "settings_bluetooth_hearing_aid";
    private static final String FEATURE_BATTERY = "settings_bluetooth_battery";
    private static long sSupportedMask = 0;

    private static class ProfileConfig {
        Class mClass;
        int mSupported;
        long mMask;

        ProfileConfig(Class theClass, int supportedFlag, long mask) {
            mClass = theClass;
            mSupported = supportedFlag;
            mMask = mask;
        }
    }

    /**
     * List of profile services related to LE audio
     */
    private static final HashSet<Class> mLeAudioUnicastProfiles = new HashSet<Class>(
            Arrays.asList(LeAudioService.class,
                        VolumeControlService.class,
                        McpService.class,
                        CsipSetCoordinatorService.class));

    /**
     * List of profile services with the profile-supported resource flag and bit mask.
     */
    private static final ProfileConfig[] PROFILE_SERVICES_AND_FLAGS = {
            new ProfileConfig(HeadsetService.class, R.bool.profile_supported_hs_hfp,
                    (1 << BluetoothProfile.HEADSET)),
            new ProfileConfig(A2dpService.class, R.bool.profile_supported_a2dp,
                    (1 << BluetoothProfile.A2DP)),
            new ProfileConfig(A2dpSinkService.class, R.bool.profile_supported_a2dp_sink,
                    (1 << BluetoothProfile.A2DP_SINK)),
            new ProfileConfig(HidHostService.class, R.bool.profile_supported_hid_host,
                    (1 << BluetoothProfile.HID_HOST)),
            new ProfileConfig(PanService.class, R.bool.profile_supported_pan,
                    (1 << BluetoothProfile.PAN)),
            new ProfileConfig(GattService.class, R.bool.profile_supported_gatt,
                    (1 << BluetoothProfile.GATT)),
            new ProfileConfig(BluetoothMapService.class, R.bool.profile_supported_map,
                    (1 << BluetoothProfile.MAP)),
            new ProfileConfig(HeadsetClientService.class, R.bool.profile_supported_hfpclient,
                    (1 << BluetoothProfile.HEADSET_CLIENT)),
            new ProfileConfig(AvrcpTargetService.class, R.bool.profile_supported_avrcp_target,
                    (1 << BluetoothProfile.AVRCP)),
            new ProfileConfig(AvrcpControllerService.class,
                    R.bool.profile_supported_avrcp_controller,
                    (1 << BluetoothProfile.AVRCP_CONTROLLER)),
            new ProfileConfig(SapService.class, R.bool.profile_supported_sap,
                    (1 << BluetoothProfile.SAP)),
            new ProfileConfig(PbapClientService.class, R.bool.profile_supported_pbapclient,
                    (1 << BluetoothProfile.PBAP_CLIENT)),
            new ProfileConfig(MapClientService.class, R.bool.profile_supported_mapmce,
                    (1 << BluetoothProfile.MAP_CLIENT)),
            new ProfileConfig(HidDeviceService.class, R.bool.profile_supported_hid_device,
                    (1 << BluetoothProfile.HID_DEVICE)),
            new ProfileConfig(BluetoothOppService.class, R.bool.profile_supported_opp,
                    (1 << BluetoothProfile.OPP)),
            new ProfileConfig(BluetoothPbapService.class, R.bool.profile_supported_pbap,
                    (1 << BluetoothProfile.PBAP)),
            new ProfileConfig(VolumeControlService.class, R.bool.profile_supported_vc,
                    (1 << BluetoothProfile.VOLUME_CONTROL)),
            new ProfileConfig(McpService.class, R.bool.profile_supported_mcp_server,
                    (1 << BluetoothProfile.MCP_SERVER)),
            new ProfileConfig(TbsService.class, R.bool.profile_supported_le_call_control,
                    (1 << BluetoothProfile.LE_CALL_CONTROL)),
            new ProfileConfig(HearingAidService.class,
                    -1,
                    (1 << BluetoothProfile.HEARING_AID)),
            new ProfileConfig(LeAudioService.class, R.bool.profile_supported_le_audio,
                    (1 << BluetoothProfile.LE_AUDIO)),
            new ProfileConfig(CsipSetCoordinatorService.class,
                    R.bool.profile_supported_csip_set_coordinator,
                    (1 << BluetoothProfile.CSIP_SET_COORDINATOR)),
            new ProfileConfig(HapClientService.class, R.bool.profile_supported_hap_client,
                    (1 << BluetoothProfile.HAP_CLIENT)),
            new ProfileConfig(BassClientService.class,
                    R.bool.profile_supported_bass_client,
                    (1 << BluetoothProfile.LE_AUDIO_BROADCAST_ASSISTANT)),
            new ProfileConfig(BatteryService.class, R.bool.profile_supported_battery,
                    (1 << BluetoothProfile.BATTERY)),
    };

    private static Class[] sSupportedProfiles = new Class[0];

    private static boolean sIsGdEnabledUptoScanningLayer = false;

    static void init(Context ctx) {
        if (ctx == null) {
            return;
        }
        Resources resources = ctx.getResources();
        if (resources == null) {
            return;
        }
        List<String> enabledProfiles = getSystemConfigEnabledProfilesForPackage(ctx);

        ArrayList<Class> profiles = new ArrayList<>(PROFILE_SERVICES_AND_FLAGS.length);
        for (ProfileConfig config : PROFILE_SERVICES_AND_FLAGS) {
            boolean supported = false;
            if (config.mClass == HearingAidService.class) {
                supported =
                        BluetoothProperties.isProfileAshaCentralEnabled().orElse(false);
            } else {
                supported = config.mSupported > 0 ? resources.getBoolean(config.mSupported) : false;
            }

            if (!supported && (config.mClass == HearingAidService.class)
                    && isFeatureEnabled(ctx, FEATURE_HEARING_AID, /*defaultValue=*/false)) {
                Log.i(TAG, "Feature Flag enables support for HearingAidService");
                supported = true;
            }

            if (config.mClass == BatteryService.class) {
                // It could be overridden by flag.
                supported = isFeatureEnabled(ctx, FEATURE_BATTERY, /*defaultValue=*/ supported);
            }

            if (enabledProfiles != null && enabledProfiles.contains(config.mClass.getName())) {
                supported = true;
                Log.i(TAG, config.mClass.getSimpleName() + " Feature Flag set to " + supported
                        + " by components configuration");
            }

            if (supported && !isProfileDisabled(ctx, config.mMask)) {
                Log.i(TAG, "Adding " + config.mClass.getSimpleName());
                profiles.add(config.mClass);
            }
        }
        sSupportedProfiles = profiles.toArray(new Class[profiles.size()]);
        sIsGdEnabledUptoScanningLayer = resources.getBoolean(R.bool.enable_gd_up_to_scanning_layer);
    }

    /**
     * Remove the input profiles from the supported list.
     */
    static void removeProfileFromSupportedList(HashSet<Class> nonSupportedProfiles) {
        ArrayList<Class> profilesList = new ArrayList<Class>(Arrays.asList(sSupportedProfiles));
        Iterator<Class> iter = profilesList.iterator();

        while (iter.hasNext()) {
            Class profileClass = iter.next();

            if (nonSupportedProfiles.contains(profileClass)) {
                iter.remove();
                Log.v(TAG, "Remove " + profileClass.getSimpleName() + " from supported list.");
            }
        }

        sSupportedProfiles = profilesList.toArray(new Class[profilesList.size()]);
    }

    static void addSupportedProfile(int supportedProfile) {
        sSupportedMask |= (1 << supportedProfile);
    }

    static HashSet<Class> geLeAudioUnicastProfiles() {
        return mLeAudioUnicastProfiles;
    }

    static Class[] getSupportedProfiles() {
        return sSupportedProfiles;
    }

    static boolean isGdEnabledUpToScanningLayer() {
        return sIsGdEnabledUptoScanningLayer;
    }

    private static long getProfileMask(Class profile) {
        for (ProfileConfig config : PROFILE_SERVICES_AND_FLAGS) {
            if (config.mClass == profile) {
                return config.mMask;
            }
        }
        Log.w(TAG, "Could not find profile bit mask for " + profile.getSimpleName());
        return 0;
    }

    static long getSupportedProfilesBitMask() {
        long mask = sSupportedMask;
        for (final Class profileClass : getSupportedProfiles()) {
            mask |= getProfileMask(profileClass);
        }
        return mask;
    }

    private static boolean isProfileDisabled(Context context, long profileMask) {
        final ContentResolver resolver = context.getContentResolver();
        final long disabledProfilesBitMask =
                Settings.Global.getLong(resolver, Settings.Global.BLUETOOTH_DISABLED_PROFILES, 0);

        return (disabledProfilesBitMask & profileMask) != 0;
    }

    private static boolean isHearingAidSettingsEnabled(Context context) {
        final String flagOverridePrefix = "sys.fflag.override.";
        final String hearingAidSettings = "settings_bluetooth_hearing_aid";

        return isFeatureEnabled(context, hearingAidSettings, /*defaultValue=*/false);
    }

    private static boolean isFeatureEnabled(Context context, String feature, boolean defaultValue) {
        final String flagOverridePrefix = "sys.fflag.override.";
        // Override precedence:
        // Settings.Global -> sys.fflag.override.* -> static list

        // Step 1: check if feature flag is set in Settings.Global.
        String value;
        if (context != null) {
            value = Settings.Global.getString(context.getContentResolver(), feature);
            if (!TextUtils.isEmpty(value)) {
                return Boolean.parseBoolean(value);
            }
        }

        // Step 2: check if feature flag has any override.
        // Flag name: sys.fflag.override.<feature>
        value = SystemProperties.get(flagOverridePrefix + feature);
        if (!TextUtils.isEmpty(value)) {
            return Boolean.parseBoolean(value);
        }
        // Step 3: return default value
        return defaultValue;
    }

    private static List<String> getSystemConfigEnabledProfilesForPackage(Context ctx) {
        SystemConfigManager systemConfigManager = ctx.getSystemService(SystemConfigManager.class);
        if (systemConfigManager == null) {
            return null;
        }
        List<String> enabledComponent = new ArrayList<>();
        for (ComponentName comp :
                systemConfigManager.getEnabledComponentOverrides(ctx.getPackageName())) {
            enabledComponent.add(comp.getClassName());
        }
        return enabledComponent;
    }
}
