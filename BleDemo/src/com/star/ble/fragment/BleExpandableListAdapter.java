package com.star.ble.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.star.ble.R;
import com.star.ble.app.AppConfig;

public class BleExpandableListAdapter extends BaseExpandableListAdapter {

	//Constructor
	private Context mContext;
	private ArrayList<HashMap<String,String>> serviceList;
	private ArrayList<ArrayList<HashMap<String,String>>> characterList;
	
    //GUI
	private TextView tvServiceName,tvServiceId,tvServiceType;
	private TextView tvCharacterName,tvCharacterId,tvCharacterProperties;
	
	public BleExpandableListAdapter(Context mContext,
			ArrayList<HashMap<String, String>> serviceList,
			ArrayList<ArrayList<HashMap<String, String>>> characterList) {
		super();
		this.mContext = mContext;
		this.serviceList = serviceList;
		this.characterList = characterList;
	}

	@Override
	public int getGroupCount() {
	
		return serviceList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		
		return characterList.get(groupPosition).size();
	}

	/* (non-Javadoc)    
	 * @see android.widget.ExpandableListAdapter#getGroup(int)    
	 */
	@Override
	public Object getGroup(int groupPosition) {
		return serviceList.get(groupPosition);
	}
/**
 * return child item
 */
	@Override
	public Object getChild(int groupPosition, int childPosition) {

		return characterList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		HashMap<String,String> serviceItem =  (HashMap<String, String>) getGroup(groupPosition);
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.services_group_first, parent, false);
		}
		
		 tvServiceName = (TextView) convertView .findViewById(R.id.service_name);
		 tvServiceId = (TextView) convertView .findViewById(R.id.service_uuid);
		 tvServiceType = (TextView) convertView .findViewById(R.id.service_type);

		 tvServiceName.setText(serviceItem.get(AppConfig.LIST_SERVICE_NAME));
		 tvServiceId.setText(serviceItem.get(AppConfig.LIST_SERVICE_UUID));
		 tvServiceType.setText(serviceItem.get(AppConfig.LIST_SERVICE_TYPE));
		 
		return convertView;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		HashMap<String,String> characterItem =  (HashMap<String, String>) getChild(groupPosition, childPosition);
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.characters_group_second, parent, false);
		}
		
		tvCharacterName = (TextView) convertView .findViewById(R.id.character_name);
		tvCharacterId = (TextView) convertView .findViewById(R.id.character_uuid);
		tvCharacterProperties = (TextView) convertView .findViewById(R.id.character_property);

		tvCharacterName.setText(characterItem.get(AppConfig.LIST_CHARACTER_NAME));
		tvCharacterId.setText(characterItem.get(AppConfig.LIST_CHARACTER_UUID));
		tvCharacterProperties.setText(characterItem.get(AppConfig.LIST_CHARACTER_PROPERTIES));
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		
		return true;
	}

}
