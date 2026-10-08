package com.shilapi.xcertplay.compat

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import com.shilapi.xcertplay.hud.BydOutputSettings

/**
 * Read-only snapshot of services exposed to this Android process.
 *
 * Feature declarations are diagnostic only because vendor VMs sometimes omit them. Runtime
 * decisions use the corresponding service/adapter instead of assuming the main car OS hardware is
 * visible inside the VM.
 */
data class HeadUnitCapabilities(
    val sdkInt: Int,
    val bydHardware: Boolean,
    val usbHostFeature: Boolean,
    val usbService: Boolean,
    val bluetoothFeature: Boolean,
    val bluetoothService: Boolean,
    val bluetoothAdapter: Boolean,
    val wifiFeature: Boolean,
    val wifiService: Boolean,
    val wifiDirectFeature: Boolean,
    val wifiDirectService: Boolean,
    val audioService: Boolean,
    val audioOutputTypes: List<Int>,
) {
    fun diagnosticSummary(): String =
        "Head-unit capabilities api=$sdkInt byd=$bydHardware " +
            "usbHost(feature=$usbHostFeature,service=$usbService) " +
            "bluetooth(feature=$bluetoothFeature,service=$bluetoothService,adapter=$bluetoothAdapter) " +
            "wifi(feature=$wifiFeature,service=$wifiService) " +
            "wifiDirect(feature=$wifiDirectFeature,service=$wifiDirectService) " +
            "audio(service=$audioService,outputTypes=${audioOutputTypes.joinToString(",").ifEmpty { "none-reported" }})"
}

object HeadUnitCapabilityDetector {
    fun detect(context: Context): HeadUnitCapabilities {
        val app = context.applicationContext
        val packages = app.packageManager
        val bluetooth = runCatching { app.getSystemService(BluetoothManager::class.java) }.getOrNull()
        val audio = runCatching { app.getSystemService(AudioManager::class.java) }.getOrNull()
        val outputTypes = runCatching {
            audio?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                ?.map { it.type }
                ?.distinct()
                ?.sorted()
                .orEmpty()
        }.getOrDefault(emptyList())
        return HeadUnitCapabilities(
            sdkInt = Build.VERSION.SDK_INT,
            bydHardware = BydOutputSettings.available(app),
            usbHostFeature = packages.hasFeature(PackageManager.FEATURE_USB_HOST),
            usbService = runCatching { app.getSystemService(UsbManager::class.java) != null }.getOrDefault(false),
            bluetoothFeature = packages.hasFeature(PackageManager.FEATURE_BLUETOOTH),
            bluetoothService = bluetooth != null,
            bluetoothAdapter = runCatching { bluetooth?.adapter != null }.getOrDefault(false),
            wifiFeature = packages.hasFeature(PackageManager.FEATURE_WIFI),
            wifiService = runCatching { app.getSystemService(WifiManager::class.java) != null }.getOrDefault(false),
            wifiDirectFeature = packages.hasFeature(PackageManager.FEATURE_WIFI_DIRECT),
            wifiDirectService = runCatching {
                app.getSystemService(WifiP2pManager::class.java) != null
            }.getOrDefault(false),
            audioService = audio != null,
            audioOutputTypes = outputTypes,
        )
    }
}
