package com.shilapi.xcertplay.orchestration

/**
 * Safe wireless alternatives that use only details the driver has already saved.
 *
 * Existing-Wi-Fi mode asks for fewer runtime permissions, so it never silently escalates to Wi-Fi
 * Direct. Other modes may fall back to an already configured LAN or car hotspot.
 */
internal object WirelessHotspotFallbackPlan {
    fun modes(
        preferred: WirelessHotspotMode,
        wifiP2pServiceAvailable: Boolean,
        manualConfigured: Boolean,
        existingWifiConfigured: Boolean,
    ): List<WirelessHotspotMode> = buildList {
        fun addOnce(mode: WirelessHotspotMode) {
            if (mode !in this) add(mode)
        }

        addOnce(preferred)
        when (preferred) {
            WirelessHotspotMode.WIFI_P2P -> {
                if (existingWifiConfigured) addOnce(WirelessHotspotMode.EXISTING_WIFI)
                if (manualConfigured) addOnce(WirelessHotspotMode.MANUAL)
            }
            WirelessHotspotMode.MANUAL -> {
                if (wifiP2pServiceAvailable) addOnce(WirelessHotspotMode.WIFI_P2P)
                if (existingWifiConfigured) addOnce(WirelessHotspotMode.EXISTING_WIFI)
            }
            WirelessHotspotMode.EXISTING_WIFI -> {
                if (manualConfigured) addOnce(WirelessHotspotMode.MANUAL)
            }
            WirelessHotspotMode.LOCAL_ONLY_HOTSPOT -> {
                if (wifiP2pServiceAvailable) addOnce(WirelessHotspotMode.WIFI_P2P)
                if (existingWifiConfigured) addOnce(WirelessHotspotMode.EXISTING_WIFI)
                if (manualConfigured) addOnce(WirelessHotspotMode.MANUAL)
            }
        }
    }
}
