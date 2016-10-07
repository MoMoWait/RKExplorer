package com.android.rockchip;

import java.util.ArrayList;

import android.os.Environment;
import android.os.Handler;
import android.widget.ListView;

public class EnumConstent{
	public static final int   MSG_BASE_VALUE   = 1000;
	public static final int  MSG_ANI_ZOOM_IN   = MSG_BASE_VALUE+1;
	public static final int MSG_MOUNT_CHANGE   = MSG_BASE_VALUE+2;
	public static final int MSG_NETWORK_CHANGE = MSG_BASE_VALUE+3;
	public static final int MSG_OP_STOP_COPY   = MSG_BASE_VALUE+4;
	public static final int MSG_OP_START_COPY  = MSG_BASE_VALUE+5;
	public static final int MSG_CLEAR_CONTENT  = MSG_BASE_VALUE+6;
	
	public static final int MSG_MOUNTED        = MSG_BASE_VALUE+7;
	public static final int MSG_REMOVED        = MSG_BASE_VALUE+8;
	public static final int MSG_DLG_SHOW       = MSG_BASE_VALUE+9;
	public static final int MSG_DLG_HIDE       = MSG_BASE_VALUE+10;
	public static final int MSG_DLG_LOGIN_FAIL = MSG_BASE_VALUE+11;
	public static final int MSG_DLG_COUNT      = MSG_BASE_VALUE+12;
	public static final int MSG_DLG_EDIT       = MSG_BASE_VALUE+13;
	public static final int MSG_DLG_DELETE     = MSG_BASE_VALUE+14;
	
	public static final String mDirSmb = "SMB";
	public static final String mDirSmbMoutPoint = "/data/smb";
	
}

