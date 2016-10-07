package com.android.rockchip;

import android.util.Log;
import android.graphics.Bitmap;

public class Device
{
	private String mPath = null;
	private String mName = null;
	private String mTag = null;
	private int mIcon = 0;
	private boolean mMountStatus = false;

	public Device(String tag,String name,String path,int icon,boolean mount)
	{
		mTag = tag;
		mPath = path;
		mName = name;
		mIcon = icon;
		mMountStatus = mount;
	}

	public String getTag()
	{
		return mTag;
	}
	
	public String getName()
	{
		return  mName;
	}

	public String getPath()
	{
		return mPath;
	}

	public int getIcon()
	{
		return mIcon;
	}

	public void setMountStatus(boolean mount)
	{
		Log.d("Device","setMountStatus, status = "+mount);
		mMountStatus = mount;
	}

	public boolean IsMount()
	{
		return mMountStatus;
	}
}