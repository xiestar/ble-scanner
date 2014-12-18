package com.star.ble;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.star.ble.fragment.DeviceDetailsFragment;
import com.star.ble.fragment.DevicesListFragment;
import com.star.ble.model.BleDeviceInfo;

public class MainActivity extends FragmentActivity implements
		DevicesListFragment.OnDeviceConListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get fragment manager
		FragmentManager fm = this.getSupportFragmentManager();
		// add
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.fragment_container, new DevicesListFragment());
		// alternatively add it with a tag
		// trx.add(R.id.your_placehodler, new YourFragment(), "detail");
		ft.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.syat.ble.fragment.DevicesListFragment.OnDeviceConListener#onDeviceCon
	 * (java.lang.String)
	 */
	@Override
	public void onDeviceCon(BleDeviceInfo deviceInfo) {
		
		  Bundle arguments = new Bundle();
		  
		  arguments.putParcelable(DeviceDetailsFragment.EXTRAS_DEVICE_INFO,deviceInfo);
//          arguments.putString(DeviceDetailsFragment.EXTRAS_DEVICE_INFO,address);
		DeviceDetailsFragment fragment = new DeviceDetailsFragment();
		fragment.setArguments(arguments);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, fragment)
				.commit();

	}

}
