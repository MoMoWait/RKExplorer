package com.android.rockchip;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.Inflater;

import jcifs.smb.SmbFile;

import com.android.rockchip.cifs.SmbUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue; 
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CifsExplorerProxy {
	private static final String TAG = "CifsExplorerProxy";
	final boolean DEBUG = true;//true;
	private void LOG(String str)
	{
		if(DEBUG)
		{
			Log.d(TAG,str);
		}
	}
	
	private static final int MSG_EXPAND_GROUP = 0;
	private static final int MSG_REFRESH_ADAPTER = 1;
	
	private RockExplorer mRockExplorer;
	public SmbUtil mSmbUtil;
	
	private Dialog mScanProgressDialog;
	private View scanProgress;
	private Resources res;
	private SharedPreferences sp;
	
	private ArrayList<FileInfo> mSmbinfoList;

	private Map<String, FileInfo> MountSmbMap;
	
	public CifsExplorerProxy(RockExplorer rockExplorer){
		mRockExplorer = rockExplorer;
		res = rockExplorer.getResources();
		mSmbUtil = new SmbUtil(rockExplorer,mHandler);
		mSmbinfoList = new ArrayList<FileInfo>();
		MountSmbMap = new HashMap<String, FileInfo>();
	}
	
	public void onCreate() {
		// get host
		LOG("start thread load....");
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				sp = mRockExplorer.getSharedPreferences("smbServer", 0);
		    	int count = sp.getInt("count", 0);
		    	LOG("load count:"+count);
		    					
			synchronized (mSmbinfoList) {
		    	mSmbinfoList.clear();
				//FileInfo item = new FileInfo();
				//item.icon = res.getDrawable(R.drawable.icon_folder);
				//item.isDir = false;
				//item.mFile = new File("..");
				//item.mFileType = "..";	
				//mSmbinfoList.add(0,item);
		    	for (int i=0;i<count;i++){
				    if (sp.getString("location"+i, null) == null)
					    continue;
		        	FileInfo smbinfo = new FileInfo();
		        	smbinfo.mIcon = res.getDrawable(R.drawable.icon_smb);
		        	smbinfo.mFile = new File(sp.getString("location"+i, null));
		        	smbinfo.mDescription = sp.getString("description"+i, null);
		        	smbinfo.setMUsername(sp.getString("username"+i, null));
		        	smbinfo.setMPassword(sp.getString("password"+i, null));
		        	smbinfo.setMAnonymous(sp.getBoolean("anonymous"+i, true));
		        	mSmbinfoList.add(smbinfo);
		    	}
			}
		   }
			
		}).start();
		
    	
	}
	
	public void onResume() {
		
	}
	
	public void onPause() {
		LOG("start thread store....");
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int index = 0;
				sp = mRockExplorer.getSharedPreferences("smbServer", 0);
				Editor editor = sp.edit();
				editor.clear().commit();
				
				synchronized (mSmbinfoList) {			
				for (FileInfo smbinfo:mSmbinfoList){
					if(!smbinfo.mFile.getPath().equals("..")){
						editor.putString("location"+index, smbinfo.mFile.getPath()).commit();
						editor.putString("description"+index, smbinfo.mDescription).commit();
						editor.putString("username"+index, smbinfo.getMUsername()).commit();
						editor.putString("password"+index, smbinfo.getMPassword()).commit();
						editor.putBoolean("anonymous"+index, smbinfo.isMAnonymous()).commit();
						index++;
					}
				}
				}
				editor.putInt("count", index).commit();
				//editor.clear().commit();
				LOG("store count:"+index);
				}
			}).start();
	}
	
	public void onDestroy() {
		umountAllCifs();
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_EXPAND_GROUP:
				FileInfo smbinfo = (FileInfo) msg.obj;
				String smbname = smbinfo.mDescription;
				String smbpath = smbinfo.mFile.getPath();
				
				int size = mSmbinfoList.size();
				int i;
				for (i = 0;i<size;i++){
					FileInfo info = mSmbinfoList.get(i);
					String name = info.mDescription;
					String path = info.mFile.getPath();
					if (smbpath.equals(path)){
						return;
					}
					if (smbname.compareTo(name)<= 0){
						break;
					}
				}
				mSmbinfoList.add(i,smbinfo);
				getFileControl().folder_array = mSmbinfoList;
				setFileAdapter(false, false);
				break;
				
			case MSG_REFRESH_ADAPTER:
				setFileAdapter(false, false);
				mRockExplorer.enableButton(true,true);
				break;
			}
		}
	};

	public void getCifsContent(String path){
		FileControl.is_first_path = false;
		if (path.equals(EnumConstent.mDirSmb)){
			getFileControl().currently_path = path;
			getFileControl().currently_parent = mRockExplorer.mDefaultPath;
			
			synchronized (mSmbinfoList) {
				//cifs dirctionary is empty
				if (mSmbinfoList.size()<1){ 
					searchSmb();
				}else{
					getFileControl().folder_array = mSmbinfoList;
					mHandler.postDelayed(mFillSmb, 300); 
				}
			}
		}
	}

	Runnable mFillSmb = new Runnable(){
		public void run() {
			setFileAdapter(false, false);
		}
	};


	
	
	public boolean getCifsChildContent(String path){
		String host;
		boolean Anonymous;
		String User;
		String Password;
		SmbFile smbfile;
		
		FileInfo smbinfocache = getSmbinfoFromSmbPath(path);
		if(smbinfocache == null)
			return false;
		
		host = smbinfocache.mFile.getPath().substring(4);
		Anonymous = smbinfocache.isMAnonymous();
		User = smbinfocache.getMUsername();
		Password = smbinfocache.getMPassword();
		
		LOG("host:"+host+" anonymous:"+Anonymous+" user:"+User+" password:"+Password);
		if(!mRockExplorer.ping(host)){
			mRockExplorer.showToast(res.getString(R.string.smb_host_error,host));
			return false;
		}
		
		ArrayList<FileInfo> smbinfolist = new ArrayList<FileInfo>();
		
		if(Anonymous){
			try {
				LOG("smb://guest:@"+host+"/");
				smbfile = new SmbFile("smb://guest:@"+host+"/");
				for (String share:smbfile.list()){
					if (!share.endsWith("$")){
						LOG("smb://"+host+"/"+share);
						String keypath = share;
						
						FileInfo smbinfo = new FileInfo();
						smbinfo.mFile = new File(EnumConstent.mDirSmb+"/"+host+"/"+share);
						smbinfo.mDescription = share;
						smbinfo.mIcon = res.getDrawable(R.drawable.icon_smb);
						smbinfo.mIsDir= true;
						smbinfolist.add(smbinfo);
						
					}
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}else{
			try {
				LOG("smb://"+User+":"+Password+"@"+host+"/");
				smbfile = new SmbFile("smb://"+User+":"+Password+"@"+host+"/");
				for (String share:smbfile.list()){
					if (!share.endsWith("$")){
						LOG("smb://"+host+"/"+share);
						String keypath = share;
						
						FileInfo smbinfo = new FileInfo();
						smbinfo.mFile = new File(EnumConstent.mDirSmb+"/"+host+"/"+share);
						smbinfo.mDescription = share;
						smbinfo.mIcon= res.getDrawable(R.drawable.icon_smb);
						smbinfo.mIsDir= true;
						smbinfolist.add(smbinfo);

					}
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}
		getFileControl().currently_path = path;
		getFileControl().currently_parent = EnumConstent.mDirSmb;
		
		getFileControl().folder_array = smbinfolist;
		
		Message msg = new Message();
		msg.what = MSG_REFRESH_ADAPTER;
		mHandler.sendMessage(msg);
		
		return true;
	}
	
	public String getMountPoint(FileInfo smbinfo,String goPath){
		String path = smbinfo.mFile.getPath();
		if (path.startsWith(EnumConstent.mDirSmb+"/")){
			if (path.substring(4).contains("/")){
				String mountPoint = mount(smbinfo);
				return mountPoint;
			}else {
				return goPath;
			}
		}else{
			return goPath;
		}
	}
	
	public boolean NetworkIsConnect(Context context){
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		NetworkInfo ethernetInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		
		WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(!wifiInfo.isConnected()&&!ethernetInfo.isConnected()
				&&!(wifimanager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED)){
			return false;
		}
		return true;
	}
	
	public void searchSmb(){
		if(!NetworkIsConnect(mRockExplorer)){
			AlertDialog.Builder netbuilder = new AlertDialog.Builder(mRockExplorer);
				netbuilder.setTitle(res.getString(R.string.network_error_title))
					.setMessage(res.getString(R.string.network_error_message))
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener(){

						@Override
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					
					})
					.setPositiveButton(res.getString(R.string.network_settings), new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setClassName("com.android.settings", "com.android.settings.WirelessSettings");
							mRockExplorer.startActivity(intent);
							dialog.dismiss();
						}
					
					}).create();
				netbuilder.show();
				return;
		}
		mScanProgressDialog = new Dialog(mRockExplorer,R.style.MyDialog);
		mScanProgressDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater layoutinflater = LayoutInflater.from(mRockExplorer);
		scanProgress =  (View) layoutinflater.inflate(R.layout.smb_search_progress, null);
		TextView textview = (TextView) scanProgress.findViewById(R.id.percent);
		textview.setText(res.getString(R.string.scanningSmbMessage)+"0%");
		mScanProgressDialog.setContentView(scanProgress);
		mScanProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				mSmbUtil.setSearchOver(true);
				dialog.dismiss();
			}
			
		});
		mScanProgressDialog.show();

		Thread scanthread = new Thread(mScanRun);
		scanthread.setPriority(10);
		scanthread.start();
		mHandler.postDelayed(mRefreshRun, 1000);
	}
	
	
	private FileControl getFileControl(){
		return mRockExplorer.mFileControl;
	}
	
	private void setFileAdapter(boolean is_animation, boolean is_left){
		mRockExplorer.setFileAdapter(is_animation, is_left);
	}
	
	
	Runnable mScanRun = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mSmbUtil.searchSmb();
		}
		
	};
	
	Runnable mRefreshRun = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			LOG("refresh smb list");
			if(mSmbUtil.searchover()){
				if (mScanProgressDialog !=null){
					mScanProgressDialog.dismiss();
				}
				mHandler.removeCallbacks(mRefreshRun);
			} else {
				if (mScanProgressDialog !=null){
					int percent = (mSmbUtil.getScannedHostCount()*100)/mSmbUtil.getAllHostCount();
					TextView textview = (TextView) scanProgress.findViewById(R.id.percent);
					textview.setText(res.getString(R.string.scanningSmbMessage)+percent+"%");
				}
				mHandler.postDelayed(mRefreshRun, 1000);
			}
		}
		
	};

	
	public void umountAllCifs(){
		new Thread(new Runnable() {
			public void run() {
				ArrayList<String> mountlist = SmbUtil.getMountMsg();
		    	for (String str : mountlist){
		    		String split[] = str.split(" ");
		    		if (split[2].equals("cifs")){
		    			LOG("mount point********:"+split[1]);
		    			mSmbUtil.umount(split[1]);
		    		}
		    	}
		    	File mountPointDir = new File(EnumConstent.mDirSmbMoutPoint);
				if(!mountPointDir.exists())
					return ;
		    	for (File file: mountPointDir.listFiles()){
		    		file.delete();
		    	}
			}
		}).start();
		
	}
	
	public String mount(FileInfo smbinfo){
		
		String path = smbinfo.mFile.getPath().substring(4);

		String smbSplit[] = path.split("/");
					
		String smbpath = "//"+smbSplit[0]+"/"+smbSplit[1];
		
		ArrayList<String> mountlist = SmbUtil.getMountMsg();
    	ArrayList<String> cifslist = new ArrayList<String>();
    	ArrayList<String> mountpointlist = new ArrayList<String>();
    	for (String str : mountlist){
    		String split[] = str.split(" ");
    		if (split[2].equals("cifs")){
    			cifslist.add(split[0].replace("\\"+"040", " ").replace("\\"+"134", "/"));
    			mountpointlist.add(split[1]);
    		}
    	}
    	if(cifslist.contains(smbpath)){
    		String mountpoint = mountpointlist.get(cifslist.indexOf(smbpath));
    		smbinfo.setMMountpoint(mountpoint);
    		
    		File files = new File(mountpoint);
    		LOG("file read:"+files.canRead()+" file exists:"+files.exists()+"file list:"+files.list().length);
    		
    		if(!files.exists() || !files.canRead() || files.list().length == 0){
    			LOG("remote sharefolder can not read or null");
    			SmbUtil.umount(mountpoint);
    		}else{
        		smbinfo.setMIsMount(true);
    			return mountpoint;
    		}
    	}
    	
    	String result = SmbUtil.mount(smbpath, smbinfo);
	
		if(result == null){

			ArrayList<String> mountlist1 = SmbUtil.getMountMsg();
	    	ArrayList<String> cifslist1 = new ArrayList<String>();
	    	for (String str : mountlist1){
	    		String split[] = str.split(" ");
	    		if (split[2].equals("cifs")){
	    			cifslist1.add(split[0].replace("\\"+"040", " ").replace("\\"+"134", "/"));
	    		}
	    	}
	    	if(!cifslist1.contains(smbpath)){
	    		smbinfo.setMIsMount(false);	    		
	    		mRockExplorer.showToast(res.getString(R.string.mount_fail));
	    		String mountPoint = smbinfo.getMMountpoint();
	    		SmbUtil.deleteMountPoint(mountPoint);
	    		return null;
	    	}
	    	
    		String mountPoint = smbinfo.getMMountpoint();
	    	
			smbinfo.setMIsMount(true);
			
	    	MountSmbMap.put(mountPoint,smbinfo);
			mRockExplorer.showToast(res.getString(R.string.mount_success));
	    	return mountPoint;
		} else {
			mRockExplorer.showToast(res.getString(R.string.mount_fail)+"\n"+result);
			return null;
		}
    	
	}

	
	private EditText mLocation;
	private EditText mUsername;
	private EditText mPassword;
	private CheckBox mAnonymous;
    public void CreateNewSmb(){
    	LayoutInflater inflater = LayoutInflater.from(mRockExplorer);
		View view = (View) inflater.inflate(R.layout.new_smb_server, null);
		mLocation = (EditText) view.findViewById(R.id.location);
		mUsername = (EditText) view.findViewById(R.id.username);
		mPassword = (EditText) view.findViewById(R.id.password);
		mAnonymous = (CheckBox) view.findViewById(R.id.use_anonymous);
		mAnonymous.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					mUsername.setEnabled(false);
					mPassword.setEnabled(false);
					mUsername.setInputType(InputType.TYPE_NULL);
					mPassword.setInputType(InputType.TYPE_NULL);
				}else{
					mUsername.setEnabled(true);
					mPassword.setEnabled(true);
					mUsername.setInputType(InputType.TYPE_CLASS_TEXT);
					mPassword.setInputType(InputType.TYPE_CLASS_TEXT);
				}
			}
			
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mRockExplorer);
		builder.setTitle(R.string.smb_edit)
			.setView(view)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					boolean test = true;
					if(!SmbUtil.judge(mLocation.getText().toString().trim())){
						Toast.makeText(mRockExplorer, res.getString(R.string.network_location_null), Toast.LENGTH_SHORT).show();
						test = false;
					}else{
						if (!mAnonymous.isChecked()){
							if(mUsername.getText() == null){
								Toast.makeText(mRockExplorer, res.getString(R.string.username_null), Toast.LENGTH_SHORT).show();
								test = false;
							}else if (mUsername.getText().toString().trim().length() == 0){
								Toast.makeText(mRockExplorer, res.getString(R.string.username_null), Toast.LENGTH_SHORT).show();
								test = false;
							}else if(mPassword.getText() == null){
								Toast.makeText(mRockExplorer, res.getString(R.string.password_null),Toast.LENGTH_SHORT).show();
								test = false;
							}else if (mPassword.getText().toString().trim().length() == 0){
								Toast.makeText(mRockExplorer, res.getString(R.string.password_null), Toast.LENGTH_SHORT).show();
								test = false;
							}									
						}
					}
					if(!test){
						try { 
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
							field.setAccessible(true); field.set(dialog, false);
						} 
						catch (Exception e) { 
								e.printStackTrace(); 
						}
						return;
					}
					
					FileInfo smbinfo =new FileInfo();
					String smbpath = mLocation.getText().toString().trim();
					smbinfo.mFile = new File(EnumConstent.mDirSmb+"/"+smbpath);
					smbinfo.mDescription = smbpath;

					smbinfo.setMUsername(mUsername.getText().toString().trim());
					smbinfo.setMPassword(mPassword.getText().toString().trim());
					smbinfo.setMAnonymous(mAnonymous.isChecked());
					smbinfo.mIcon = res.getDrawable(R.drawable.icon_smb);
					smbinfo.mIsDir = true;
					mSmbUtil.SmbSort(smbinfo);
					
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); 
						field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); 
						field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); 
						field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			}).create();
		builder.show();
    }
    
    FileInfo mEditSmbInfo;
    public void EditSmb(FileInfo smbinfo){
    	mEditSmbInfo = smbinfo;
    	LayoutInflater inflater = LayoutInflater.from(mRockExplorer);
		View view = (View) inflater.inflate(R.layout.new_smb_server, null);
		mLocation = (EditText) view.findViewById(R.id.location);
		mUsername = (EditText) view.findViewById(R.id.username);
		mPassword = (EditText) view.findViewById(R.id.password);
		mAnonymous = (CheckBox) view.findViewById(R.id.use_anonymous);
		
		mAnonymous.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					mUsername.setEnabled(false);
					mPassword.setEnabled(false);
					mUsername.setInputType(InputType.TYPE_NULL);
					mPassword.setInputType(InputType.TYPE_NULL);
				}else{
					mUsername.setEnabled(true);
					mPassword.setEnabled(true);
					mUsername.setInputType(InputType.TYPE_CLASS_TEXT);
					mPassword.setInputType(InputType.TYPE_CLASS_TEXT);
				}
			}
			
		});
		
		String str = smbinfo.mFile.getPath().substring(4);
	
		mLocation.setText(str);				
//		mLocation.setEnabled(false);
		
		mUsername.setText(smbinfo.getMUsername());
		mPassword.setText(smbinfo.getMPassword());
		mAnonymous.setChecked(smbinfo.isMAnonymous());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mRockExplorer);
		builder.setTitle(R.string.smb_edit)
			.setView(view)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					boolean test = true;
					if(!SmbUtil.judge(mLocation.getText().toString().trim())){
						Toast.makeText(mRockExplorer, res.getString(R.string.network_location_null), Toast.LENGTH_SHORT).show();
						test = false;
					}else{
						if (!mAnonymous.isChecked()){
							if(mUsername.getText().toString() == null){
								Toast.makeText(mRockExplorer, res.getString(R.string.username_null), Toast.LENGTH_SHORT).show();
								test = false;
							}else if (mUsername.getText().toString().trim().length() == 0){
								Toast.makeText(mRockExplorer, res.getString(R.string.username_null), Toast.LENGTH_SHORT).show();
								test = false;
							}else if(mPassword.getText().toString() == null){
								Toast.makeText(mRockExplorer, res.getString(R.string.password_null),Toast.LENGTH_SHORT).show();
								test = false;
							}else if (mPassword.getText().toString().trim().length() == 0){
								Toast.makeText(mRockExplorer, res.getString(R.string.password_null), Toast.LENGTH_SHORT).show();
								test = false;
							}									
						}
					}
					if(!test){
						try { 
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
							field.setAccessible(true); field.set(dialog, false);
						} 
						catch (Exception e) { 
								e.printStackTrace(); 
						}
						return;
					}
					
					String smbpath = mLocation.getText().toString().trim();
					mEditSmbInfo.mFile = new File(EnumConstent.mDirSmb+"/"+smbpath);
					mEditSmbInfo.mDescription = smbpath;
					mEditSmbInfo.setMUsername(mUsername.getText().toString().trim());
					mEditSmbInfo.setMPassword(mPassword.getText().toString().trim());
					mEditSmbInfo.setMAnonymous(mAnonymous.isChecked());
					getFileControl().folder_array.remove(mEditSmbInfo);
					mSmbUtil.SmbSort(mEditSmbInfo);

					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener(){
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			}).create();
		builder.show();
    }
    
    public class mDeleteSmbRun implements Runnable{

    	private ArrayList<FileInfo> smblist;
    	
		/**
		 * @param smblist
		 */
		public mDeleteSmbRun(ArrayList<FileInfo> smblist) {
			super();
			this.smblist = smblist;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int size = smblist.size();
			for (int i = 0;i<size;i++){
				String mountpoint = smblist.get(i).getMMountpoint();
				if (mountpoint != null)
					mSmbUtil.umount(mountpoint);
			}
			smblist.clear();
		}
    	
    }
    
    public void delete(ArrayList<FileInfo> smbinfolist){
    	int size = smbinfolist.size();
    	for (int i=0;i<size;i++){
    		getFileControl().folder_array.remove(smbinfolist.get(i));
    	}
    	setFileAdapter(false, false);
    	new Thread(new mDeleteSmbRun(smbinfolist)).start();
    }
    
    public String getSmbFromMountPoint(String mountpoint){
    	LOG("mountpoint:"+mountpoint);
    	ArrayList<String> mountlist = SmbUtil.getMountMsg();
    	ArrayList<String> cifslist = new ArrayList<String>();
    	ArrayList<String> mountpointlist = new ArrayList<String>();
    	for (String str : mountlist){
    		String split[] = str.split(" ");
    		if (split[2].equals("cifs")){
    			String cifs = split[0].replace("\\"+"040", " ").replace("\\"+"134", "/");
    			cifslist.add(cifs);
    			mountpointlist.add(split[1]);
    		}
    	}
    	String smb = null;
    	if (mountpointlist.contains(mountpoint)){
    		smb = cifslist.get(mountpointlist.indexOf(mountpoint));
    	}
    	
    	return smb;
    }
    
    public FileInfo getSmbinfoMap(String mountpoint){
  	
    	FileInfo smbinfo = MountSmbMap.get(mountpoint);
		return smbinfo;
	}
    
    public FileInfo getSmbinfoFromSmbPath(String smbpath){
    	for (FileInfo smbinfo : mSmbinfoList){
			if (smbinfo.mFile.getPath() == null)
				continue;
			if (smbinfo.mFile.getPath().equals(smbpath)){
				return smbinfo;
			}
		}
    	return null;
    }
    
    public void showLoginFailDialog(FileInfo smbinfo){
    	if (smbinfo == null)
    		return ;
    	try
		{
		    Thread.sleep(1000);

		}catch(Exception ex){

		}
    	mEditSmbInfo = smbinfo;
    	AlertDialog.Builder builder = new AlertDialog.Builder(mRockExplorer);
    	builder.setTitle(R.string.login_fail_title)
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.setMessage(R.string.relogin_confirm_message)
    		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					showReloginDialog();
					dialog.dismiss();
				}
    			
    		})
    		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
    			
    		})
    		.setOnCancelListener(new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
    			
    		}).create();
    	builder.show();
    }
	
    
    public void showReloginDialog(){

    	if (mEditSmbInfo == null)
    		return;
    	
    	LayoutInflater inflater = LayoutInflater.from(mRockExplorer);
		View view = (View) inflater.inflate(R.layout.new_username_password, null);
		mUsername = (EditText) view.findViewById(R.id.re_username);
		mPassword = (EditText) view.findViewById(R.id.re_password);
		mAnonymous = (CheckBox) view.findViewById(R.id.re_use_anonymous);
		
		mAnonymous.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					LOG("relogin check:"+isChecked);
					mUsername.setEnabled(false);
					mPassword.setEnabled(false);
					lockUnlock(mUsername, true);
					lockUnlock(mPassword, true);

				}else{
					LOG("relogin check:"+isChecked);

					mUsername.setEnabled(true);
					mPassword.setEnabled(true);
					lockUnlock(mUsername, false);
					lockUnlock(mPassword, false);
				}
			}
			
		});
		
		mUsername.setText(mEditSmbInfo.getMUsername());
		mPassword.setText(mEditSmbInfo.getMPassword());
		mAnonymous.setChecked(mEditSmbInfo.isMAnonymous());
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mRockExplorer);
		builder.setTitle(R.string.relogin_title)
			.setView(view)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					boolean test = true;
					if (!mAnonymous.isChecked()){
						if(mUsername.getText().toString() == null){
							Toast.makeText(mRockExplorer, res.getString(R.string.username_null), Toast.LENGTH_SHORT).show();
							test = false;
						}else if (mUsername.getText().toString().trim().length() == 0){
							Toast.makeText(mRockExplorer, res.getString(R.string.username_null), Toast.LENGTH_SHORT).show();
							test = false;
						}else if(mPassword.getText().toString() == null){
							Toast.makeText(mRockExplorer, res.getString(R.string.password_null),Toast.LENGTH_SHORT).show();
							test = false;
						}else if (mPassword.getText().toString().trim().length() == 0){
							Toast.makeText(mRockExplorer, res.getString(R.string.password_null), Toast.LENGTH_SHORT).show();
							test = false;
						}									
					}
					if(!test){
						try { 
							Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
							field.setAccessible(true); field.set(dialog, false);
						} 
						catch (Exception e) { 
								e.printStackTrace(); 
						}
						return;
					}
					
					mEditSmbInfo.setMUsername(mUsername.getText().toString().trim());
					mEditSmbInfo.setMPassword(mPassword.getText().toString().trim());
					mEditSmbInfo.setMAnonymous(mAnonymous.isChecked());
					
					
					if (mRockExplorer.isItemClick){
						int position = getFileControl().folder_array.indexOf(mEditSmbInfo);
						if(position != -1){
							mRockExplorer.mContentList.performItemClick(null, position, 0);
						}
					}
					
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					try { 
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
						field.setAccessible(true); field.set(dialog, true);
					} 
					catch (Exception e) { 
							e.printStackTrace(); 
					} 
					dialog.dismiss();
				}
				
			}).create();
		builder.show();
    }
    
    private void lockUnlock(EditText editText,boolean value) {   
    	if (value) {   
    		editText.setFilters(new InputFilter[] {  new InputFilter() {  

				@Override
				public CharSequence filter(CharSequence source, int start,
						int end, Spanned dest, int dstart, int dend) {
					// TODO Auto-generated method stub
					return source.length() < 1 ? dest.subSequence(dstart, dend): "";
				} 
    		}
    		});   
    	} else {   
    		editText.setFilters(new InputFilter[] {  new InputFilter() {  
    			@Override  
    			public CharSequence filter(CharSequence source, int start,   
    					int end, Spanned dest, int dstart, int dend) {   
    				return null;   
    			}
    		}
    		});   
    	}   
	}

    public void clearSmbcache(){
    	mSmbinfoList.clear();
    }
    
    public boolean openSmbDir(String path){
    	mRockExplorer.fill_path = path;
    	mRockExplorer.mFillHandler.removeCallbacks(mRockExplorer.mFillRun);
    	
    	String smbpath = mRockExplorer.parseMountDirToSmbpath(mRockExplorer.mCurrnetSmb);
		
		LOG("parse smbpath:"+smbpath);
		
    	if(!mRockExplorer.cifsIsMountAndConnect(smbpath)){
    		FileControl.is_enable_fill = true;
			FileControl.is_finish_fill = true;
			
			Message msg = new Message();
	    	msg.what = EnumConstent.MSG_DLG_HIDE;
	    	mRockExplorer.mHandler.sendMessage(msg);
			return false;
		}
		
    	if (!mRockExplorer.SmbReadPermissionTest(path)){
			mRockExplorer.showToast(res.getString(R.string.read_error)+"\n"+
					res.getString(R.string.smb_read_denied));
			
			FileControl.is_enable_fill = true;
			FileControl.is_finish_fill = true;
			
			Message msg = new Message();
	    	msg.what = EnumConstent.MSG_DLG_HIDE;
	    	mRockExplorer.mHandler.sendMessage(msg);
			return false; 
		}
    	
    	File files = new File(path); 
		if(!files.exists() || !files.canRead()){
			mRockExplorer.showToast(res.getString(R.string.read_error)+"\n"+
					res.getString(R.string.read_denied));
			FileControl.is_enable_fill = true;
			FileControl.is_finish_fill = true;
			
			Message msg = new Message();
	    	msg.what = EnumConstent.MSG_DLG_HIDE;
	    	mRockExplorer.mHandler.sendMessage(msg);
			return false; 
		}
    	long file_count = files.list().length; 
    	LOG("in the openDir, file_count = " + file_count);
    	if(file_count > 1500){
    		mRockExplorer.openwiththread = true;
    	}else{    	
    		mRockExplorer.openwiththread = false;
    	}
    	mRockExplorer.mOpenHandler.postDelayed(mRockExplorer.mOpeningRun, 200);	
    	mRockExplorer.mFillHandler.postDelayed(mRockExplorer.mFillRun, 300);
    	
    	return true;
    }
    
    public void openSmbfile(FileInfo mListtmp,int position){
    	Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    	    
	    if(mListtmp.mFileType.equals("image/*") || mListtmp.mFileType.equals("audio/*")
	    	|| mListtmp.mFileType.equals("video/*")){
	    	Uri tmpUri = null;
			if(mListtmp.mUri!=null)
				tmpUri = Uri.parse(mListtmp.mUri);
			else
				tmpUri = mRockExplorer.getFileUri(mListtmp.mFile, mListtmp.mFileType);
			
			if(tmpUri != null)
		    	intent.setDataAndType(tmpUri, mListtmp.mFileType);
			else
				intent.setDataAndType(Uri.fromFile(mListtmp.mFile),mListtmp.mFileType);
	    }else    		    	
	    	intent.setDataAndType(Uri.fromFile(mListtmp.mFile),mListtmp.mFileType);
	    
	    try { 
	    	mRockExplorer.startActivity(intent);
	    } catch (android.content.ActivityNotFoundException e) {
	    	Toast.makeText(mRockExplorer, mRockExplorer.getString(R.string.noapp), Toast.LENGTH_SHORT).show();
            		Log.e(TAG, "Couldn't launch music browser", e);
	    }
    }
    
}
