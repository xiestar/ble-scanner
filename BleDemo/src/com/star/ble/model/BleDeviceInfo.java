package com.star.ble.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**    
 *     
 * 项目名称：BleDemo    
 * 类名称：BleDeviceInfo    
 * 类描述：BluetoothAdapter.LeScanCallback get the model    
 * 创建人：star    
 * 创建时间：2014-12-17 上午8:43:17    
 * 修改人：star    
 * 修改时间：2014-12-17 上午8:43:17    
 * 修改备注：    
 * @version     
 *     
 */
public class BleDeviceInfo implements Parcelable{
  // Data
  private BluetoothDevice mBtDevice;
  private int mRssi;

  public BleDeviceInfo(BluetoothDevice device, int rssi) {
    mBtDevice = device;
    mRssi = rssi;
  }

  public BluetoothDevice getBluetoothDevice() {
    return mBtDevice;
  }

  public int getRssi() {
    return mRssi;
  }

  public void updateRssi(int rssiValue) {
    mRssi = rssiValue;
  }

@Override
public int describeContents() {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public void writeToParcel(Parcel out, int flags) {
	out.writeParcelable(mBtDevice, flags);
	out.writeInt(mRssi);
}

public static final Parcelable.Creator<BleDeviceInfo> CREATOR
= new Parcelable.Creator<BleDeviceInfo>() {
public BleDeviceInfo createFromParcel(Parcel in) {
return new BleDeviceInfo(in);
}

public BleDeviceInfo[] newArray(int size) {
return new BleDeviceInfo[size];
}
};

private BleDeviceInfo(Parcel in) {
	mBtDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
	mRssi = in.readInt();
}


}
