/**
 * @Title: BlueService.java
 * @Package com.mbelec.doctorm.service
 * @Description: TODO
 * @author star
 * @date 2014-11-7 ����11:14:36
 * @version V1.0
 */

package com.star.ble.service;

import java.util.List;
import java.util.UUID;

import com.star.ble.app.MyGattCharacteristic;
import com.star.ble.app.MyGattDescriptor;
import com.star.ble.app.MyGattService;
import com.star.ble.utils.Utils;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class BlueService extends Service {
	private final static String TAG = BlueService.class.getSimpleName();
	
	public static String SAMPLE_CHARACTERISTIC= "0000ff0a-0000-1000-8000-00805f9b34fb";
	// BLE
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
	/* GATT ACTION */
	public final static String ACTION_GATT_RECONNECT = "com.star.ble.service.ACTION_GATT_RECONNECT";
	public final static String ACTION_GATT_CONNECTED = "com.star.ble.service.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.star.ble.service.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.star.ble.service.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.star.ble.service.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.star.ble.service.EXTRA_DATA";

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			super.onConnectionStateChange(gatt, status, newState);	
			Log.i(TAG, "-------------->onConnectionStateChange:");
			
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				 mConnectionState = STATE_CONNECTED;
				broadcastUpdate(ACTION_GATT_CONNECTED);
				Log.i(TAG, "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());
				
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.w(TAG, "Disconnected from GATT server successfully !");
				  mConnectionState = STATE_DISCONNECTED;
				broadcastUpdate(ACTION_GATT_DISCONNECTED);	
			} 
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(TAG, "-------------->onServicesDiscovered:");
				
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);		
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			
			Log.i(TAG, "-------------->onCharacteristicRead:");	
			if (status == BluetoothGatt.GATT_SUCCESS){
				broadcastAvailableData(ACTION_DATA_AVAILABLE, characteristic);
				Log.d(TAG,
						Utils.bytesToHexString(characteristic.getValue()));
			}else {
				Log.w(TAG, "BluetoothGatt.GATT_Failed");
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			
			Log.i(TAG, "-------------->onCharacteristicWrite:");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.w(TAG,
						"Characteristic has been written ! Value: "
								+ Utils.bytesToHexString(characteristic
										.getValue()));
			} else {
				Log.w(TAG, "write failed ! " + status);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			
			Log.i(TAG, "-------------->onCharacteristicChanged:");

			broadcastAvailableData(ACTION_DATA_AVAILABLE, characteristic);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorWrite(gatt, descriptor, status);
			
			Log.i(TAG, "-------------->onDescriptorWrite:");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.i(TAG,
						"descriptor has been written ! "
								+ descriptor.getValue());
			
			} else {
				Log.w(TAG, "write failed ! ");
			}
		}

	};



	/**
	 * @Title broadcastAvailableData 
	 * @Description TODO
	 * @param actionDataAvailable
	 * @param characteristic 
	 * @return void
	 */
	private void broadcastAvailableData(final String action,
			final BluetoothGattCharacteristic characteristic) {
		 final Intent intent = new Intent(action);
		 
		// This is special handling for the Heart Rate Measurement profile.  Data parsing is
	        // carried out as per profile specifications:
	        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
	        if (MyGattCharacteristic.HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
	            int flag = characteristic.getProperties();
	            int format = -1;
	            if ((flag & 0x01) != 0) {
	                format = BluetoothGattCharacteristic.FORMAT_UINT16;
	                Log.d(TAG, "Heart rate format UINT16.");
	            } else {
	                format = BluetoothGattCharacteristic.FORMAT_UINT8;
	                Log.d(TAG, "Heart rate format UINT8.");
	            }
	            final int heartRate = characteristic.getIntValue(format, 1);
	            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
	            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
	        }else if (MyGattCharacteristic.BATTERY_LEVEL.equals(characteristic.getUuid())) {
	        	int flag = characteristic.getProperties();
	        	int format = -1;
	        	if ((flag & 0x01) != 0) {
	                format = BluetoothGattCharacteristic.FORMAT_UINT16;
	                Log.d(TAG, "Battery level format UINT16.");
	            } else {
	                format = BluetoothGattCharacteristic.FORMAT_UINT8;
	                Log.d(TAG, "Battery level format UINT8.");
	            }
	            final int batteryLevel = characteristic.getIntValue(format, 1);
	            Log.d(TAG, String.format("Received battery level: %d", batteryLevel));
	            intent.putExtra(EXTRA_DATA, String.valueOf(batteryLevel));
	        }else {
	            // For all other profiles, writes the data formatted in HEX.
	            final byte[] data = characteristic.getValue();
	            if (data != null && data.length > 0) {
	                final StringBuilder stringBuilder = new StringBuilder(data.length);
	                for(byte byteChar : data)
	                    stringBuilder.append(String.format("%02X ", byteChar));
	                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
	            }
	        }
	        sendBroadcast(intent);
		 
	}

	/**
	 * @Title broadcastUpdate 
	 * @Description TODO
	 * @param actionGattConnected 
	 * @return void
	 */
	private void broadcastUpdate(String action) {
		// TODO Auto-generated method stub
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}
	
	
	  /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
	
	  /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        
        // This is specific to Heart Rate Measurement.
        if (MyGattCharacteristic.HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
            		MyGattDescriptor.CLIENT_CHARACTERISTIC_CONFIGURATION);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
    	 if (mBluetoothAdapter == null || mBluetoothGatt == null) {
             Log.w(TAG, "BluetoothAdapter not initialized");
             return;
    	    }
          // This is specific to custom characteristic.
             if (UUID.fromString(SAMPLE_CHARACTERISTIC).equals(characteristic.getUuid())) {
            	 byte[] value = {0x55, 0x01};
            	 characteristic.setValue(value);
                 mBluetoothGatt.writeCharacteristic(characteristic);
             }

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    
	/***************** Bind Service **************************/
	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public BlueService getService() {
			return BlueService.this;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		close();
		return super.onUnbind(intent);

	}

	/**
	 * @Title close
	 * @Description TODO
	 * @return void
	 */
	private void close() {
		
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * @Title initialize
	 * @Description initialize initialize BluetoothManager and BluetoothAdapter
	 * @return boolean
	 */
	public boolean initialize() {

		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.w(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.w(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}
		return true;
	}

	/**
	 * @Title connectBle
	 * @Description connect to ble device , can be invoked by others
	 * @param address
	 *            that need to be connected
	 * @return boolean return true if connect successfully else return false
	 */
	public boolean connectBle(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}
		if (mBluetoothDeviceAddress != null
				&& address.equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.i(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			  if (mBluetoothGatt.connect()) {
	                mConnectionState = STATE_CONNECTING;
	                return true;
	            } else {
	                return false;
	            }
		}
		final BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		 // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

		Log.d(TAG, "Trying to create a new connection.");
		mBluetoothDeviceAddress = address;
		 mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * @Title disconnect
	 * @Description Disconnects an established connection
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
		this.close();
		Log.w(TAG, "Disconnects an established connection ");
	}

}
