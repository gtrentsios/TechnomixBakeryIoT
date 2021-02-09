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

package com.technomix.technomixbakeryiot.ui.models;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import com.technomix.technomixbakeryiot.ui.adapters.DiscoveredBluetoothDevice;
import com.technomix.technomixbakeryiot.profile.MD360Manager;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.log.LogSession;


public class DeviceViewModel extends AndroidViewModel {
	private final MD360Manager md360Manager;
	private BluetoothDevice device;

	public DeviceViewModel(@NonNull final Application application) {
		super(application);

		// Initialize the manager.
		md360Manager = new MD360Manager(getApplication());
	}

	public LiveData<ConnectionState> getConnectionState() {
		return md360Manager.getState();
	}

	public LiveData<String> getModelName() {
		return md360Manager.getModelName();
	}

	public LiveData<Float> getTemperature() {
		return md360Manager.getTemperature();
	}

	/**
	 * Connect to the given peripheral.
	 *
	 * @param target the target device.
	 */
	public void connect(@NonNull final DiscoveredBluetoothDevice target) {
		// Prevent from calling again when called again (screen orientation changed).
		if (device == null) {
			device = target.getDevice();
			final LogSession logSession = Logger
					.newSession(getApplication(), null, target.getAddress(), target.getName());
			md360Manager.setLogger(logSession);
			reconnect();
		}
	}

	/**
	 * Reconnects to previously connected device.
	 * If this device was not supported, its services were cleared on disconnection, so
	 * reconnection may help.
	 */
	public void reconnect() {
		if (device != null) {
			md360Manager.connect(device)
					.retry(3, 100)
					.useAutoConnect(false)
					.enqueue();
		}
	}

	/**
	 * Disconnect from peripheral.
	 */
	private void disconnect() {
		device = null;
		md360Manager.disconnect().enqueue();
	}

	/**
	 * Sends a command to turn ON or OFF the LED on the nRF5 DK.
	 *
	 * @param temp true to turn the LED on, false to turn it OFF.
	 */
	public void setTempChanged(final String temp) {
		md360Manager.tempChanged(temp);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		if (md360Manager.isConnected()) {
			disconnect();
		}
	}
}
