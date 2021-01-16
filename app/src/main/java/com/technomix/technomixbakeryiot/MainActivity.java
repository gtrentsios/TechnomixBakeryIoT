package com.technomix.technomixbakeryiot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.technomix.technomixbakeryiot.ui.main.MainFragment;
import com.technomix.technomixbakeryiot.utils.BluetoothConfiguration;
import com.technomix.technomixbakeryiot.utils.BluetoothLeService;
import com.technomix.technomixbakeryiot.utils.BluetoothService;
import com.technomix.technomixbakeryiot.utils.BluetoothStatus;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback {
    private BluetoothService mBLEService;
    private static final int ACCESS_FINE_LOCATION = 1;
    private static final int ACCESS_COARSE_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }

    }

    private void configBluetoothLeService() {
        BluetoothConfiguration config = new BluetoothConfiguration();
        config.context = getApplicationContext();
        config.bluetoothServiceClass = BluetoothLeService.class;
        config.bufferSize = 1024;
        config.characterDelimiter = '\n';
        config.deviceName = "Your App Name";
        config.callListenersInMainThread = true;
        config.uuidService = UUID.fromString("e7810a71-73ae-499d-8c15-faa9aef0c3f2"); // Required
        config.uuidCharacteristic = UUID.fromString("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f"); // Required
        config.transport = BluetoothDevice.TRANSPORT_LE; // Required for dual-mode devices
        config.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Used to filter found devices. Set null to find all devices.
        BluetoothLeService.init(config);
        mBLEService = BluetoothLeService.getDefaultInstance();
    }

    @Override
    public void onDataRead(byte[] buffer, int length) {

    }

    @Override
    public void onStatusChange(BluetoothStatus status) {

    }

    @Override
    public void onDeviceName(String deviceName) {

    }

    @Override
    public void onToast(String message) {

    }

    @Override
    public void onDataWrite(byte[] buffer) {

    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi) {

    }

    @Override
    public void onStartScan() {

    }

    @Override
    public void onStopScan() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
/*
   Permissions methods
 */
    void checkBackgroundLocationPermission() {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //Android 10 and higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, ACCESS_COARSE_LOCATION);
            }
        }
    }
    void checkLocationPermission() {
        boolean hasPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //Android 9 and higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, ACCESS_FINE_LOCATION );
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION }, ACCESS_COARSE_LOCATION );
            }
        }
    }
}