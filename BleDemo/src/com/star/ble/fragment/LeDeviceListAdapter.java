package com.star.ble.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.star.ble.R;
import com.star.ble.model.BleDeviceInfo;

public class LeDeviceListAdapter extends BaseAdapter {

	// Adapter for holding devices found through scanning.

//	private ArrayList<BluetoothDevice> mLeDevices;
	private ArrayList<BleDeviceInfo> mLeDeviceInfos;
	private LayoutInflater mInflator;
	private Activity mContext;

	public LeDeviceListAdapter(Activity c) {
		super();
		mContext = c;
		mLeDeviceInfos = new ArrayList<BleDeviceInfo>();
		mInflator = mContext.getLayoutInflater();
	}
/**
 * addDevice(add devices found through scanning.)    
   
 * TODO(参数为BluetoothDevice时可以通过contains方法来判断是否添加不同的设备对象，
 * 点击scan刷新界面时不需要mLeDeviceInfos.clear()方法。参数为BleDeviceInfo时，
 * 由于对象中的rssi距离对象是时刻改变的，所以无法通过contains方法判断是否添加不同的
 * 设备对象。因此我们在点击scan刷新界面时添加mLeDeviceInfos.clear()方法)    
 */
	public void addDevice(BleDeviceInfo deviceInfo) {
		if (!mLeDeviceInfos.contains(deviceInfo)) {
			mLeDeviceInfos.add(deviceInfo);
		}
	}
	
	public BleDeviceInfo getDevice(int position) {
		return mLeDeviceInfos.get(position);
	}

	public void clear() {
		mLeDeviceInfos.clear();
	}

	@Override
	public int getCount() {
		return mLeDeviceInfos.size();
	}

	@Override
	public Object getItem(int i) {
		return mLeDeviceInfos.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}
/**
 * 添加deviceRssi
 */
	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		// General ListView optimization code.
		if (view == null) {
			view = mInflator.inflate(R.layout.devices_list_item, viewGroup, false);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view
					.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view
					.findViewById(R.id.device_name);
			viewHolder.deviceRssi = (TextView) view
					.findViewById(R.id.device_rssi);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		BleDeviceInfo deviceInfo = mLeDeviceInfos.get(i);
		BluetoothDevice device = deviceInfo.getBluetoothDevice();
		
		final String deviceName = device.getName();
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText(deviceName);
		else
			viewHolder.deviceName.setText(R.string.unknown_device);
		viewHolder.deviceAddress.setText(device.getAddress());
		viewHolder.deviceRssi.setText(deviceInfo.getRssi() + "bm");
		return view;
	}

	class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
		TextView deviceRssi;
		
	}
}
