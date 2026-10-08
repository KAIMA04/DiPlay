package com.shilapi.xcertplay.orchestration

import org.junit.Assert.assertEquals
import org.junit.Test

class WirelessHotspotFallbackPlanTest {
    @Test
    fun wifiDirectFallsBackOnlyToAlreadyConfiguredNetworks() {
        assertEquals(
            listOf(
                WirelessHotspotMode.WIFI_P2P,
                WirelessHotspotMode.EXISTING_WIFI,
                WirelessHotspotMode.MANUAL,
            ),
            plan(
                preferred = WirelessHotspotMode.WIFI_P2P,
                manual = true,
                existing = true,
            ),
        )
        assertEquals(
            listOf(WirelessHotspotMode.WIFI_P2P),
            plan(preferred = WirelessHotspotMode.WIFI_P2P),
        )
    }

    @Test
    fun carHotspotCanUseWifiDirectWhenTheServiceExists() {
        assertEquals(
            listOf(WirelessHotspotMode.MANUAL, WirelessHotspotMode.WIFI_P2P),
            plan(
                preferred = WirelessHotspotMode.MANUAL,
                p2p = true,
            ),
        )
    }

    @Test
    fun existingWifiDoesNotEscalateToAPermissionItDidNotRequest() {
        assertEquals(
            listOf(WirelessHotspotMode.EXISTING_WIFI, WirelessHotspotMode.MANUAL),
            plan(
                preferred = WirelessHotspotMode.EXISTING_WIFI,
                p2p = true,
                manual = true,
            ),
        )
    }

    private fun plan(
        preferred: WirelessHotspotMode,
        p2p: Boolean = false,
        manual: Boolean = false,
        existing: Boolean = false,
    ) = WirelessHotspotFallbackPlan.modes(
        preferred = preferred,
        wifiP2pServiceAvailable = p2p,
        manualConfigured = manual,
        existingWifiConfigured = existing,
    )
}
