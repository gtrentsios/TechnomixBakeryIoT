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

package com.technomix.technomixbakeryiot.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.UUID;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import com.technomix.technomixbakeryiot.profile.callback.ModelNameDataCallback;
import com.technomix.technomixbakeryiot.profile.callback.TemperatureDataCallback;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class MD360Manager extends ObservableBleManager {
	public static final UUID BAKERY_IOT_SERVICE_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
	public static final UUID BAKERY_IOT_CHAR_TEMPERATURE_UUID = UUID.fromString("a7ff642e-2ce5-49e2-8255-9fcfd028fa21");
	public static final UUID BAKERY_IOT_CHAR_MANUFACTURER_UUID = UUID.fromString("4167741F-FD2F-4608-9F77-4DD94888F34F");
	public static final UUID BAKERY_IOT_CHAR_MODEL_UUID = UUID.fromString("8B98FFD3-1D3F-4993-9136-2C30FC9658D5");
	public static final UUID BAKERY_IOT_CHAR_WIFI_SID_UUID = UUID.fromString("0e26269d-53f9-4760-b764-0f24e5ea8d87");
	public static final UUID BAKERY_IOT_CHAR_WIFI_PWD_UUID = UUID.fromString("2920f967-4444-45a2-b2da-a5350eb31874");
	private BluetoothGattCharacteristic modelCharacteristic, temperatureCharacteristic;

	private final MutableLiveData<Float> temperature = new MutableLiveData<Float>();
	private final MutableLiveData<String> modelName = new MutableLiveData<>();


	private LogSession logSession;
	private boolean supported;
	private boolean ledOn;

	public MD360Manager(@NonNull final Context context) {
		super(context);
	}

	public final LiveData<Float> getTemperature() {
		return temperature;
	}

	public final LiveData<String> getModelName() {
		return modelName;
	}

	@NonNull
	@Override
	protected BleManagerGattCallback getGattCallback() {
		return new MD360BleManagerGattCallback();
	}

	/**
	 * Sets the log session to be used for low level logging.
	 * @param session the session, or null, if nRF Logger is not installed.
	 */
	public void setLogger(@Nullable final LogSession session) {
		logSession = session;
	}

	@Override
	public void log(final int priority, @NonNull final String message) {
		// The priority is a Log.X constant, while the Logger accepts it's log levels.
		Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message);
	}

	@Override
	protected boolean shouldClearCacheWhenDisconnected() {
		return !supported;
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
	private	final ModelNameDataCallback buttonCallback = new ModelNameDataCallback() {
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

			supported = modelCharacteristic != null && temperatureCharacteristic != null ;
			return supported;
		}

		@Override
		protected void onDeviceDisconnected() {
			modelCharacteristic = null;
			temperatureCharacteristic = null;
		}
	}

	/**
	 * Sends a request to the device to turn the LED on or off.
	 *
	 * @param temp true to turn the LED on, false to turn it off.
	 * @param temp
	 */
	public void tempChanged(final String temp) {

	}
}
