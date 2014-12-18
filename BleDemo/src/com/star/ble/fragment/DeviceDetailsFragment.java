package com.star.ble.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

import com.star.ble.R;
import com.star.ble.app.AppConfig;
import com.star.ble.app.MyGattCharacteristic;
import com.star.ble.app.MyGattService;
import com.star.ble.model.BleDeviceInfo;
import com.star.ble.service.BlueService;
import com.star.ble.utils.Utils;

public class DeviceDetailsFragment extends Fragment {
	private final static String TAG = DeviceDetailsFragment.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_INFO = "device_info";
	private static boolean mConnecting = false;
    private ArrayList<List<BluetoothGattCharacteristic>> mGattCharacteristics;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

	//
	private Activity mActivity = null;
	private BlueService mBleService = null;

	private BleDeviceInfo mDeviceInfo;
	private BluetoothDevice mDevice;
	private String mDeviceAddress;
	private String mDeviceName;
//	private int mRssi;
	// GUI
	private MenuItem mItemSwitch;
	private TextView tvDevice;
	private TextView tvAddress;
	private TextView tvState;
	private TextView tvData;

	private ExpandableListView expListServices;
	private BleExpandableListAdapter expListAdapter;

	public DeviceDetailsFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = activity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		if (getArguments().containsKey(EXTRAS_DEVICE_INFO)) {
			mDeviceInfo = getArguments().getParcelable(EXTRAS_DEVICE_INFO);
			mDevice = mDeviceInfo.getBluetoothDevice();
//			mRssi = mDeviceInfo.getRssi();
			mDeviceName = mDevice.getName();
			mDeviceAddress = mDevice.getAddress();

		}

		Intent gattServiceIntent = new Intent(getActivity(), BlueService.class);
		getActivity().getApplicationContext().bindService(gattServiceIntent,
				mServiceConnection, Context.BIND_AUTO_CREATE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.device_details_fragment,
				container, false);
		tvDevice = (TextView) view.findViewById(R.id.device_name);
		tvAddress = (TextView) view.findViewById(R.id.device_address);
		tvState = (TextView) view.findViewById(R.id.connection_state);
		tvData = (TextView) view.findViewById(R.id.characteristic_value);

		tvDevice.setText(mDeviceName);
		tvAddress.setText(mDeviceAddress);
		

		expListServices = (ExpandableListView) view
				.findViewById(R.id.gatt_services_list);
		expListServices.setOnChildClickListener(servicesListClickListner);
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		mActivity.registerReceiver(mGattUpdateReceiver,
				makeGattUpdateIntentFilter());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		mActivity.unregisterReceiver(mGattUpdateReceiver);
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBleService = ((BlueService.LocalBinder) service).getService();

			if (mBleService == null) {
				Log.w(TAG, "Service  bind failed !");
			} else {
				Log.i(TAG, "Service has been binded !");
			}
			if (!mBleService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				getActivity().finish();
			}

			Log.i(TAG, "连接设备的地址：" + mDeviceAddress);
			mBleService.connectBle(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.w(TAG, "service unbind!!!!!!!!!");
			mBleService = null;

		}

	};

	private IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(BlueService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BlueService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BlueService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BlueService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BlueService.ACTION_GATT_RECONNECT);

		return intentFilter;
	}

	private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (BlueService.ACTION_GATT_CONNECTED.equals(action)) {
				Log.i(TAG, "BLE连接成功");

				mConnecting = true;
				mActivity.invalidateOptionsMenu();
				tvState.setText(R.string.connected);

			} else if (BlueService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Log.i(TAG, "BLE断开连接");

				mConnecting = false;
				mActivity.invalidateOptionsMenu();
				tvState.setText(R.string.disconnected);
				clearUI();
			} else if (BlueService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				Log.i(TAG, "已发现服务");
				displayGattServices(mBleService.getSupportedGattServices());
			}else if (BlueService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BlueService.EXTRA_DATA));
            }

		}

	};

	 // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                            	mBleService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBleService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBleService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };
	
	
	/**
	 * 展示所选设备的所有Services、Characteristics、descriptors
	 * 
	 * @param gattServices
	 */
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		String unknownServiceString = getResources().getString(
				R.string.unknown_service);
		String unknownCharaString = getResources().getString(
				R.string.unknown_characteristic);
		 mGattCharacteristics = new ArrayList<List<BluetoothGattCharacteristic>>();
		 
		HashMap<String, String> serviceItem;
		HashMap<String, String> characterItem;
		ArrayList<HashMap<String, String>> characterItems;
		
		ArrayList<HashMap<String, String>> serviceList = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> characterList = new ArrayList<ArrayList<HashMap<String, String>>>();
		
		if (gattServices == null)
			return;

		for (BluetoothGattService gattService : gattServices) {
			// -----Service的字段信息-----//
			serviceItem = new HashMap<String, String>();
			UUID serUUID = gattService.getUuid();
			int type = gattService.getType();

			Log.i(TAG, "-->service type:" + Utils.getServiceType(type));
			Log.i(TAG, "-->includedServices size:"
					+ gattService.getIncludedServices().size());
			Log.d(TAG, "-->service uuid:" + serUUID);
			Log.w(TAG, "-->service name:" + MyGattService.lookup(serUUID, unknownServiceString));
		
			serviceItem.put(AppConfig.LIST_SERVICE_NAME,
					MyGattService.lookup(serUUID, unknownServiceString));
			serviceItem.put(AppConfig.LIST_SERVICE_UUID, serUUID.toString());
			serviceItem.put(AppConfig.LIST_SERVICE_TYPE,
					Utils.getServiceType(type));
			serviceList.add(serviceItem);

			// -----Characteristics的字段信息-----//
			characterItems = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			mGattCharacteristics.add(gattCharacteristics);
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				characterItem = new HashMap<String, String>();
				UUID charUUID = gattCharacteristic.getUuid();
				Log.i(TAG, "---->char uuid:" + charUUID);
				int permission = gattCharacteristic.getPermissions();
				Log.d(TAG,
						"---->char permission:"
								+ Utils.getCharPermission(permission));
				int property = gattCharacteristic.getProperties();
				Log.d(TAG,
						"---->char property:"
								+ Utils.getCharPropertie(property));
				Log.w(TAG, "-->char name:" + MyGattCharacteristic.lookup(charUUID, unknownCharaString));
				
				characterItem.put(AppConfig.LIST_CHARACTER_NAME,
						MyGattCharacteristic.lookup(charUUID, unknownCharaString));
				characterItem.put(AppConfig.LIST_CHARACTER_UUID,
						charUUID.toString());
				characterItem.put(AppConfig.LIST_CHARACTER_PROPERTIES,
						Utils.getCharPropertie(property));
				characterItems.add(characterItem);

				byte[] data = gattCharacteristic.getValue();
				if (data != null && data.length > 0) {
					Log.w(TAG, "---->char value:" + new String(data));
				}

				// -----Descriptors的字段信息-----//
				List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic
						.getDescriptors();
				for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
					Log.i(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
					int descPermission = gattDescriptor.getPermissions();
					Log.d(TAG,
							"-------->desc permission:"
									+ Utils.getDescPermission(descPermission));

					byte[] desData = gattDescriptor.getValue();
					if (desData != null && desData.length > 0) {
						Log.w(TAG, "-------->desc value:" + new String(desData));
					}
				}

			}
			characterList.add(characterItems);

		}

		expListAdapter = new BleExpandableListAdapter(mActivity, serviceList,
				characterList);
		expListServices.setAdapter(expListAdapter);

	}

	protected void clearUI() {
		expListServices.setAdapter((BleExpandableListAdapter) null);
		tvData.setText(R.string.no_data);
		
	}

	protected void displayData(String data) {
		 if (data != null) {
	            tvData.setText(data);
	        }
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mActivity.getApplicationContext().unbindService(mServiceConnection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
	 * android.view.MenuInflater)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_details_fragment, menu);
		mItemSwitch = menu.findItem(R.id.menu_switch);

		if (mConnecting) {
			mItemSwitch.setTitle(R.string.menu_disconnect);
		} else {
			mItemSwitch.setTitle(R.string.menu_connect);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onOptionsItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_switch:
			if (mConnecting) {
				mBleService.disconnect();
				Log.i(TAG, "item disconnect");
			} else {
				mBleService.connectBle(mDeviceAddress);
				Log.d(TAG, "item connect");
			}

			break;
		}
		return super.onOptionsItemSelected(item);
	}



}
