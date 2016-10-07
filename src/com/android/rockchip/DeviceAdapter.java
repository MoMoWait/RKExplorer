package com.android.rockchip;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue; 


public class DeviceAdapter extends BaseAdapter
{
	private Context mContext = null;
	private ArrayList<Device> mDevice = null;
	private LayoutInflater flater = null;
	private int mItemWidth=0;
	private int mItemHeight=0;
	public DeviceAdapter(Context context,ArrayList<Device> device)
	{
		mContext = context;
		mDevice = device;
		flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public int getCount() 
	{
		if(mDevice != null)
			return mDevice.size();
		return 0;
	}

	public Object getItem(int position) 
	{
		// TODO Auto-generated method stub
		if(mDevice != null)
			return mDevice.get(position);

		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}


	public int getBackgroundResource(int position, boolean mounted){
		int resource_id = 0;
		switch(position){
			case 0:
				if(mounted){
					resource_id=R.drawable.nav_flash1;
				}else{
					resource_id=R.drawable.nav_flash2;
				}
				break;
			case 1:
				if(mounted){
					resource_id=R.drawable.nav_sdcard1;
				}else{
					resource_id=R.drawable.nav_sdcard2;
				}
				break;
			case 2:
				if(mounted){
					resource_id=R.drawable.nav_usb1;
				}else{
					resource_id=R.drawable.nav_usb2;
				}
				break;
/*			case 3:
				if(mounted){
					resource_id=R.drawable.nav_sata1;
				}else{
					resource_id=R.drawable.nav_sata2;
				}
				break;*/
			case 3:
				if(mounted){
					resource_id=R.drawable.nav_network1;
				}else{
					resource_id=R.drawable.nav_network2;
				}

				break;
			default:
				break;
		}
		return resource_id;
	}

	public int getFucusDown(int postion){
		for(int i= postion; i < 4; i++){
			Device device = mDevice.get(i);
			if(device.IsMount()){
				return i;
			}
		}
		return -1;
	}

	public int getFucusUp(int postion){
		for(int i= postion; i >=0; i--){
			Device device = mDevice.get(i);
			if(device.IsMount()){
				return (i);
			}
		}
		return -1;
	}


	public View getView(int position, View convertView, ViewGroup parent)
	{
		if((mDevice == null) || (position >= getCount()))
			return null;
		float factor = AutoSize.getInstance().getDensityFactor();
		Device device = mDevice.get(position);
		int resourceid = getBackgroundResource(position, device.IsMount());
		Drawable item_bg = mContext.getResources().getDrawable(resourceid);
		
		LinearLayout layout = (LinearLayout)flater.inflate(R.layout.device_list_item,null);
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
								(int)(item_bg.getIntrinsicWidth()*factor),
								(int)(item_bg.getIntrinsicHeight()*factor));
		layout.setLayoutParams(layoutParams);
		
		
		ImageView image = (ImageView)layout.findViewById(R.id.device_image);
		image.setLeft(AutoSize.getInstance().getMargin(0.02f));
		//image.setBackgroundResource(device.getIcon());

		TextView text = (TextView)layout.findViewById(R.id.device_name);
		text.setText(device.getName());
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSize.getInstance().getTextSize(0.05f));
		text.setLeft(AutoSize.getInstance().getMargin(0.02f));

		if(device.IsMount()){	
			text.setTextColor(Color.WHITE);
		}else{
			text.setTextColor(Color.GRAY);
		}
		layout.setBackground(item_bg);
		
		return layout;
	}
}

