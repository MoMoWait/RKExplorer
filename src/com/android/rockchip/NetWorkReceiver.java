package com.android.rockchip;


import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.Message;



public class NetWorkReceiver extends BroadcastReceiver
{
	private static Handler mHandler = null;

	public static void setHandler(Handler handler)
	{
		mHandler = handler;
	}
	
	public void onReceive(Context context, Intent intent)
	{ 
		if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
		{ 
			NetworkInfo networkInfo = (NetworkInfo)intent.getExtra(ConnectivityManager.EXTRA_NETWORK_INFO,null);
			int status = 0;
			if(networkInfo != null)
			{
				if(networkInfo.isAvailable() && networkInfo.isConnected())
				{
					status = 1;
				}
				else
				{
					status = 0;
				}
				
				if(mHandler != null)
				{
					Message msg1 = new Message();
					msg1.what = EnumConstent.MSG_OP_STOP_COPY;
					msg1.arg1 = status;
					mHandler.sendMessage(msg1);
					
					Message msg = new Message();
					msg.arg1 = status;
					msg.what = EnumConstent.MSG_NETWORK_CHANGE;
					mHandler.sendMessage(msg);
				}
			}
		}
	}
}


