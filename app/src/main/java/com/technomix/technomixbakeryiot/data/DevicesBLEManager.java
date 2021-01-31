package com.technomix.technomixbakeryiot.data;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

import com.technomix.technomixbakeryiot.data.charscallback.TemperatureDataCallback;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;

class DevicesBLEManager extends ObservableBleManager {
    private static final UUID BAKERY_IOT_SERVICE_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID BAKERY_IOT_CHAR_TEMPERATURE_UUID = UUID.fromString("a7ff642e-2ce5-49e2-8255-9fcfd028fa21");
    private static final UUID BAKERY_IOT_CHAR_MANUFACTURER_UUID = UUID.fromString("4167741F-FD2F-4608-9F77-4DD94888F34F");
    private static final UUID BAKERY_IOT_CHAR_MODEL_UUID = UUID.fromString("8B98FFD3-1D3F-4993-9136-2C30FC9658D5");
    private static final UUID BAKERY_IOT_CHAR_WIFI_SID_UUID = UUID.fromString("0e26269d-53f9-4760-b764-0f24e5ea8d87");
    private static final UUID BAKERY_IOT_CHAR_WIFI_PWD_UUID = UUID.fromString("2920f967-4444-45a2-b2da-a5350eb31874");

    private BluetoothGattCharacteristic charTemperature, charWiFiSID, charWiFiPWD, charModel, charManufacturer;

    public DevicesBLEManager(@NotNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new DeviceBleManagerGattCallback();
    }

    private final TemperatureDataCallback temperatureCallback = new TemperatureDataCallback() {


        @Override
        public void onTemperatureChanged(@NonNull BluetoothDevice device, float temp) {

        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };
    private class DeviceBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            setNotificationCallback(charTemperature).with(temperatureCallback);
            readCharacteristic(charTemperature).with(temperatureCallback).enqueue();
            enableNotifications(charTemperature).enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(BAKERY_IOT_SERVICE_UUID);
            if (service != null) {
                charTemperature = service.getCharacteristic(BAKERY_IOT_CHAR_TEMPERATURE_UUID);
                charWiFiSID = service.getCharacteristic(BAKERY_IOT_CHAR_WIFI_SID_UUID);
                charWiFiPWD = service.getCharacteristic(BAKERY_IOT_CHAR_WIFI_PWD_UUID);
            }
            return charTemperature != null && charWiFiSID != null && charWiFiPWD != null;
        }

        @Override
        protected void onDeviceDisconnected() {
            charTemperature = null;
            charWiFiSID = null;
            charWiFiPWD = null;
            charModel = null;
            charManufacturer = null;
        }
    }
}
