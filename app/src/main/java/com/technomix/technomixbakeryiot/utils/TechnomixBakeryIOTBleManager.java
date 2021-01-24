package com.technomix.technomixbakeryiot.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.UUID;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.PhyRequest;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

class TechnomixBakeryIOTBleManager  extends BleManager {
    private static final boolean DEBUG = true;
    private static final UUID  BAKERY_IOT_SERVICE_UUID  = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID BAKERY_IOT_CHAR_TEMPERATURE_UUID  = UUID.fromString("a7ff642e-2ce5-49e2-8255-9fcfd028fa21");
    private static final UUID BAKERY_INT_CHAR_MANUFACTURER_UUID = UUID.fromString("4167741F-FD2F-4608-9F77-4DD94888F34F");
    private static final UUID BAKERY_IOT_CHAR_MODEL_UUID  = UUID.fromString("8B98FFD3-1D3F-4993-9136-2C30FC9658D5");
    private static final UUID  BAKERY_IOT_CHAR_WIFI_SID_UUID  = UUID.fromString("0e26269d-53f9-4760-b764-0f24e5ea8d87");
    private static final UUID  BAKERY_IOT_CHAR_WIFI_PWD_UUID  = UUID.fromString("2920f967-4444-45a2-b2da-a5350eb31874");
    // Client characteristics
    private BluetoothGattCharacteristic charTemp, charWifiSID, charWfiPwd, charManifacturer, charModel;

    TechnomixBakeryIOTBleManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        if (DEBUG || priority == Log.ERROR) {
            Log.println(priority, "MyBleManager", message);
        }
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private class MyManagerGattCallback extends BleManagerGattCallback {

        // This method will be called when the device is connected and services are discovered.
        // You need to obtain references to the characteristics and descriptors that you will use.
        // Return true if all required services are found, false otherwise.
        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(BAKERY_IOT_SERVICE_UUID);
            if (service != null) {
                charTemp = service.getCharacteristic(BAKERY_IOT_CHAR_TEMPERATURE_UUID);
                charWifiSID = service.getCharacteristic(BAKERY_IOT_CHAR_WIFI_SID_UUID);
                charWfiPwd = service.getCharacteristic(BAKERY_IOT_CHAR_WIFI_PWD_UUID);
                charManifacturer = service.getCharacteristic(BAKERY_INT_CHAR_MANUFACTURER_UUID);
                charModel = service.getCharacteristic(BAKERY_IOT_CHAR_MODEL_UUID);
            }
            // Validate properties
            boolean notify = false;
            if (charTemp != null) {
                final int properties = charTemp.getProperties();
                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
            }
            boolean writeRequest = false;
            if (charWifiSID != null) {
                final int properties = charWifiSID.getProperties();
                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
                charWifiSID.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
            // Return true if all required services have been found
            return charTemp != null && charWifiSID != null
                    && notify && writeRequest;
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        @Override
        protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            return super.isOptionalServiceSupported(gatt);
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        @Override
        protected void initialize() {
            // You may enqueue multiple operations. A queue ensures that all operations are
            // performed one after another, but it is not required.
            beginAtomicRequestQueue()
                    .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
                    .add(enableNotifications(charTemp))
                    .done(device -> log(Log.INFO, "Target initialized"))
                    .enqueue();
            // You may easily enqueue more operations here like such:
            writeCharacteristic(charWifiSID, "Hello World!".getBytes())
                    .done(device -> log(Log.INFO, "Greetings sent"))
                    .enqueue();

            // If you need to send very long data using Write Without Response, use split()
            // or define your own splitter in split(DataSplitter splitter, WriteProgressCallback cb).
            writeCharacteristic(charWifiSID,   "Very, very long data that will no fit into MTU".getBytes())
                    .split()
                    .enqueue();
        }

        @Override
        protected void onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            charTemp = null;
            charWifiSID = null;
        }
    }

    // Define your API.

    private abstract class FluxHandler implements ProfileDataCallback {
        @Override
        public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
            // Some validation?
            if (data.size() != 1) {
                onInvalidDataReceived(device, data);
                return;
            }
            onFluxCapacitorEngaged();
        }

        abstract void onFluxCapacitorEngaged();
    }

    /** Initialize time machine. */
    public void enableFluxCapacitor(final int year) {
/*
        waitForNotification(charTemp)
                .trigger(
                        writeCharacteristic(charWifiSID, new FluxJumpRequest(year))
                                .done(device -> log(Log.INDO, "Power on command sent"))
                )
                .with(new FluxHandler() {
                    public void onFluxCapacitorEngaged() {
                        log(Log.WARN, "Flux Capacitor enabled! Going back to the future in 3 seconds!");

                        sleep(3000).enqueue();
                        writeCharacteristic(charWifiSID, "Hold on!".getBytes() )
                                .done(device -> log(Log.WARN, "It's " + year + "!"))
                                .fail((device, status) -> "Not enough flux? (status: " + status + ")")
                                .enqueue();
                    }
                })
                .enqueue();

 */

    }

    /**
     * Aborts time travel. Call during 3 sec after enabling Flux Capacitor and only if you don't
     * like 2020.
     */
    public void abort() {
        cancelQueue();
    }

}
