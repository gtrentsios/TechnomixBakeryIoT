/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.technomix.technomixbakeryiot.ui.adapters;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.technomix.technomixbakeryiot.R;
import com.technomix.technomixbakeryiot.profile.MD360Manager;
import com.technomix.technomixbakeryiot.profile.callback.ModelNameDataCallback;
import com.technomix.technomixbakeryiot.profile.callback.TemperatureDataCallback;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class DiscoveredBluetoothDevice extends ObservableBleManager {
    public static final UUID BAKERY_IOT_SERVICE_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    public static final UUID BAKERY_IOT_CHAR_TEMPERATURE_UUID = UUID.fromString("a7ff642e-2ce5-49e2-8255-9fcfd028fa21");
    public static final UUID BAKERY_IOT_CHAR_MANUFACTURER_UUID = UUID.fromString("4167741F-FD2F-4608-9F77-4DD94888F34F");
    public static final UUID BAKERY_IOT_CHAR_MODEL_UUID = UUID.fromString("8B98FFD3-1D3F-4993-9136-2C30FC9658D5");
    public static final UUID BAKERY_IOT_CHAR_WIFI_SID_UUID = UUID.fromString("0e26269d-53f9-4760-b764-0f24e5ea8d87");
    public static final UUID BAKERY_IOT_CHAR_WIFI_PWD_UUID = UUID.fromString("2920f967-4444-45a2-b2da-a5350eb31874");
    private BluetoothGattCharacteristic modelCharacteristic, temperatureCharacteristic;
    private final BluetoothDevice device;
    private ScanResult lastScanResult;
    private String name;
    private int rssi;
    private int previousRssi;
    private int highestRssi = -128;
    private final MD360Manager md360Manager;
    private final MutableLiveData<Float> temperature = new MutableLiveData<Float>();
    private final MutableLiveData<String> modelName = new MutableLiveData<>();

    public DiscoveredBluetoothDevice(@NonNull final ScanResult scanResult, @NonNull final Context context) {
        super(context);
        this.device = scanResult.getDevice();
        update(scanResult);
        md360Manager = new MD360Manager(context);
        reconnect();
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MD360BleManagerGattCallback();
    }

    /**
     * The Button callback will be notified when a notification from Button characteristic
     * has been received, or its data was read.
     * <p>
     * If the data received are valid (single byte equal to 0x00 or 0x01), the
     * {@link ModelNameDataCallback#onModelNameChanged} will be called.
     * Otherwise, the {@link ModelNameDataCallback#onInvalidDataReceived(BluetoothDevice, Data)}
     * will be called with the data received.
     */
    private final ModelNameDataCallback buttonCallback = new ModelNameDataCallback() {
        @Override
        public void onModelNameChanged(@NonNull BluetoothDevice device, String Model) {
            modelName.setValue(Model);
        }


        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    /**
     * The LED callback will be notified when the LED state was read or sent to the target device.
     * <p>
     * This callback implements both {@link no.nordicsemi.android.ble.callback.DataReceivedCallback}
     * and {@link no.nordicsemi.android.ble.callback.DataSentCallback} and calls the same
     * method on success.
     * <p>
     * If the data received were invalid, the
     * {@link TemperatureDataCallback#onInvalidDataReceived(BluetoothDevice, Data)} will be
     * called.
     */
    private final TemperatureDataCallback ledCallback = new TemperatureDataCallback() {
        @Override
        public void onTemperatureChanged(@NonNull final BluetoothDevice device,
                                         final float iTemperature) {

            log(LogContract.Log.Level.APPLICATION, "Temperature " + temperature);
            temperature.setValue(iTemperature);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            // Data can only invalid if we read them. We assume the app always sends correct data.
            log(Log.WARN, "Invalid data received: " + data);
        }
    };


    @NonNull
    public BluetoothDevice getDevice() {
        return device;
    }

    @NonNull
    public String getAddress() {
        return device.getAddress();
    }

    @Nullable
    public String getName() {
        return name;
    }

    @SuppressWarnings("WeakerAccess")
    public int getRssi() {
        return rssi;
    }

    @NonNull
    public ScanResult getScanResult() {
        return lastScanResult;
    }

    /**
     * Returns the highest recorded RSSI value during the scan.
     *
     * @return Highest RSSI value.
     */
    public int getHighestRssi() {
        return highestRssi;
    }

    /**
     * This method returns true if the RSSI range has changed. The RSSI range depends on drawable
     * levels from {@link com.technomix.technomixbakeryiot.R.drawable#ic_signal_bar}.
     *
     * @return True, if the RSSI range has changed.
     */
    /* package */ boolean hasRssiLevelChanged() {
        final int newLevel =
                rssi <= 10 ?
                        0 :
                        rssi <= 28 ?
                                1 :
                                rssi <= 45 ?
                                        2 :
                                        rssi <= 65 ?
                                                3 :
                                                4;
        final int oldLevel =
                previousRssi <= 10 ?
                        0 :
                        previousRssi <= 28 ?
                                1 :
                                previousRssi <= 45 ?
                                        2 :
                                        previousRssi <= 65 ?
                                                3 :
                                                4;
        return newLevel != oldLevel;
    }

    /**
     * Updates the device values based on the scan result.
     *
     * @param scanResult the new received scan result.
     */
    public void update(@NonNull final ScanResult scanResult) {
        lastScanResult = scanResult;
        name = scanResult.getScanRecord() != null ?
                scanResult.getScanRecord().getDeviceName() : null;
        previousRssi = rssi;
        rssi = scanResult.getRssi();
        if (highestRssi < rssi)
            highestRssi = rssi;
    }

    public boolean matches(@NonNull final ScanResult scanResult) {
        return device.getAddress().equals(scanResult.getDevice().getAddress());
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof DiscoveredBluetoothDevice) {
            final DiscoveredBluetoothDevice that = (DiscoveredBluetoothDevice) o;
            return device.getAddress().equals(that.device.getAddress());
        }
        return super.equals(o);
    }


    public void reconnect() {
        if (this.device != null) {
            md360Manager.connect(device)
                    .retry(3, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private class MD360BleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            setNotificationCallback(modelCharacteristic).with(buttonCallback);
            setNotificationCallback(temperatureCharacteristic).with(ledCallback);
            readCharacteristic(temperatureCharacteristic).with(ledCallback).enqueue();
            readCharacteristic(modelCharacteristic).with(buttonCallback).enqueue();
            enableNotifications(modelCharacteristic).enqueue();
            enableNotifications(temperatureCharacteristic).enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(BAKERY_IOT_SERVICE_UUID);
            if (service != null) {
                modelCharacteristic = service.getCharacteristic(BAKERY_IOT_CHAR_MODEL_UUID);
                temperatureCharacteristic = service.getCharacteristic(BAKERY_IOT_CHAR_TEMPERATURE_UUID);
            }

            boolean writeRequest = false;
            if (temperatureCharacteristic != null) {
                final int rxProperties = temperatureCharacteristic.getProperties();
                writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }

            boolean supported = modelCharacteristic != null && temperatureCharacteristic != null;
            return supported;
        }

        @Override
        protected void onDeviceDisconnected() {
            modelCharacteristic = null;
            temperatureCharacteristic = null;
        }
    }

}
