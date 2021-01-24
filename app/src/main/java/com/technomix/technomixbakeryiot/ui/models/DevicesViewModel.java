package com.technomix.technomixbakeryiot.ui.models;

import android.app.Activity;
import android.content.Context;
import android.os.ParcelUuid;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.technomix.technomixbakeryiot.R;
import com.technomix.technomixbakeryiot.data.ConnectedDevices;
import com.technomix.technomixbakeryiot.ui.adapters.DevicesAdapter;
import java.util.ArrayList;
import java.util.List;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class DevicesViewModel extends ViewModel {
    private static final ParcelUuid BAKERY_IOT_SERVICE_UUID = ParcelUuid.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final ParcelUuid BAKERY_IOT_CHAR_TEMPERATURE_UUID = ParcelUuid.fromString("a7ff642e-2ce5-49e2-8255-9fcfd028fa21");
    private static final ParcelUuid BAKERY_IOT_CHAR_MANUFACTURER_UUID = ParcelUuid.fromString("4167741F-FD2F-4608-9F77-4DD94888F34F");
    private static final ParcelUuid BAKERY_IOT_CHAR_MODEL_UUID = ParcelUuid.fromString("8B98FFD3-1D3F-4993-9136-2C30FC9658D5");
    private static final ParcelUuid BAKERY_IOT_CHAR_WIFI_SID_UUID = ParcelUuid.fromString("0e26269d-53f9-4760-b764-0f24e5ea8d87");
    private static final ParcelUuid BAKERY_IOT_CHAR_WIFI_PWD_UUID = ParcelUuid.fromString("2920f967-4444-45a2-b2da-a5350eb31874");
    private static DevicesAdapter mAdapter;


    private scanCallbackImplementation mScanCallbackImplementation = new scanCallbackImplementation();

    public void init(){
        mAdapter = new DevicesAdapter();
        ConnectedDevices connectedDevice = new ConnectedDevices();
        connectedDevice.setDeviceName("Device Name");
        connectedDevice.setDeviceAddress("Device Address");
        mAdapter.addDevice(connectedDevice);
        startScan();
    }
    public DevicesAdapter getAdapter(){
        return mAdapter;
    }
/*
    Bluetooth scanning methods
 */
    private void startScan(){
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(true)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(BAKERY_IOT_SERVICE_UUID).build());
       // scanner.startScan(filters, settings, mScanCallbackImplementation);
    }

    private static class scanCallbackImplementation extends ScanCallback {
        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(this);
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {

            if (!results.isEmpty()) {
                BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(this);
                for (int i=0; i < results.size(); i++){
                    ConnectedDevices connectedDevice = new ConnectedDevices();
                    connectedDevice.setDeviceName(results.get(i).getDevice().getName());
                    connectedDevice.setDeviceAddress(results.get(i).getDevice().getAddress());
                    mAdapter.addDevice(connectedDevice);
                }
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(this);
        }
    }

}