package com.star.ble.fragment;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.star.ble.R;
import com.star.ble.model.BleDeviceInfo;

public class DevicesListFragment extends ListFragment {


	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;
	/**BLE*/
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private BleDeviceInfo mDeviceInfo;

	/**GUI*/
	private MenuItem mItemProgress;
	private MenuItem mItemSwitch;

	private Activity mActivity;

	private OnDeviceConListener mCallback;
	// The container Activity must implement this interface so the frag can
	// deliver messages
	public interface OnDeviceConListener {

//		public void onDeviceCon(String address);
		public void onDeviceCon(BleDeviceInfo deviceInfo);
	}
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);	
		mActivity = activity;
		
		// This makes sure that the container activity has implemented
				// the callback interface. If not, it throws an exception.
				try {
					mCallback = (OnDeviceConListener) activity;
				} catch (ClassCastException e) {
					throw new ClassCastException(activity.toString()
							+ " must implement OnDeviceConListener");
				}
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	//  your fragments menu items will be displayed in the ActionBar
			setHasOptionsMenu(true);
			
			mHandler = new Handler();
			initBle();
	}


	/* Check BLE */
	private void initBle() {
		// Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
		if (!mActivity.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(mActivity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			mActivity.finish();
		}
		 // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) mActivity
				.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		 // Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(mActivity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			mActivity.finish();
			return;
		}

	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		View view = inflater.inflate(R.layout.devices_list_fragment, container,
				false);

		return view;
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		//request to turn on Bluetooth
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter(mActivity);
		setListAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);
	}


	/**
	 * check if scan devices
	 * @param enable
	 */
	private void scanLeDevice(boolean enable) {
		
		  if (enable) {
	            // Stops scanning after a pre-defined scan period.
	            mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                    mScanning = false;
	                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
	                    mActivity.invalidateOptionsMenu();
	                }
	            }, SCAN_PERIOD);

	            mScanning = true;
	            mBluetoothAdapter.startLeScan(mLeScanCallback);

	        } else {
	            mScanning = false;
	            mBluetoothAdapter.stopLeScan(mLeScanCallback);

	        }
		  mActivity.invalidateOptionsMenu();
	}
	
	 // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
        	mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	mDeviceInfo = new BleDeviceInfo(device,rssi);
                    mLeDeviceListAdapter.addDevice(mDeviceInfo);     
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };



	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		scanLeDevice(false);
	     mLeDeviceListAdapter.clear();
	}




	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		  final BleDeviceInfo deviceInfo = mLeDeviceListAdapter.getDevice(position);
	        if (deviceInfo == null) return;
	        if (mScanning) {
	            mBluetoothAdapter.stopLeScan(mLeScanCallback);
	            mScanning = false;
	        }
//	        mBLE.connect(device.getAddress());
	
	        mCallback.onDeviceCon(deviceInfo);
	}


	


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu, android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.menu_list_fragment, menu);
		mItemProgress = menu.findItem(R.id.menu_progress);
		mItemSwitch = menu.findItem(R.id.menu_switch);

		if (mScanning) {
			mItemProgress.setVisible(true);
			mItemSwitch.setTitle(R.string.menu_stop);
		} else {
			mItemProgress.setVisible(false);
			mItemSwitch.setTitle(R.string.menu_scan);
		}
	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_switch:
			if (mScanning) {
				scanLeDevice(false);
				item.setTitle(R.string.menu_scan);
				mItemProgress.setVisible(false);
			} else {
				mLeDeviceListAdapter.clear();//refresh deviceListAdapter
				scanLeDevice(true);
				item.setTitle(R.string.menu_stop);
				mItemProgress.setVisible(true);
			}

			break;
		}
		return super.onOptionsItemSelected(item);
	}




}
