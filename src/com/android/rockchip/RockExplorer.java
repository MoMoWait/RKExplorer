package com.android.rockchip;

import com.android.rockchip.cifs.SmbUtil;

import java.lang.Integer;
import java.lang.reflect.Field;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Activity;
import android.app.ProgressDialog;
import android.annotation.TargetApi;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ActivityInfo;
import android.content.ActivityNotFoundException;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue; 
import android.database.Cursor;

import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.graphics.Color;
import android.graphics.Rect;


import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;


import android.view.animation.Animation;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;


import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.os.StrictMode;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;



import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;

import android.os.storage.VolumeInfo;
public class RockExplorer extends Activity 
	implements AdapterView.OnItemSelectedListener,OnSystemUiVisibilityChangeListener{
	
	final String TAG = "RkExplorer";
	final boolean DEBUG = true;
	private void LOG(String str)                                                                                                                                                                              
	{ 
		if(DEBUG)
		{
			Log.d(TAG,str);
		}
	}

	private class Path
	{
		private String mPath = null;
		private String mDirection = null;

		public Path(String path,String dir)
		{
			mPath = path;
			mDirection = dir;
		}
		
		public String getPath()
		{
			return mPath;
		}

		public String getPathDirection()
		{
			return mDirection;
		}
	}
	
    /** Called when the activity is first created. */
	public ViewGroup tool_bar = null;
	public View title_bar = null;
	public TextView mTextTitle = null;
	public ImageView title_image = null;
	
	NormalListAdapter mTempListAdapter = null;
	
	public FileControl mFileControl = null;
	private CopyFileUtils mCopyFileUtils = null;
	
	Resources resources = null;
	
	ListView mDeviceList = null;
	ListView mContentList = null;
	private View mPreviousItem=null;
	private ImageView mItemHL1=null;
	private ImageView mItemHL2=null;
	private Rect mRectBake=null;
	
	private ArrayList<Path> mSavePath = null;
	private int mPitSavePath = 0;
	private boolean is_del_save_path = false;
	
	private ImageView image_multi_choice = null;
	
	private ArrayList<FileInfo> mSelectedPathList = null;
	private ArrayList<FileInfo> mSourcePathList = null;
	private int[] multi_position = null;
	private int mLongClickPosition = 0;
		
	public ProgressDialog openingDialog = null;
	public boolean openwiththread = false;
		
	private boolean mEnablePaste = false;
	private boolean mEnableCopy = false;
	private boolean mEnableMove = false;
	private boolean mEnableRename = false;
	private boolean mEnableSmbEdit = false;
	private boolean mEnableCopyInBg = false;
	private boolean mEnableLeft = true;
	private boolean mEnableFinish = true;
	
	private String mSourcePath = null;
	private String mTargetPath = null;
	
	public String mDefaultPath = "first_path";
	private String mDirFilePath;
	
	public String flash_dir = StorageUtils.getFlashDir();
	public String sdcard_dir;
    public String usb_dir;
    //private String currentDir = flash_dir;
//	public String usb_dir0 = Environment.getHostStorage_Extern_0_Directory().getPath();
//	public String usb_dir1 = Environment.getHostStorage_Extern_1_Directory().getPath();
//	public String usb_dir2 = Environment.getHostStorage_Extern_2_Directory().getPath();
//	public String usb_dir3 = Environment.getHostStorage_Extern_3_Directory().getPath();
//	public String usb_dir4 = Environment.getHostStorage_Extern_4_Directory().getPath();
//	public String usb_dir5 = Environment.getHostStorage_Extern_5_Directory().getPath();
	public static final String smb_dir = "SMB";
	public static final String smb_mountPoint = "/data/smb";

//	public LinearLayout contentLayout;
//	public Handler mUIhandler;
	private CifsExplorerProxy mCifsProxy;
	
	public static int mDisableSATA   = 0;
	public static int mDisableSDCARD = 0;
	PowerManager mPowerManager;
	PowerManager.WakeLock mWakeLock;

	public ProgressDialog delDialog = null;
	
	public ProgressDialog mWaitDialog;
	private int mInitDone=0;

	public static String mLastDirectory = null;
    public static boolean mIsLastDirectory = false;
    public int last_item_tmp = 0;
    public int list_status_tmp = 0;
	private StorageManager mStorageManager = null;	
	/**SD卡路径*/
	private List<String> mSDCardPaths;
	/**USB路径*/
	private List<String> mUsbPaths;
	/**存储类别*/
	private String mStorageType = "flash";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		LOG("Activity LifeCycle: onCreate");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
			                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 

		// verity platform for protecting copyright
		File file  = new File("init.rk29board.rc");
		File file1 = new File("init.rk30board.rc");
		if(((file == null)|| !file.exists()) && ((file1 == null)|| !file1.exists())){
			finish();
		}

        if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
        }
        
        setContentView(R.layout.main);
		final View bar_top = findViewById(R.id.bar_top);
		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
       
        initData();
        initViews();
		mDelDialog = CreateDelDialog();

		try{
		    mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		    mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,TAG);
		}catch (Exception e){
			e.printStackTrace();
		}

        registBroadcastRec();
		NetWorkReceiver.setHandler(mHandler);
			
    }


	@TargetApi(19)
	private void hideSystemUI() {
		LOG("[Function] hideSystemUI() Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT);
		
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().getDecorView().setSystemUiVisibility(
							  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE);
		}
	}
	
/*	@Override
	public void onWindowFocusChanged(boolean hasFocus) { 
	
		//TODO Auto-generated method stub 
		super.onWindowFocusChanged(hasFocus);
		if(mInitDone==0){
			mDeviceList.setSelection(mOldPosition);
			mDeviceList.getChildAt(mOldPosition).requestFocus();
			mInitDone=1;
		}
	}*/
	private void LockScreen(){
		if (mWakeLock != null) {
			try {
				if (mWakeLock.isHeld() == false){
					 mWakeLock.acquire();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    private void UnLockScreen(){
		if (mWakeLock != null) {
			try {
				if (mWakeLock.isHeld()) {
 					mWakeLock.release();
					mWakeLock.setReferenceCounted(false);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       }
    }

    private Runnable mRefreshUI = new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub		
			View view = mDeviceList.getChildAt(mOldPosition);
			
			if(null!=view){
				LOG("Refresh Hightlight in Runnable for DeviceList");
				onItemSelected(null, view, mOldPosition, 0);
			}
		}
    	
    };

    @Override
    public void onStart(){
    	LOG("Activity LifeCycle: default onStart");
    	super.onStart();
		/*Bug: start position of highlight is error,when we use MOUSE to start this app*/
		/*Solution: when we start this app, make lightlight invisible first*/
		if(null!=mItemHL1){
			mItemHL1.setVisibility(View.INVISIBLE);
		}
		if(null!=mItemHL2){
			mItemHL2.setVisibility(View.INVISIBLE);
		}
    }
    
    @Override
    protected void onResume() {
		LOG("Activity LifeCycle: default onResume");
        super.onResume();

		// TODO Auto-generated method stub	

		/*For Bug: Can't restore old highlight position;    make sure getting System Foucs First*/
		if(null!=mDeviceList){
		  mDeviceList.forceLayout();
		  mDeviceList.setSelection(mOldPosition);
		 // mDeviceList.requestFocus();
		}
		mHandler.postDelayed(mRefreshUI, 100);

		/*For Bug: Can't restore old content list,  make sure getting System Foucs Second*/
		if(null != mLastDirectory){
			LOG("Activity onResume mLastDirectory="+mLastDirectory);
			if(openDir(mLastDirectory)){
				enableButton(true,true);
			}
		}
		if((0==mOldPosition)&&(null==mLastDirectory)){
			if(openDir(flash_dir)){
				enableButton(true,true);
			}			
		}


		hideSystemUI();
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		UnLockScreen();
		mLastDirectory = mFileControl.get_currently_path();
		mFileControl.str_last_path = mLastDirectory;
		LOG("onStop(),mFileControl.str_last_path = " + mFileControl.str_last_path);
		mFileControl.last_item = last_item_tmp;

		CopyFileUtils.is_recover = false;
		CopyFileUtils.is_wait_choice_recover = false; //resume copy thread
		CopyFileUtils.is_enable_copy = false; // stop copy thread
		FileControl.is_enable_fill = false; // stop fill thread
		if (mDialogCopy != null) { // Close when Copy ended 
			mDialogCopy.dismiss();
			mDialogCopy = null;
		}
		if (mDelDialog != null && mDelDialog.isShowing()) {
			dissmissDelDialog();
		}
		if (delDialog != null && delDialog.isShowing()) {
			delDialog.dismiss();
			FileControl.is_enable_del = false;
		}
		
		if (openingDialog != null){
			openingDialog.dismiss();
			openingDialog = null;
		}
		
		if (mWaitDialog != null){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}

		if(mCifsProxy != null)
			mCifsProxy.onPause();
	}


	@Override
	public void onSystemUiVisibilityChange(int visibility) {
	    
		LOG("[Function] onSystemUiVisibilityChange() visibility=" + visibility);
	    
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
				hideSystemUI();
            }
        }, 50);
	}

	static int mOldPosition=0;
	boolean mAnimationEnable = true;
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {	
		Device item = mDevice.get(position);
		if(null == view || item == null){
			LOG("onItemSelected() fital error: view=null");
			return;
		}
		LOG("onItemSelected position="+position+";  view.height="+view.getY());
        LOG("onItemSelected "+"mPreviousItem="+mPreviousItem);
		
		final View bar_top = findViewById(R.id.bar_top);
		final View line_top = findViewById(R.id.image_line_top);
	
		mItemHL1.setLayoutParams(new LayoutParams(view.getWidth()+18,view.getHeight()+18)); 
		mItemHL2.setLayoutParams(new LayoutParams(view.getWidth()+6,view.getHeight()+6));
			
		//mHandler.removeCallbacksAndMessages(view);
		//Message msg = Message.obtain(mHandler, EnumConstent.MSG_ANI_ZOOM_IN, view);
		//mHandler.sendMessageDelayed(msg, 100);
		int pad_top = (int)(4.0f/AutoSize.getInstance().getDensityFactor());
		int pad_left = (int)(20.0f/AutoSize.getInstance().getDensityFactor());
			
		if(mPreviousItem == null){
			flyWhiteBorder(mItemHL1, view.getX()+pad_left-9, 
					bar_top.getHeight()+line_top.getHeight()+view.getY()+pad_top-9,0);
			flyWhiteBorder(mItemHL2, view.getX()+pad_left-3, 
					bar_top.getHeight()+line_top.getHeight()+view.getY()+pad_top-3,0);
		}else if (item.IsMount() && mAnimationEnable){
			flyWhiteBorder(mItemHL1, view.getX()+pad_left-9, 
					bar_top.getHeight()+line_top.getHeight()+view.getY()+pad_top-9,200);
			flyWhiteBorder(mItemHL2, view.getX()+pad_left-3, 
					bar_top.getHeight()+line_top.getHeight()+view.getY()+pad_top-3,200);
		}
		mPreviousItem = view;
        mAnimationEnable = true;
		
		int newPosition=0;
		if(position>mOldPosition){
			newPosition=this.mDeviceAdapter.getFucusDown(position);
			if(newPosition>=0){
				position=newPosition;
			}else{
				position=mOldPosition;
			}
		}else if(position<mOldPosition){
			newPosition=this.mDeviceAdapter.getFucusUp(position);
			if(newPosition>=0){
				position=newPosition;
			}else{
				position=mOldPosition;
			}
		}

		//mDeviceList.setSelection(position);
		//mDeviceList.requestFocus();
		mOldPosition=position;

	}

	private void flyWhiteBorder(ImageView image, float toX, float toY, int duration) {
		if (image != null) {
			image.setVisibility(View.VISIBLE);
			int width = image.getWidth();
			int height = image.getHeight();
			ViewPropertyAnimator animator = image.animate();
			animator.setDuration(duration);
			animator.scaleX(1);
			animator.scaleY(1);
			animator.x(toX);
			animator.y(toY);
			animator.start();
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		if (mPreviousItem != null) {
			//itemZoomOut();
			//mPreviousItem = null;
		}
	}

	private void itemZoomOut() {
		if (mHandler.hasMessages(EnumConstent.MSG_ANI_ZOOM_IN, mPreviousItem)) {
			mHandler.removeCallbacksAndMessages(mPreviousItem);
		} else {
			ZoomAnimation a = (ZoomAnimation) mPreviousItem.getAnimation();

			if (a != null) {
				a.resetForZoomOut();
				mPreviousItem.startAnimation(a);
				LOG("Reset for Zoomout....");
			}
		}
	}

    @Override
    public void onDestroy() {     
    	LOG("Activity LifeCycle: onDestroy");
        mLastDirectory = null;   
    	try {
            unregisterReceiver(mScanListener);
        } catch (IllegalArgumentException e) {
            LOG("unregisterReceiver error~");
        }
        super.onDestroy();
        mCifsProxy.onDestroy();
		System.gc();
    }


	public Adapter getAdpater()
	{
		return new NormalListAdapter(this, mFileControl.folder_array, mStorageManager);
	}
	
    public void initData(){
		mOldPosition=0;
    	mSavePath = new ArrayList<Path>();
    	mSavePath.add(new Path(mDefaultPath,""));
    	mPitSavePath = 0;
    	resources = this.getResources();
    	mSelectedPathList = new ArrayList<FileInfo>();
    	mEnableCopy = false;
		mEnablePaste = false;
    	mCopyFileUtils = new CopyFileUtils(mStorageManager);
		mCopyFileUtils.setEventHandler(mHandler);
		sdcard_dir = StorageUtils.getSDcardDir(mStorageManager);
		mSDCardPaths = StorageUtils.getSdCardPaths(mStorageManager);
		Log.i(TAG, "sdCardDir:" + sdcard_dir);
		usb_dir = StorageUtils.getUsbDir(mStorageManager);
		mUsbPaths = StorageUtils.getUsbPaths(mStorageManager);
		Log.i(TAG, "usbDir:" + sdcard_dir);
		FileControl.mRockExplorer = this;
    }

/*
	public boolean usbIsMount()
	{
		if(isMountUSB0() || isMountUSB1() || isMountUSB2() || isMountUSB3() || isMountUSB4() || isMountUSB4())
		{
			return true;
		}

		return false;
	}
*/
	
    private DeviceAdapter mDeviceAdapter = null;
	ArrayList<Device> mDevice = null;

	public Device getDevice(String tag)
	{
		if(tag == null)
			return null;

		for(int i = 0; i < mDevice.size(); i++)
		{
			Device device = mDevice.get(i);
			if(tag.equals(device.getTag()))
				return device;
		}

		return null;
	}

	public void initTextSize(){
		TextView text = (TextView)findViewById(R.id.explorer_name);
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSize.getInstance().getTextSize(0.04f));
		
		text = (TextView)findViewById(R.id.txt_dir_name);
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.035f));
		
		text = (TextView)findViewById(R.id.txt_file_name);
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.035f));

		text = (TextView)findViewById(R.id.msg_info);
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.035f));
	}
	
    public void initViews(){  
		int width = getWindowManager().getDefaultDisplay().getWidth(); 
		int height = getWindowManager().getDefaultDisplay().getHeight(); 
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int density = displayMetrics.densityDpi;

		AutoSize.getInstance().setWinSize(width, height,density);
		
		//set text size
		initTextSize();
		//set icon size
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)findViewById(R.id.explorer_logo).getLayoutParams();
		lp.width = (int)(AutoSize.getInstance().getTextSize(0.11f)*AutoSize.getInstance().getDensityFactor());
		lp.height = (int)(AutoSize.getInstance().getTextSize(0.11f)*AutoSize.getInstance().getDensityFactor());
		
		mDeviceList = (ListView)findViewById(R.id.device_list);
		rebuildDeviceList();
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
		 	mDeviceList.setSelector(R.drawable.yellow_border3);
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
            mDeviceList.setSelector(R.drawable.list_selector_background);
        }
		mDeviceList.setSelection(0);
		mDeviceList.getSelector().setAlpha(0);
		mDeviceList.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET); 
		mDeviceList.setOnItemClickListener(mDeviceListListener);
		mDeviceList.setOnItemSelectedListener(this);
		//mDeviceList.requestFocus();
		
		mItemHL1 = (ImageView) findViewById(R.id.yellow_image1);
		mItemHL2 = (ImageView) findViewById(R.id.yellow_image2);
		mItemHL1.setVisibility(View.INVISIBLE);
		mItemHL2.setVisibility(View.INVISIBLE);
		mDeviceList.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus){
					LOG("device list lose focus");
					mItemHL1.setVisibility(View.INVISIBLE);
				}else{
					mItemHL1.setVisibility(View.VISIBLE);
					mDeviceList.post(new Runnable() {
					    @Override
					    public void run() {
							LOG("device list get focus");
							mAnimationEnable = false;
					        mDeviceList.setSelection(mOldPosition);
					    }
					});
				}
				//mItemHL2.setVisibility(View.VISIBLE);
			}});


		mContentList = (ListView)findViewById(R.id.content_list);
		mContentList.setBackgroundDrawable(null);
        //hhq
        if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.GINGERBREAD){
			mContentList.setSelector(R.drawable.content_item);
        }else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD){
    		mContentList.setSelector(R.drawable.list_selector_background);
        }
		mContentList.setClickable(true);
		mContentList.setOnItemClickListener(mListItemListener);
		mContentList.setOnItemLongClickListener(mListItemLongListener);
		mContentList.setOnItemSelectedListener(mListItemSelectedListener);

    	mFileControl = new FileControl(this, null, mContentList, mStorageManager);

		mFileControl.last_item = 0;
		enableButton(false);

		
		image_multi_choice = (ImageView)findViewById(R.id.tool_multi_choice_ImageView);
		mSourcePath = mFileControl.get_currently_path();
		mTargetPath = mFileControl.get_currently_path();
		CopyFileUtils.mFileControl = mFileControl;

    }

	Runnable mResumeHL = new Runnable() {
		public void run() {
			if(mItemHL1!=null){
				mItemHL1.setVisibility(View.VISIBLE);
			}
			if(mDeviceList.getCount()>mOldPosition){
				mDeviceList.setSelection(mOldPosition);
				mDeviceList.requestChildFocus(mDeviceList.getChildAt(mOldPosition), null);
			}
		}
	};
	
	/**
	 *重新构建设备列表
	 */
	public void rebuildDeviceList(){
mDevice = new ArrayList<Device>();
		
		Device flash = new Device("flash",this.getResources().getString(R.string.str_flash_name),flash_dir,R.drawable.flash,isMountFLASH());//isMountFLASH(
		mDevice.add(flash);
		
		//in android 4.0 use permissions(<feature name="android.settings.sdcard" />) to show sata in UI
		if(!getPackageManager().hasSystemFeature("android.settings.sdcard"))
		{
			Device sdCard = new Device("sdCard",this.getResources().getString(R.string.str_sdcard_name),sdcard_dir,R.drawable.sdcard,isMountSD());//isMountSD()
			mDevice.add(sdCard);
		}
		
		Device usb = new Device("usb",this.getResources().getString(R.string.str_usb1_name),usb_dir,R.drawable.flash,isMountUSB());//usbIsMount()
		mDevice.add(usb);

		//android 2.3 use Property to control the UI to show sata,when Property(ro.explorer.supportsata) set to 1,then show sata
		//in android 4.0 use permissions(<feature name="android.settings.sata" />) to show sata in UI
//		if(!getPackageManager().hasSystemFeature("android.settings.sata") || (support_sata == 1))
//		{
//			Device sata = new Device("sata",this.getResources().getString(R.string.str_sata_name),sata_dir,R.drawable.flash,isSataMount());//isSataMount()
//			mDevice.add(sata);
//		}		
		
		Device smb = new Device("smb",this.getResources().getString(R.string.str_smb_name),smb_dir,R.drawable.smb,isNetworkAvailable() || isWifiApEnabled());
		mDevice.add(smb);

		mDeviceAdapter = new DeviceAdapter(this,mDevice);
		mDeviceList.setAdapter(mDeviceAdapter);
	}
	
	/**
	 * Get the State of Smb
	 */
	public boolean cifsIsMountAndConnect(String smbpath){
		String line = null;
		ArrayList<String> strlist = new ArrayList<String>();
		try {
			Process pro = Runtime.getRuntime().exec("mount");
			BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while ((line = br.readLine())!=null){
				strlist.add(line);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		ArrayList<String> cifslist = new ArrayList<String>();
    	for (String str : strlist){
    		String split[] = str.split(" ");
    		if (split[2].equals("cifs")){
    			cifslist.add(split[0].replace("\\"+"040", " ").replace("\\"+"134", "/"));
    		}
    	}
    	if(cifslist.contains(smbpath)){
    		String ip = smbpath.substring(2, smbpath.indexOf("/", 2));
    		if(ping(ip))
    			return true;
    		else
    			showToast(resources.getString(R.string.smb_host_error,ip));
    	}
    	return false;
	}
	
	public boolean ping(String ip){
		try  {
	           Socket server = new Socket();
	           InetSocketAddress address = new InetSocketAddress(ip,
	                    445);
	           server.connect(address, 4000);
	           server.close();
	        } catch (UnknownHostException e){
				try  {
	           		Socket server = new Socket();
	           		InetSocketAddress address = new InetSocketAddress(ip,
	                    139);
	           		server.connect(address, 4000);
	           		server.close();
	        		} catch (UnknownHostException e1){
	            		return false;
	       		 	} catch (IOException e1){
	            		return false;
	        		}
	        		return true;
	        } catch (IOException e){
				try  {
	           		Socket server = new Socket();
	           		InetSocketAddress address = new InetSocketAddress(ip,
	                    139);
	           		server.connect(address, 4000);
	           		server.close();
	        		} catch (UnknownHostException e1){
	            		return false;
	       		 	} catch (IOException e1){
	            		return false;
	        		}
	        		return true;
	        }
	        return true;
	}
	
	public String parseMountDirToSmbpath(String mountdir){
		String split [] = mountdir.substring(4).split("/");
		String smbpath = "//"+split[0]+"/"+split[1];
		return smbpath;
	}
    int TOOL_BAR_LEN = 800;
    int PER_MOVE = 10;
    int tool_bar_len = 0;
    int[] last_step = {-8, -6, -4, -3, -2, -2, -1, -1, 
    					1, 1, 2, 2, 3, 4, 6, 8};
    int last_step_pit = 0;
    Handler mhandlerscandata = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
				case 0:
					tool_bar_len -= PER_MOVE;
					if(tool_bar_len >= 0){
						tool_bar.scrollBy(-PER_MOVE, 0);
						mhandlerscandata.sendEmptyMessage(0);
					}else if(last_step_pit < last_step.length){
						tool_bar.scrollBy(last_step[last_step_pit], 0);
						last_step_pit ++;
						mhandlerscandata.sendEmptyMessage(0);
					}
					break;
			}
		}
	};
    OnScrollListener mOnScrollListener = new OnScrollListener(){
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(list_status_tmp != 0)
			last_item_tmp = firstVisibleItem;	
	}
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		list_status_tmp = scrollState;
	}        	
    };   
 
    public void setFileAdapter(boolean is_animation, boolean is_left)
    {
    	mTempListAdapter = new NormalListAdapter(this, new ArrayList<FileInfo>(mFileControl.folder_array), mStorageManager);
    	mContentList.setAdapter(mTempListAdapter);
    	mTempListAdapter.notifyDataSetChanged();

		LOG("setFileAdapter Path="+mFileControl.get_currently_path());
		mContentList.setSelection(mSelectedPosition >= mTempListAdapter.getCount() 
				? 0 : mSelectedPosition);
		mContentList.requestFocus();
    	setCurrentFileDir(mFileControl.get_currently_path());
    }
    
    public void setCurrentFileDir(String content){
    	if(content == null)
    		return;
		mTextTitle = (TextView)findViewById(R.id.txt_dir_name);
		String dir = mSavePath.get(mPitSavePath).getPathDirection();
		if((usb_dir != null) && content.startsWith(usb_dir)){		
    		mTextTitle.setText(getString(R.string.str_usb1_name) + content.substring(usb_dir.length()));		
    	}else if((sdcard_dir != null) && content.startsWith(sdcard_dir)){
//    		if(dir.equals("sdCard"))
    			mTextTitle.setText(getString(R.string.str_sdcard_name) + content.substring(sdcard_dir.length()));
//			else
//				mTextTitle.setText(getString(R.string.str_flash_name)  + content.substring(flash_dir.length()));
    	}else if((flash_dir != null) && content.startsWith(flash_dir)){
    		mTextTitle.setText(getString(R.string.str_flash_name)  + content.substring(flash_dir.length()));
    	}else if(content.equals(smb_dir)){
    		mTextTitle.setText(getString(R.string.str_smb_name));
    	}else if ((smb_dir != null) && content.startsWith(smb_dir+"/")){
    		mTextTitle.setText("smb://"+content.substring(4));
    	}else if ((smb_mountPoint != null) && content.startsWith(smb_mountPoint)){
    		String smbpath = parseMountDirToSmbpath(mCurrnetSmb);
    		mTextTitle.setText("smb:"+smbpath + content.substring(EnumConstent.mDirSmbMoutPoint.length()+15));
    	}else{
    		mTextTitle.setText(content);
		}
		mDirFilePath = mTextTitle.getText().toString();
    }

    public String getChangePath(String content){
	String ret = "";
        if(content == null)
                return null;
		if((usb_dir != null) && content.startsWith(usb_dir)){
                ret = getString(R.string.str_usb1_name) + content.substring(usb_dir.length());
        }else if((sdcard_dir != null) && content.startsWith(sdcard_dir)){
                ret = getString(R.string.str_sdcard_name) + content.substring(sdcard_dir.length());
        }else if((flash_dir != null) && content.startsWith(flash_dir)){
                ret = getString(R.string.str_flash_name)  + content.substring(flash_dir.length());
        }else if((smb_mountPoint != null) && content.startsWith(smb_mountPoint)){
        		String smbpath = mCifsProxy.getSmbFromMountPoint(content.substring(0, 24));
        		ret = "smb:"+smbpath + content.substring(smb_mountPoint.length()+15);
        }else{
                ret = content;
        }
	return ret;
    }
	
    View.OnClickListener title_listen = new View.OnClickListener(){
    	public void onClick(View v) {
    		if(tool_bar.getVisibility() == View.VISIBLE){
    			tool_bar.setVisibility(View.GONE);
    			title_image.setImageDrawable(resources.getDrawable(R.drawable.toolbar_down_arrow));
    		}else{
    			tool_bar.setVisibility(View.VISIBLE);
    			title_image.setImageDrawable(resources.getDrawable(R.drawable.toolbar_up_arrow));
    		}
    	}
  };

	private String mCurrentDir = null;
  	public String mCurrnetSmb = null;
  	private String mCurSmbUsername = null;
  	private String mCurSmbPassword = null;
  	private boolean mCurSmbAnonymous = true;
  	OnItemClickListener mDeviceListListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			Log.d(TAG,"mDeviceListListener.onItemClick position="+position);
			if (position >= mDevice.size()){
				return;
			}
			Device device = mDevice.get(position);
			if((device == null) || (!device.IsMount()))
				return ;
			String path = device.getPath();
			String tag = device.getTag();
			//if (tag.equals("usb") && (usb_dir != null))
			 //   path = usb_dir;
			    
			mCurrentDir = tag;
			mStorageType = tag;
			FileControl.setStorageType(mStorageType);
			if(openDir(path))
			{
				String devicePath = mSavePath.get(mSavePath.size()-1).getPath();
				String dir = mSavePath.get(mSavePath.size()-1).getPathDirection();
				if(!(path.equals(devicePath) && tag.equals(dir)))
				{
					mSavePath.add(new Path(path,tag));
				}
					
				mPitSavePath = mSavePath.size() - 1;
				Log.d(TAG,"mDeviceListListener,mPitSavePath = "+mPitSavePath);
				enableButton(true,true);
			}else{
				clearContentList();
				mSavePath.clear();            
				mSavePath.add(new Path(mDefaultPath,""));	
				mPitSavePath = mSavePath.size()-1;
				enableButton(true, true);
			}

			onItemSelected(parent,view,position,id);
		}
	};
	
	public boolean isItemClick = false;
	public FileInfo mCurListItem = null;
	public int mSelectedPosition=0;
	private OnItemSelectedListener mListItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// TODO Auto-generated method stub
			mSelectedPosition = arg2;
			String path = mFileControl.folder_array.get(mSelectedPosition).mFile.getName();

			TextView text;
			text = (TextView)findViewById(R.id.txt_dir_name);
			if(null!=text){ text.setText(mDirFilePath); }
			
			text = (TextView)findViewById(R.id.txt_file_name);
			if(null!=text){ text.setText(path); }
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			TextView text = (TextView)findViewById(R.id.txt_file_name);
			if(null!=text){ text.setText(""); }
		}
	};

    OnItemClickListener mListItemListener = new OnItemClickListener()
   	{
    	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			if(position >= mFileControl.folder_array.size())
				return;
			
			isItemClick = true;
			
    		FileInfo tmpFileInfo = mFileControl.folder_array.get(position);	
    		mCurListItem = tmpFileInfo;   	
    		
    		enableButton(true,true);
    		
    		if(tmpFileInfo.mIsDir){
    			mEnableLeft = true;
    			String path = tmpFileInfo.mFile.getPath();
				mCurrentDir = tmpFileInfo.mFile.getParent();
    			LOG("onItemClick open      File:" + path);
				LOG("onItemClick open Directory:" + mCurrentDir);
    			mSelectedPosition=0;			
    			if(openDir(path)){
    				if (!path.equals(mSavePath.get(mSavePath.size()-1).getPath())
    						|| !mCurrentDir.equals(mSavePath.get(mSavePath.size()-1).getPathDirection())){
    					mSavePath.add(new Path(path,mCurrentDir));
        				mPitSavePath = mSavePath.size() - 1;
    				}else{
    					mPitSavePath = mSavePath.size() - 1;
    				}
    				
    			}else if(!path.startsWith(EnumConstent.mDirSmb+"/")&&!path.startsWith(EnumConstent.mDirSmbMoutPoint)){
					clearContentList();
					mSavePath.clear();            
					mSavePath.add(new Path(mDefaultPath,""));	
					mPitSavePath = mSavePath.size()-1;	
    			}
    			enableButton(true,true);
    		}else{
    			if((tmpFileInfo.mFileType!=null)&&tmpFileInfo.mFileType.equals("..")){
					LOG("File(..), so Back to Last Folder");
					dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK));
					return ;
    			}
    			String path = tmpFileInfo.mFile.getPath();
    			if(path.startsWith(EnumConstent.mDirSmbMoutPoint)){
    				LOG("post wait dialog");
    				showWaitDialog();
    				
    				new Thread(new smbfileThreadRun(tmpFileInfo, position)).start();
    				return;
    			}

    			Intent intent = new Intent();
    		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		    intent.setAction(android.content.Intent.ACTION_VIEW);
    		    
    		        		    
    		    if(tmpFileInfo.mFileType.equals("image/*") || tmpFileInfo.mFileType.equals("audio/*")
    		    	|| tmpFileInfo.mFileType.equals("video/*")){
    		    	Uri tmpUri = null;
					if(tmpFileInfo.mUri!=null)
						tmpUri = Uri.parse(tmpFileInfo.mUri);
					else
						tmpUri = getFileUri(tmpFileInfo.mFile, tmpFileInfo.mFileType);
					if(tmpUri != null)
	    		    	intent.setDataAndType(tmpUri, tmpFileInfo.mFileType);
					else
						intent.setDataAndType(Uri.fromFile(tmpFileInfo.mFile),tmpFileInfo.mFileType);
    		    	}else    		    	
    		    		intent.setDataAndType(Uri.fromFile(tmpFileInfo.mFile),tmpFileInfo.mFileType);
    		    
    		    	try { 
						startActivity(intent);
		    		} catch (android.content.ActivityNotFoundException e) {
						Toast.makeText(RockExplorer.this, getString(R.string.noapp), Toast.LENGTH_SHORT).show();
                    	Log.e(TAG, "Couldn't launch music browser", e);
                    }
    		   }
    	}
    };

	public boolean checkPath(String path)
	{
		if(path == null)
			return false;

		Log.d(TAG,"checkPath, path = "+path);
		if((flash_dir != null) && path.startsWith(flash_dir) ||
		        (sdcard_dir != null) && path.startsWith(sdcard_dir) ||
		        (usb_dir != null) && path.startsWith(usb_dir) ||
		        (smb_mountPoint != null) && path.startsWith(smb_mountPoint))
		{
			Log.d(TAG,"checkPath, return true");
			return true;
		}

		return false;
	}

	
    OnItemLongClickListener mListItemLongListener = new OnItemLongClickListener(){
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			LOG("ContentList onItemLongClick position = " + position);

			mLongClickPosition = position;
			mSelectedPathList.clear();
			if(!mFileControl.is_first_path){
				if(0!=position){
					//file(..) only response paste operation, ignore other edit operation
					FileInfo tmpFileInfo = mFileControl.folder_array.get(position);
					mSelectedPathList.add(tmpFileInfo);
				}
				mEnableRename = true;	
				mEnableSmbEdit = true;
				if (mFileControl.currently_path.equals(smb_dir)){
					showSmbEditorDialog();
				}else if(!mFileControl.currently_path.startsWith(smb_dir+"/")){
					showEditorDialog();
				}
			}
			return true;
		}
    };
 

	Dialog mDialogEditor = null;
	public void showEditorDialog(){
		Log.d(TAG,"showEditorDialog()");
		View layout = View.inflate(RockExplorer.this, R.layout.editor_layout, null);
		layout.setLayoutParams(AutoSize.getInstance().getLayoutParams(0.33f,0.45f));
		
		View temp = (View)layout.findViewById(R.id.edit_copy);
        temp.setOnClickListener(EditorClickListener);
		temp.setLeft(AutoSize.getInstance().getMargin(0.03f));
		TextView text = (TextView)layout.findViewById(R.id.edit_copy_text);
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));
		
        temp = (View)layout.findViewById(R.id.edit_delete);
        temp.setOnClickListener(EditorClickListener);
		temp.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text = (TextView)layout.findViewById(R.id.edit_delete_text);
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));

        temp = (View)layout.findViewById(R.id.edit_move);
        temp.setOnClickListener(EditorClickListener);
		temp.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text = (TextView)layout.findViewById(R.id.edit_move_text);
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));
		
        temp = (View)layout.findViewById(R.id.edit_paste);
        temp.setOnClickListener(EditorClickListener);
		temp.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text = (TextView)layout.findViewById(R.id.edit_paste_text);
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));

        temp = (View)layout.findViewById(R.id.edit_rename);
        temp.setOnClickListener(EditorClickListener);
		temp.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text = (TextView)layout.findViewById(R.id.edit_rename_text);
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));

		temp = (View)layout.findViewById(R.id.edit_share);
        temp.setOnClickListener(EditorClickListener);
		temp.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text = (TextView)layout.findViewById(R.id.edit_share_text);
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));

        setEditDialog(layout);
        mDialogEditor = new Dialog(RockExplorer.this, R.style.MyDialog);
        mDialogEditor.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mDialogEditor.setContentView(layout);

        mDialogEditor.show();
	}
	
	public void showSmbEditorDialog(){
		Log.d(TAG,"showSmbEditorDialog");
		View layout = View.inflate(RockExplorer.this, R.layout.smb_editor_layout, null);
		View temp = (View)layout.findViewById(R.id.smb_search);
        temp.setOnClickListener(EditorClickListener);
        temp = (View)layout.findViewById(R.id.smb_new);
        temp.setOnClickListener(EditorClickListener);
        temp = (View)layout.findViewById(R.id.smb_delete);
        temp.setOnClickListener(EditorClickListener);
        temp = (View)layout.findViewById(R.id.smb_edit);
        temp.setOnClickListener(EditorClickListener);

		TextView text = null;
		text = (TextView)layout.findViewById(R.id.smb_search_text);		
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));
		text = (TextView)layout.findViewById(R.id.smb_new_text);		
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));
		text = (TextView)layout.findViewById(R.id.smb_delete_text);		
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));
		text = (TextView)layout.findViewById(R.id.smb_edit_text);		
		text.setLeft(AutoSize.getInstance().getMargin(0.03f));
		text.setTextSize(TypedValue.COMPLEX_UNIT_PX,AutoSize.getInstance().getTextSize(0.04f));


        setSmbEditDialog(layout);

        mDialogEditor = new Dialog(RockExplorer.this, R.style.MyDialog);
        mDialogEditor.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mDialogEditor.setContentView(layout);

        mDialogEditor.show();
	}

	private final int MSG_BEGIN_DEL = 0;
	private final int MSG_SCAN_DEL = 1;
	private Handler mDelHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case MSG_BEGIN_DEL:
				if(delDialog == null)
				{
					delDialog = new ProgressDialog(RockExplorer.this);
					delDialog.setTitle(R.string.str_copyingtitle);
					delDialog.setMessage(getString(R.string.str_delcontext));
					delDialog.setOnDismissListener(new OnDismissListener()
					{
						public void onDismiss(DialogInterface dialog) 
						{
							FileControl.is_enable_del = false;
						}
					});
					delDialog.show();
				}
				else
				{
        	        delDialog.show();
	            }	
				mDelHandler.sendEmptyMessageDelayed(MSG_SCAN_DEL, 100);
				break;
				
			case MSG_SCAN_DEL:
				if(FileControl.is_finish_del)
				{
					Log.d(TAG,"delete file num = "+FileControl.deleteFileInfo.size());
					for(int i = 0; i < FileControl.deleteFileInfo.size(); i ++)
					{
						mFileControl.folder_array.remove(FileControl.deleteFileInfo.get(i));
					}
					setFileAdapter(false, false);
					rescanStorage(mFileControl.get_currently_path());

					LOG("mSelectedPathList.size = " + mSelectedPathList.size());
					mSelectedPathList = new ArrayList<FileInfo>();
					delDialog.dismiss();
					openDir(fill_path); //  update current ListView
				}
				else
				{
					mDelHandler.sendEmptyMessageDelayed(MSG_SCAN_DEL, 100);
				}
				
				break;
			}
		}
	};
	
	public Dialog CreateDelDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(RockExplorer.this);
		builder.setTitle(R.string.str_sure_del);
		builder.setMessage(getString(R.string.str_sure_del_ask));
		builder.setPositiveButton(R.string.str_del, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mFileControl.deleteFileInfo(mSelectedPathList);
    			dissmissDelDialog();
				mDelHandler.sendEmptyMessageDelayed(MSG_BEGIN_DEL, 100);
			}
		});
		builder.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// do nothing
				mSelectedPathList = new ArrayList<FileInfo>();
				dissmissDelDialog();
			}
		});
		return builder.create();
	}
	
	View myView;
    EditText myEditText;
	public void FileRename(final FileInfo mFileInfo)	
	{
		final File file = mFileInfo.mFile;
        LayoutInflater factory=LayoutInflater.from(RockExplorer.this);
        myView=factory.inflate(R.layout.rename_alert_dialog,null);
        myEditText=(EditText)myView.findViewById(R.id.mEdit);
        myEditText.setText(file.getName());

        android.content.DialogInterface.OnClickListener listener2=
        new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int which)
          {
            String modName=myEditText.getText().toString();
	    if(modName.contains("\\") || modName.contains("/") || modName.contains(":")
                || modName.contains("*") || modName.contains("?") || modName.contains("\"")
                || modName.contains("<") || modName.contains(">") || modName.contains("|")){
                Toast.makeText(RockExplorer.this, getString(R.string.rename_error), Toast.LENGTH_SHORT).show();
                return;
            }
            final String pFile=file.getParentFile().getPath()+"/";
            final String newPath=pFile+modName;

            if(new File(newPath).exists())
            {
              if(!modName.equals(file.getName()))
              {
                new AlertDialog.Builder(RockExplorer.this)
                    .setTitle(getString(R.string.str_rename_notice))
                    .setMessage(getString(R.string.str_rename_file_exist))
                    .setPositiveButton(getString(R.string.str_OK),new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface dialog, int which)
                      {
                        if(!file.renameTo(new File(newPath))){
                        	Toast.makeText(RockExplorer.this, resources.getString(R.string.rename_fail), Toast.LENGTH_SHORT).show();
                        	mEnableRename = false;
                        	return;
                        }
                        mFileControl.folder_array.remove(mFileInfo);
                        mEnableRename = false; 
                        LOG("mLongClickPosition = " + mLongClickPosition);

                        FileInfo tmp_fileinfo = mFileControl.changeFiletoFileInfo(new File(newPath));
                        mFileControl.folder_array.add(mLongClickPosition, tmp_fileinfo);
          			    mEnableRename = false; 
          			    setFileAdapter(false, false);
          			    mContentList.setSelection(mLongClickPosition);
          			    rescanStorage(mFileControl.get_currently_path());
                        dialog.dismiss();
                      }
                    }) 
                    .setNegativeButton(getString(R.string.str_cancel),new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface dialog,int which)
                      {
                    	  dialog.dismiss();
                      }
                    }).show();
              }
            }
            else
            {
              if(!file.renameTo(new File(newPath))){
            	  Toast.makeText(RockExplorer.this, resources.getString(R.string.rename_fail), Toast.LENGTH_SHORT).show();
              	mEnableRename = false;
              	return;
              }
              mFileControl.folder_array.remove(mFileInfo);
              if(mFileInfo.mIsDir){
              	mFileControl.folder_array.add(0, mFileControl.changeFiletoFileInfo(new File(newPath)));
              }else{
              	mFileControl.folder_array.add(mFileControl.changeFiletoFileInfo(new File(newPath)));
              }
			  mEnableRename = false; 
			  setFileAdapter(false, false);
			  //if(isMultiMediaFile(mFileInfo))
			  rescanStorage(mFileControl.get_currently_path());
			  dialog.dismiss();
            }
            LOG("the select id = " + mContentList.getSelectedItemPosition());
          }
        };

        AlertDialog renameDialog = new AlertDialog.Builder(RockExplorer.this).create();
        renameDialog.setView(myView);

        renameDialog.setButton(getString(R.string.str_OK),listener2);
        renameDialog.setButton2(getString(R.string.str_cancel),new DialogInterface.OnClickListener()
        {
          public void onClick(DialogInterface dialog, int which)
          {
				// hh@2012.08.07 reset the color 
				if(mCurListItem != null)
				{
					mCurListItem.mIsSelected = false;
					mCurListItem = null;
					mTempListAdapter.notifyDataSetChanged();
				}
          }
        });
        renameDialog.show();
	}
	

	private View.OnClickListener EditorClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub			
			switch(v.getId()){
			case R.id.edit_copy:		
				mEnableMove = false;
				mEnableCopy = true;	
				if((mSelectedPathList != null) && (mSelectedPathList.size() > 0))
				{
					mSourcePathList = mSelectedPathList;
					mSelectedPathList = new ArrayList<FileInfo>();
				}
				break;
				
			case R.id.edit_delete:
				mDelDialog.show();				
				mEnableCopy = false;
				break;
				
			case R.id.edit_move:		
				mEnableMove = true; 
				mEnableCopy = true;	
				if((mSelectedPathList != null) && (mSelectedPathList.size() > 0))
				{
					if(mSelectedPathList.get(0).mFileType.equals("..")){
						mEnableMove = false; 
				        mEnableCopy = false;	
					}else{
					    mSourcePathList = mSelectedPathList;
					    mSelectedPathList = new ArrayList<FileInfo>();
					}
				}
				break;
				
			case R.id.edit_paste:
				mEnableCopyInBg = false;

				AlertDialog dialog = new AlertDialog.Builder(RockExplorer.this)
					.setTitle(RockExplorer.this.getString(R.string.edit_copy))
					.setMessage(R.string.copy_background)
					.setPositiveButton(R.string.background,new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							mEnableCopyInBg  = true;
							mHandler.sendEmptyMessage(EnumConstent.MSG_OP_START_COPY);
							dialog.dismiss();
						}
					})
					.setNegativeButton(R.string.foreground,new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							mEnableCopyInBg = false;
							mHandler.sendEmptyMessage(EnumConstent.MSG_OP_START_COPY);
							dialog.dismiss();
						}
					})
					.show();
				break;
				
			case R.id.edit_rename:
				if(mSelectedPathList.get(0).mFile.canWrite()){
					FileRename(mSelectedPathList.get(0));
				}
				mEnableCopy = false;
				mSelectedPathList = new ArrayList<FileInfo>(); 
				break;
			case R.id.edit_share:
				onShare();
				break;
            case R.id.smb_search:
            	mCifsProxy.searchSmb();
				mEnableCopy = false;
            	break;
            	
            case R.id.smb_new:
            	mCifsProxy.CreateNewSmb();
				mEnableCopy = false;
            	break;
            	
            case R.id.smb_edit:
            	mCifsProxy.EditSmb(mSelectedPathList.get(0));
				mEnableCopy = false;
            	break;
            
            case R.id.smb_delete:
            	mCifsProxy.delete(mSelectedPathList);
				mEnableCopy = false;
            	break;
			default:
				break;
			}

			if(mDialogEditor != null)
				mDialogEditor.dismiss();
		}
	};

	void copyInForeground()
	{
		if(mSourcePathList.size()>0){
		  LOG("verifyTargetPath SourceParent=" + mSourcePathList.get(0).mFile.getParent());
		}else{
		  LOG("verifyTargetPath mSourcePathList.size()=0");
		}
		LOG("verifyTargetPath DistanceParent=" + mFileControl.get_currently_path());

		if(verifyTargetPath())
		{
			if(!verityPermission()){
				Toast.makeText(RockExplorer.this, resources.getString(R.string.write_error), Toast.LENGTH_SHORT).show();
				return;
			}


			if(mEnableCopy)
				mEnablePaste = true;
			mHandler.sendEmptyMessage(EnumConstent.MSG_DLG_COUNT);
		}
	}
	
	void copyInBackgournd()
	{
		Intent intent = new Intent(RockExplorer.this,CopyService.class);
		Bundle mBundle = new Bundle(); 

		if(mEnableMove)
			mBundle.putInt("command",CopyService.MOVE);
		else
			mBundle.putInt("command",CopyService.COPY);

		ArrayList<File> array = new ArrayList<File>();
		for(int i = 0; i < mSourcePathList.size(); i ++)
			array.add(mSourcePathList.get(i).mFile);
		mBundle.putSerializable("source", array);
		String currentPath = mFileControl.get_currently_path();
		mBundle.putString("target",currentPath);
		intent.putExtras(mBundle);

		RockExplorer.this.startService(intent);
	}
	
	Intent mIntent = null;
	ResolveAdapter mResolveAdapter = null;
	AlertDialog mShareDialog;
	void onShare()
	{
		mIntent = new Intent();
		
		{
			ArrayList<Uri> uris = new ArrayList<Uri>();
			HashSet<String> type = new HashSet<String>();
			for(int i = 0; i < mSelectedPathList.size(); i++)
			{
				File file = mSelectedPathList.get(i).mFile;
				if(file.isDirectory())
				{
					getDirectoryFile(file,uris,type);
				}
				else
				{
					String uri = "file://"+file.toString();
					uris.add(Uri.parse(uri));

					String mimeType = mFileControl.getMIMEType(file);
					if(!type.contains(mimeType))
					{
						type.add(mimeType);
					}
				}
			}
			if(uris.size() > 1)
			{
				mIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
				mIntent.putExtra(Intent.EXTRA_STREAM, uris);
			}
			else if(uris.size() == 1)
			{
				mIntent.setAction(Intent.ACTION_SEND);
				mIntent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
			}

			if(type.size() == 1)
			{
				Iterator<String> it = type.iterator();
				mIntent.setType(it.next());
			}
			else
			{
				Iterator<String> it = type.iterator();
				do
				{
					String mime = it.next();
					Log.d(TAG,"onShare()**************** mimeType = "+mime);
				}while(it.hasNext());
				mIntent.setType("*/*");
			}
		}
		
		PackageManager packageManager = RockExplorer.this.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mIntent, 0);
        mResolveAdapter = new ResolveAdapter(RockExplorer.this,activities);
		ListView view = new ListView(RockExplorer.this);
		view.setAdapter(mResolveAdapter);
		view.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					startResolvedActivity(mIntent,(ResolveInfo)mResolveAdapter.getItem(position));
					mShareDialog.dismiss();
				}
			});
		mShareDialog = new AlertDialog.Builder(RockExplorer.this)
		.setTitle(RockExplorer.this.getString(R.string.select))
		.setView(view)
		.show();
	}

	void getDirectoryFile(File file,ArrayList<Uri> uris,HashSet<String> type)
	{
		File[] files = file.listFiles();
		for(int i = 0; i < files.length; i++)
		{
			if(files[i].isDirectory())
			{
				getDirectoryFile(files[i],uris,type);
			}
			else
			{
				String uri = "file://"+files[i].toString();
				uris.add(Uri.parse(uri));
				
				String mimeType = mFileControl.getMIMEType(file);
				if(!type.contains(mimeType))
				{
					type.add(mimeType);
				}
			}
		}
	}
	
	private void startResolvedActivity(Intent intent, ResolveInfo info) 
	{
		final Intent resolvedIntent = new Intent(intent);
		ActivityInfo ai = info.activityInfo;
		resolvedIntent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
		mHandler.post(new Runnable() 
		{
			public void run() 
			{
				try
				{
					startActivity(resolvedIntent);
				}
				catch(ActivityNotFoundException e)
				{
					
				}
				catch(Exception e)
				{
				}
			}
		});
	}
	
	public class ResolveAdapter extends BaseAdapter
	{
		List<ResolveInfo> mActivitys;
		Context mContext = null;
		LayoutInflater flater = null;
		PackageManager packageManager;
		ResolveAdapter(Context context,List<ResolveInfo> array)
		{
			mContext = context;
			mActivitys = array;
			flater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			packageManager = mContext.getPackageManager();
		}

		public int getCount() 
		{
			if(mActivitys != null)
				return mActivitys.size();

			return 0;
		}

		public Object getItem(int position) 
		{
			if(mActivitys != null)
				return mActivitys.get(position);

			return null;
		}

		public long getItemId(int position) 
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ResolveInfo info = mActivitys.get(position);
			String label = info.loadLabel(packageManager).toString();
			Drawable icon = info.loadIcon(packageManager);
			//reslover_adapter
			View view = (View)flater.inflate(R.layout.reslover_adapter,null);

			ImageView image = (ImageView)view.findViewById(R.id.image);
			image.setImageDrawable(icon);
			TextView text = (TextView)view.findViewById(R.id.name);
			text.setText(label);

			return view;
		}
	}
	
	
	private boolean verityPermission(){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String testfilename = mFileControl.get_currently_path()+"/"+sf.format(new Date());
		try{
		if (!new File(testfilename).createNewFile()){
			return false;
		}else{
			new File(testfilename).delete();
			return true;
		}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean SmbReadPermissionTest(String path){
		String host;
		String sharename;
		SmbFile smbfile;
		
		String split[] = mCurrnetSmb.substring(4).split("/");
		host = split[0];
		sharename = split[1];
		if (mCurSmbAnonymous){
			LOG("smb://guest:@"+host+"/"+sharename+path.substring(24)+"/");
			try{
				smbfile = new SmbFile("smb://guest:@"+host+"/"+sharename+path.substring(24)+"/");
				smbfile.list();
			}catch (SmbAuthException e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}catch (MalformedURLException e) {
				e.printStackTrace();
				return false;
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}else {
			try{
				LOG("smb://"+mCurSmbUsername+":"+mCurSmbPassword+"@"+host+"/"+sharename+path.substring(24)+"/");
				smbfile = new SmbFile("smb://"+mCurSmbUsername+":"+mCurSmbPassword+"@"+host+"/"+sharename+path.substring(24)+"/");
				smbfile.list();
			}catch (SmbAuthException e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}catch (MalformedURLException e) {
				e.printStackTrace();
				return false;
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case EnumConstent.MSG_DLG_DELETE:
				return CreateDelDialog();
		}
		return super.onCreateDialog(id);
	}

	public void clearContentList()
	{
		mContentList.setAdapter(null);
		mContentList.setBackgroundDrawable(null);
		mContentList.invalidate();
		mPitSavePath --;
		if (mPitSavePath < 0)
			mPitSavePath =0;
		enableButton(true,true);
		setCurrentFileDir("");
	}
	
	/**/
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_BACK:	
					isItemClick = false;
					for(int i = 0; i < mSavePath.size(); i++)
						LOG("dispatchKeyEvent*********, mSavePath("+i+") = "+mSavePath.get(i).getPath());
					
					if(mFileControl.is_first_path)
					{
						if(mDialogCopy != null){
							CopyFileUtils.is_copy_finish = true;
							CopyFileUtils.is_enable_copy = false;
	    					}
						if(mDelDialog!=null && mDelDialog.isShowing()){
							FileControl.is_enable_del = false;
						}
						if(!FileControl.is_finish_fill)
							FileControl.is_enable_fill = false;
						
						break;
					}
					
					if(mPitSavePath < 0){
						mPitSavePath = 0;
						break ;
					}

					Log.d(TAG,"mPitSavePath = "+mPitSavePath);
					if(mSavePath.get(mPitSavePath).getPath().equals(mDefaultPath))
					{
						break;
					}
					String dir = mSavePath.get(mPitSavePath).getPathDirection();
					mCurrentDir = dir;
					if((mFileControl.get_currently_path().equals(flash_dir) || 
						(mFileControl.get_currently_path().equals(sdcard_dir) && (!dir.equals("flash"))) ||
						mFileControl.get_currently_path().equals(usb_dir) ||
//						mFileControl.get_currently_path().equals(sata_dir) ||
						mFileControl.get_currently_path().equals(smb_dir)))
					{
						Log.d(TAG,"clearContentList");
						clearContentList();
						mPitSavePath = 0;
						mCurrentDir = mDefaultPath;
					}
					else
					{
						if(mFileControl.get_currently_path().equals(sdcard_dir))
						{
							Log.d(TAG,"path = "+flash_dir);
							openDir(flash_dir);
							mPitSavePath = 1;
							mCurrentDir = new String("flash");
						}
						else
						{
							Log.d(TAG,"openDir,path = "+mFileControl.get_currently_parent());
							
							String path = mFileControl.get_currently_parent();
							
							openDir(mFileControl.get_currently_parent());							
							mPitSavePath--;
						}
					}
					
					enableButton(true,true);
					Log.d(TAG,"mPitSavePath = "+mPitSavePath);

				return true;
				case KeyEvent.KEYCODE_MENU:
					if(mContentList.getChildCount()>0){
						int index = mContentList.getSelectedItemPosition();
						if(index>=0){
							mListItemLongListener.onItemLongClick(null,null,index,0);
						}
					}
		
				return true;
				default:
					break;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed(){
		LOG("Activity LifeCycle: onBackPressed");
		this.finish();
	}

	Dialog mDelDialog = null;	
	public void dissmissDelDialog(){
		mDelDialog.dismiss();
	}
	
	public void setEditDialog(View layout){
		View temp_edit_copy = (View)layout.findViewById(R.id.edit_copy);
		View temp_edit_move = (View)layout.findViewById(R.id.edit_move);
		View temp_edit_delete = (View)layout.findViewById(R.id.edit_delete);
		View temp_edit_rename = (View)layout.findViewById(R.id.edit_rename);
		View temp_edit_paste = (View)layout.findViewById(R.id.edit_paste);
		View temp_edit_share = (View)layout.findViewById(R.id.edit_share);
		
		TextView temp_edit_copy_text = (TextView)layout.findViewById(R.id.edit_copy_text);
		TextView temp_edit_move_text = (TextView)layout.findViewById(R.id.edit_move_text);
		TextView temp_edit_delete_text = (TextView)layout.findViewById(R.id.edit_delete_text);
		TextView temp_edit_rename_text = (TextView)layout.findViewById(R.id.edit_rename_text);
		TextView temp_edit_paste_text = (TextView)layout.findViewById(R.id.edit_paste_text);
		TextView temp_edit_share_text = (TextView)layout.findViewById(R.id.edit_share_text);
		
		temp_edit_paste.setEnabled(false);
		setAlphaAndTextColor((TextView)temp_edit_paste_text, false);
		Log.d(TAG,"setEditDialog,mSelectedPathList.size() = "+mSelectedPathList.size());
		if(mSelectedPathList.size() == 0){
			temp_edit_copy.setEnabled(false);
			setAlphaAndTextColor(temp_edit_copy_text, false);
			
			temp_edit_move.setEnabled(false);
			setAlphaAndTextColor(temp_edit_move_text, false);
			
			temp_edit_delete.setEnabled(false);
			setAlphaAndTextColor(temp_edit_delete_text, false);
			
			temp_edit_rename.setEnabled(false);
			setAlphaAndTextColor(temp_edit_rename_text, false);

			temp_edit_share.setEnabled(false);
			setAlphaAndTextColor(temp_edit_share_text, false);
			mEnableRename = false;
			
			if(mEnableCopy||mEnableMove)
			{
				temp_edit_paste.setEnabled(true);
				setAlphaAndTextColor(temp_edit_paste_text, true);
			}
		}else{					
			if(mEnableCopy||mEnableMove)
			{
				temp_edit_paste.setEnabled(true);
				setAlphaAndTextColor(temp_edit_paste_text, true);
			}
			else
			{
				temp_edit_paste.setEnabled(false);
				setAlphaAndTextColor(temp_edit_paste_text, false);
			}

			boolean is_share = true;
			for(int i = 0; i < mSelectedPathList.size(); i++)
			{
				FileInfo info = mSelectedPathList.get(i);
				if(info.mFile.isDirectory())
				{
					is_share = false;
					break;
				}
			}


			if(mEnableMove)
			{
				temp_edit_move.setEnabled(true);
				setAlphaAndTextColor(temp_edit_move_text, true);
			}
			else
			{
				temp_edit_move.setEnabled(false);
				setAlphaAndTextColor(temp_edit_move_text, false);
			}

			if(mSelectedPathList.size() == 1)
			{
				temp_edit_rename.setEnabled(true);
				setAlphaAndTextColor(temp_edit_rename_text, true);
			}
			else
			{
				temp_edit_rename.setEnabled(false);
				setAlphaAndTextColor(temp_edit_rename_text, false);
			}

			if (verityPermission()){
					temp_edit_move.setEnabled(true);
					setAlphaAndTextColor(temp_edit_move_text, true);
				
					temp_edit_delete.setEnabled(true);
					setAlphaAndTextColor(temp_edit_delete_text, true);
					mEnableRename = true;
			}else {
					temp_edit_move.setEnabled(false);
					setAlphaAndTextColor(temp_edit_move_text, false);
					
					temp_edit_delete.setEnabled(false);
					setAlphaAndTextColor(temp_edit_delete_text, false);
					mEnableRename = false;
			}
		}	
		if(mEnableRename){
			temp_edit_rename.setEnabled(true);
			setAlphaAndTextColor(temp_edit_rename_text, true);
			mEnableRename = false;
		}else{
			temp_edit_rename.setEnabled(false);
			setAlphaAndTextColor(temp_edit_rename_text, false);
		}
	}
	
	public void setSmbEditDialog(View layout){
		View smb_search = (View)layout.findViewById(R.id.smb_search);
		View smb_new = (View)layout.findViewById(R.id.smb_new);
		View smb_delete = (View)layout.findViewById(R.id.smb_delete);
		View smb_edit = (View)layout.findViewById(R.id.smb_edit);
		
		TextView smb_search_text = (TextView)layout.findViewById(R.id.smb_search_text);
		TextView smb_new_text = (TextView)layout.findViewById(R.id.smb_new_text);
		TextView smb_delete_text = (TextView)layout.findViewById(R.id.smb_delete_text);
		TextView smb_edit_text = (TextView)layout.findViewById(R.id.smb_edit_text);
		
		if(mSelectedPathList.size() == 0){  //delete unenabled
			smb_delete.setEnabled(false);
			setAlphaAndTextColor(smb_delete_text, false);
		}else{
			smb_delete.setEnabled(true);
			setAlphaAndTextColor(smb_delete_text, true);
		}
		
		if (mEnableSmbEdit){
			smb_edit.setEnabled(true);
			setAlphaAndTextColor(smb_edit_text, true);
			mEnableSmbEdit = false;
		}else{
			smb_edit.setEnabled(false);
			setAlphaAndTextColor(smb_edit_text, false);
		}
	}
	public void setAlphaAndTextColor(TextView temp_Text, boolean visibility){
		if(visibility){
			temp_Text.setTextColor(0xffffffff);
		}else{
			temp_Text.setTextColor(0xff848484);
		}
	}
	

	Handler mCopyHandler = new Handler();	
	Runnable mCopyRun = new Runnable() {
		public void run() { 
			if(CopyFileUtils.is_copy_finish){
				
				
				if ((CopyFileUtils.cope_now_sourceFile != null)
						&& (CopyFileUtils.cope_now_targetFile != null)) {
					int percent = (int) ((((float) CopyFileUtils.mHasCopytargetFileSize) / ((float) CopyFileUtils.cope_now_sourceFile
							.length())) * 100);
					LOG(" CopyFileUtils.cope_now_targetFile.length() = "
							+ CopyFileUtils.mHasCopytargetFileSize);
					LOG(" CopyFileUtils.cope_now_sourceFile.length() = "
							+ CopyFileUtils.cope_now_sourceFile.length());
					LOG(" percent = " + percent);
					((ProgressBar) myCopyView
							.findViewById(R.id.one_copy_percent))
							.setProgress((int) percent);
					((TextView) myCopyView.findViewById(R.id.one_percent_Text))
							.setText(percent + " %");

					percent = (int) ((((float) CopyFileUtils.mhascopyfilecount) / ((float) CopyFileUtils.mallcopyfilecount)) * 100);
					((ProgressBar) myCopyView
							.findViewById(R.id.all_copy_percent))
							.setProgress((int) percent);
					((TextView) myCopyView.findViewById(R.id.all_percent_Text))
							.setText("" + CopyFileUtils.mhascopyfilecount
									+ " / " + CopyFileUtils.mallcopyfilecount);
					LOG(" mhascopyfilecount = "
							+ CopyFileUtils.mhascopyfilecount
							+ ", CopyFileUtils.mallcopyfilecount = "
							+ CopyFileUtils.mallcopyfilecount);
				}
				
				
				UnLockScreen();
				LOG("mCopyRun, the CopyFileUtils.is_copy_finish = " + CopyFileUtils.is_copy_finish);
				LOG("mCopyRun, mEnableMove = " + mEnableMove);
				CopyFileUtils.mHasCopytargetFileSize = 0;
				if(mEnableMove && !CopyFileUtils.is_same_path){
					mEnableMove = false;
					mEnableCopy = false;
					
					mFileControl.deleteFileInfo(mCopyFileUtils.get_has_copy_path());					
					if(mCopyFileUtils.get_has_copy_path().size() > 0)
						rescanStorage(mCopyFileUtils.get_has_copy_path().get(0).mFile.getParent());
				}
				LOG("mCopyRun, CopyFileUtils.is_enable_copy = " + CopyFileUtils.is_enable_copy);
				if(!CopyFileUtils.is_enable_copy){
					mFileControl.deleteFile(CopyFileUtils.mInterruptFile);
				}
				
				for(int i = 0; i < mCopyFileUtils.get_has_copy_path().size(); i ++){
					File tmp_file=new File(mFileControl.get_currently_path()+File.separator+mCopyFileUtils.get_has_copy_path().get(i).mFile.getName());
					FileInfo tmp_fileinfo = mFileControl.changeFiletoFileInfo(tmp_file);
					if(!mFileControl.isFileInFolder(tmp_file)){
						if(!mFileControl.folder_array.contains(tmp_fileinfo)){
							if(tmp_file.isDirectory()){	//file is folder
								mFileControl.folder_array.add(0, tmp_fileinfo);
							}else{
								mFileControl.folder_array.add(tmp_fileinfo);
							}
						}
					}
				}
				setFileAdapter(false, false);//reflesh	
				
				if(mDialogCopy != null){
					mDialogCopy.dismiss();
					mDialogCopy = null;
				}

				rescanStorage(mFileControl.get_currently_path());	

				CopyFileUtils.is_copy_finish = false;
				if(CopyFileUtils.is_not_free_space){
					CopyFileUtils.is_not_free_space = false;
					if(CopyFileUtils.pathError)
						Toast.makeText(RockExplorer.this, getString(R.string.error_invalid_path), Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(RockExplorer.this, getString(R.string.err_not_free_space), Toast.LENGTH_SHORT).show();
				}
				
				mCopyHandler.removeCallbacks(mCopyRun);
				try
				{
				    Thread.sleep(1000);
				}catch(Exception ex){
				    Log.e(TAG, "Exception: " + ex.getMessage());
				}
				openDir(fill_path);
				setFileAdapter(false, false);
				
				mCopyFileUtils.setSourcePath("");
				mCopyFileUtils.setTargetPath("");
				
				CopyFileUtils.cope_now_sourceFile=null;
				CopyFileUtils.cope_now_targetFile=null;
				
			}else{
				if(mEnablePaste){
					LOG("in mCopyRun, the mEnableCopy = " + mCopyRun);
					mEnablePaste = false;
					LockScreen();
					mCopyFileUtils.CopyFileInfoArray(mSourcePathList, mFileControl.get_currently_path());
				}
				if((CopyFileUtils.cope_now_sourceFile != null) && (CopyFileUtils.cope_now_targetFile != null)){
					((TextView)myCopyView.findViewById(R.id.source_Text)).setText(getChangePath(CopyFileUtils.cope_now_sourceFile.getPath()));
					((TextView)myCopyView.findViewById(R.id.target_Text)).setText(getChangePath(CopyFileUtils.cope_now_targetFile.getPath()));
					
					int percent = (int)((((float)CopyFileUtils.mHasCopytargetFileSize) / ((float)CopyFileUtils.cope_now_sourceFile.length())) * 100);
					LOG(" CopyFileUtils.cope_now_targetFile.length() = " + CopyFileUtils.mHasCopytargetFileSize);
					LOG(" CopyFileUtils.cope_now_sourceFile.length() = " + CopyFileUtils.cope_now_sourceFile.length());
					LOG(" percent = " + percent);
					((ProgressBar)myCopyView.findViewById(R.id.one_copy_percent)).setProgress((int)percent);
					((TextView)myCopyView.findViewById(R.id.one_percent_Text)).setText(percent + " %");
					
					percent = (int)((((float)CopyFileUtils.mhascopyfilecount) / ((float)CopyFileUtils.mallcopyfilecount)) * 100);
					((ProgressBar)myCopyView.findViewById(R.id.all_copy_percent)).setProgress((int)percent);
					((TextView)myCopyView.findViewById(R.id.all_percent_Text)).setText(""+CopyFileUtils.mhascopyfilecount+" / "+CopyFileUtils.mallcopyfilecount);
					LOG(" mhascopyfilecount = " + CopyFileUtils.mhascopyfilecount+", CopyFileUtils.mallcopyfilecount = "+CopyFileUtils.mallcopyfilecount);
				}
				LOG("in mCopyRun, --- --- the mEnableCopy = " + mEnableCopy);
				
				if(CopyFileUtils.mRecoverFile != null){ 
					new AlertDialog.Builder(RockExplorer.this)
                    .setMessage(CopyFileUtils.mRecoverFile + getString(R.string.copy_revocer_text))
                    .setPositiveButton(getString(R.string.copy_revocer_yes),new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface dialog, int which)
                      {
                    	  CopyFileUtils.is_recover = true;
                    	  CopyFileUtils.is_wait_choice_recover = false;
                    	  dialog.dismiss();
                      }
                    }) 
                    .setNegativeButton(getString(R.string.copy_revocer_no),new DialogInterface.OnClickListener()
                    {
                      public void onClick(DialogInterface dialog,int which)
                      {
                    	  CopyFileUtils.is_recover = false;	
                    	  CopyFileUtils.is_wait_choice_recover = false;
                    	  dialog.dismiss();
                      }
                    }).show();
					CopyFileUtils.mRecoverFile = null;
				}
				
				if ((CopyFileUtils.cope_now_sourceFile == null)
						&& (CopyFileUtils.cope_now_targetFile == null)) 
				mCopyHandler.postDelayed(mCopyRun, 10);
				else mCopyHandler.postDelayed(mCopyRun, 500);
				
			}
		}
	};

	private void showCopyDialog(){
		if(mDialogCopy == null){
			LayoutInflater factory=LayoutInflater.from(RockExplorer.this);
			myCopyView=factory.inflate(R.layout.copy_dialog,null);
			mDialogCopy = new Dialog(this, R.style.MyDialog);

			
			int cell_w = AutoSize.getInstance().getScaleWidth(0.4f);
			int cell_h = AutoSize.getInstance().getScaleHeight(0.08f);
			LOG("showCopyDialog cell_w=" + cell_w + "; cell_h="+cell_h);
			LinearLayout.LayoutParams xx2 = new LinearLayout.LayoutParams(cell_w,cell_h);
			LinearLayout lt_source = (LinearLayout)myCopyView.findViewById(R.id.layout_source);
			LinearLayout lt_target = (LinearLayout)myCopyView.findViewById(R.id.layout_target);
			LinearLayout one_percent = (LinearLayout)myCopyView.findViewById(R.id.layout_one_percent);
			LinearLayout all_percent = (LinearLayout)myCopyView.findViewById(R.id.layout_all_percent);
			lt_source.setLayoutParams(xx2);
			lt_target.setLayoutParams(xx2);
			one_percent.setLayoutParams(xx2);
			all_percent.setLayoutParams(xx2);

			((TextView)myCopyView.findViewById(R.id.source_text_title)).setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSize.getInstance().getTextSize(0.03f));
			((TextView)myCopyView.findViewById(R.id.source_Text)).setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSize.getInstance().getTextSize(0.03f));
			((TextView)myCopyView.findViewById(R.id.target_text_title)).setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSize.getInstance().getTextSize(0.03f));
			((TextView)myCopyView.findViewById(R.id.target_Text)).setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoSize.getInstance().getTextSize(0.03f));

			((Button)myCopyView.findViewById(R.id.but_stop_copy)).setOnClickListener(new View.OnClickListener(){
				public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(mDialogCopy != null){
					LOG("-------in the mCopyingRun");
						//CopyFileUtils.is_copy_finish = true;
						CopyFileUtils.is_enable_copy = false;
						UnLockScreen();
					}
				}
			});
			mDialogCopy.setContentView(myCopyView);
			mDialogCopy.show();
		}else{
			mDialogCopy.show();
		}
	}

	private void showGetFileCountDialog()
	{
		GetFileCountDialog = new ProgressDialog(this);
		String msg = this.getResources().getString(R.string.copy_get_file_count);
		GetFileCountDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		GetFileCountDialog.setMessage(msg);
		GetFileCountDialog.setIcon(0);
		GetFileCountDialog.setIndeterminate(false);
		GetFileCountDialog.show();
		GetFileCountDialog.setOnCancelListener(null);
	}
	

	View myCopyView;
	Dialog mDialogCopy;
	ProgressDialog  GetFileCountDialog = null;
	Handler mCopyingHandler = new Handler();	
	Runnable mCopyingRun = new Runnable() {
		public void run() {
			mCopyFileUtils.getCopyFileCount(mSelectedPathList);
			if(GetFileCountDialog != null)
			{
				GetFileCountDialog.dismiss();
				GetFileCountDialog = null;
			}
			mCopyFileUtils.is_copy_finish = false;
			mCopyFileUtils.is_enable_copy = true;
			
			showCopyDialog();
			mCopyHandler.postDelayed(mCopyRun, 10);
		}
	};
	public String fill_path = null;
	Handler mOpenHandler = new Handler();	
	Runnable mOpeningRun = new Runnable() {
		public void run() { 
			Log.d(TAG,"openwiththread = "+openwiththread);
			if(openwiththread){
				mFileControl.refillwithThread(fill_path);
			}
			else
			{
				mFileControl.refill(fill_path);
			}
			mOpenHandler.removeCallbacks(mOpeningRun);
		}
	};
	
	Handler mFillHandler = new Handler();	
	Runnable mFillRun = new Runnable() {
		public void run() { 
			LOG("in the mFillRun, is_finish_fill = " + FileControl.is_finish_fill +" is_enable_fill:"+ FileControl.is_enable_fill);
			LOG("in the mFillRun, adapte size = " + mFileControl.folder_array.size());
			if (!FileControl.is_enable_fill){
				FileControl.is_enable_fill = true;
				return;
			}
			
			if(!FileControl.is_finish_fill)
			{	
				if(openwiththread)
				{
					setFileAdapter(false, false);
				}
				mFillHandler.postDelayed(mFillRun, 1500); 				
			}
			else
			{
				if(openwiththread)
					setFileAdapter(false, false);
				else
				{
                    if(mIsLastDirectory)
					{
                        mIsLastDirectory = false;
                        setFileAdapter(false, false);
                    }
					else
					{
                        setFileAdapter(true, mEnableLeft);
                    }
				}
				mFillHandler.removeCallbacks(mFillRun);
				if(openingDialog != null)
					openingDialog.dismiss();
		    	enableButton(true,true);
		    	
		    	Message msg = new Message();
		    	msg.what = EnumConstent.MSG_DLG_HIDE;
		    	mHandler.sendMessage(msg);
			}			
		}
	};
	/**/
    private static final int MENU_APP_MANAGE = Menu.FIRST + 1;
    public boolean onCreateOptionsMenu(Menu menu) {
                //super.onCreateOptionsMenu(menu);
                //menu.add(0, MENU_APP_MANAGE, 0, R.string.str_menu_app_manage).setAlphabeticShortcut('M');
                return false;
        }
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
        	case MENU_APP_MANAGE:
        		String action="android.intent.action.MAIN";   
        		String category="android.intent.category.DEFAULT"; 
        		String packageName = "com.android.settings";
        		String className = "com.android.settings.ManageApplications";
        		ComponentName cn = new ComponentName(packageName, className);        		
        		Intent tmp = new Intent();
        		tmp.setComponent (cn);
        		tmp .setAction(action);   
        		tmp .addCategory(category);   
        		tmp .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        		startActivity(tmp);   
        		return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public boolean verifyTargetPath(){		
    	if(mSourcePathList.get(0).mFile.getParent().equals(mFileControl.get_currently_path())){
    		Toast.makeText(this, getString(R.string.err_target_same), Toast.LENGTH_SHORT).show();
    		return false;
    	}else{
    		for(int i = 0; i < mSourcePathList.size(); i ++){
    			if(mSourcePathList.get(i).mFile.isDirectory()){
    				if(mFileControl.get_currently_path().startsWith(mSourcePathList.get(i).mFile.getPath() + "/") ||
                                mFileControl.get_currently_path().equals(mSourcePathList.get(i).mFile.getPath()) ){
    					Toast.makeText(this, getString(R.string.err_target_child), Toast.LENGTH_SHORT).show();
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }
    
    public Uri getFileUri(File tmp_file, String tmp_type){
    	String path = tmp_file.getPath();
    	String name = tmp_file.getName();
    	if(tmp_type.equals("image/*")){
	    	ContentResolver resolver = getContentResolver();
	    	String[] audiocols = new String[] {
	    			MediaStore.Images.Media._ID,
	    			MediaStore.Images.Media.DATA,
	    			MediaStore.Images.Media.TITLE
	        };  
	    	LOG("getFileUri() path = " + path);
	    	StringBuilder where = new StringBuilder();
	    	where.append(MediaStore.Images.Media.DATA + "=" + "'" + addspecialchar(path) + "'");
	    	Cursor cur = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	        		audiocols,
	        		where.toString(), null, null);
	    	if(cur != null && cur.moveToFirst()){
	    		int Idx = cur.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
	    		String id = cur.getString(Idx);
	    		return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
	    	}
    	}else if(tmp_type.equals("audio/*")){
    		if(path.endsWith(".3gpp")){
    			ContentResolver resolver = getContentResolver();
    	    	String[] audiocols = new String[] {
    	    			MediaStore.Audio.Media._ID,
    	    			MediaStore.Audio.Media.DATA,
    	    			MediaStore.Audio.Media.TITLE,
    	    			MediaStore.Audio.Media.MIME_TYPE
    	        };
    	    	if (path.startsWith("mnt/")){
    	    		path = "/"+path;
    	    	}
    	    	LOG("getFileUri() path = " + path + " path.lenght:" + path.length() + " trimlength:" + path.trim().length());
    	    	StringBuilder where = new StringBuilder();
    	    	where.append(MediaStore.Audio.Media.DATA + "=" + "'" + path + "'");
    	    	
    	    	Cursor cur = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    	        		audiocols,
    	        		MediaStore.Audio.Media.DATA + "=?", new String[]{path.trim()}, null);
    	    	
    	    	
    	    	if(cur != null && cur.moveToNext()){
    	    		int Idx = cur.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
    	    		String id = cur.getString(Idx);
    	    		
    	    		int dataIdx = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
    	    		String data = cur.getString(dataIdx);
    	    		return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    	    	}
    	    	return null;
    		}
    		ContentResolver resolver = getContentResolver();
	    	String[] audiocols = new String[] {
	    			MediaStore.Audio.Media._ID,
	    			MediaStore.Audio.Media.DATA,
	    			MediaStore.Audio.Media.TITLE
	        };
	    	if (path.startsWith("mnt/")){
	    		path = "/"+path;
	    	}
	    	LOG("getFileUri() path = " + path);
	    	StringBuilder where = new StringBuilder();
	    	where.append(MediaStore.Audio.Media.DATA + "=" + "'" + addspecialchar(path) + "'");
	    	Cursor cur = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	        		audiocols,
	        		where.toString(), null, null);
	    	if(cur != null && cur.moveToFirst()){
	    		int Idx = cur.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
	    		String id = cur.getString(Idx);
	    		return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
	    	}
    	}else if(tmp_type.equals("video/*")){
    		ContentResolver resolver = getContentResolver();
	    	String[] audiocols = new String[] {
	    			MediaStore.Video.Media._ID,
	    			MediaStore.Video.Media.DATA,
	    			MediaStore.Video.Media.TITLE
	        };  
	    	LOG("getFileUri path = " + path);
	    	StringBuilder where = new StringBuilder();
			if(where != null)
			{
		    	where.append(MediaStore.Video.Media.DATA + "=" + "'" + addspecialchar(path) + "'");
		    	Cursor cur = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
		        		audiocols,
		        		where.toString(), null, null);
		    	if(cur != null && cur.moveToFirst()){
		    		int Idx = cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
		    		String id = cur.getString(Idx);
		    		return Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
		    	}
	    	}
    	}
    	return null;
    }

    public String addspecialchar(String mstring){
        String ret = mstring;
        char[] tmpchar = null;
        if(ret.contains("'")){
                int csize = 0;
                int i = 0;
                for(i = 0; i < ret.length(); i ++){
                        if(ret.charAt(i) == '\''){
                                csize ++;
                        }
                }
                int len = ret.length() + (csize * 1);
                tmpchar = new char[len];
                int j = 0;
                for(i = 0; i < ret.length(); i ++){
                        if(ret.charAt(i) == '\''){
                                tmpchar[j] = '\'';
                                j ++;
                        }
                        tmpchar[j] = ret.charAt(i);
                        j ++;
                }
                ret = String.valueOf(tmpchar);
        }
        return ret;
    }
    
    public boolean openDir(String path){
    	LOG("openDir() path=" + path + "; dir="+mCurrentDir);
    	
    	FileControl.is_enable_fill = true;
    	FileControl.is_finish_fill = false;
    	
		if(path == null){
			return false;
		}
		
    	if (path.equals(mDefaultPath)){
    		return false;
    	}
    	
    	
    	if(path.equals(EnumConstent.mDirSmb)){
    		mCifsProxy.getCifsContent(path);
    		return true;
    	}
    	
    	if(path.startsWith(EnumConstent.mDirSmb+"/")||path.startsWith(EnumConstent.mDirSmbMoutPoint)){
			LOG("openDir() ->showWaitDialog()");
			showWaitDialog();
			
			new Thread(new smbThreadRun(mCurListItem, path)).start();
			return false;
		}
    	
    	fill_path = path;
    	mFillHandler.removeCallbacks(mFillRun);	
		
    	File files = new File(path); 
        if((files == null) || !files.exists() || !files.canRead() || (files.list() == null)){
			Toast.makeText(this, resources.getString(R.string.read_error)+"\n"+
					resources.getString(R.string.read_denied), Toast.LENGTH_SHORT).show();

			FileControl.is_enable_fill = true;
			FileControl.is_finish_fill = true;
			
			Message msg = new Message();
	    	msg.what = EnumConstent.MSG_DLG_HIDE;
	    	mHandler.sendMessage(msg);
			return false; 
		}
    	long file_count = files.list().length;
    	LOG("openDir(), file_count=" + file_count);
    	if(file_count > 1500){
    		openwiththread = true;
	    	if(openingDialog == null){
				openingDialog = new ProgressDialog(RockExplorer.this);
				openingDialog.setTitle(R.string.str_openingtitle);
				openingDialog.setMessage(getString(R.string.str_openingcontext));
				openingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
						mFileControl.is_enable_fill = false;
					}
				});
				openingDialog.show();
			}else{
				openingDialog.show();
			}
    	}else{    	
    		openwiththread = false;
    	}
    	mOpenHandler.postDelayed(mOpeningRun, 200);
    	mFillHandler.postDelayed(mFillRun, 300);
    	return true;
    }
    
    public void rescanStorage(String rescanFilePath){    	
    	String scan_path = new String();
        if((sdcard_dir != null) && rescanFilePath.startsWith(sdcard_dir)){
                scan_path = sdcard_dir;
        }else if((usb_dir != null) && rescanFilePath.startsWith(usb_dir)){
                scan_path = usb_dir;//"usb"
//        }else if(rescanFilePath.startsWith(usb_dir1)){
//                scan_path = usb_dir1;//"usb1"
//        }else if(rescanFilePath.startsWith(usb_dir2)){
//                scan_path = usb_dir2;//"usb2"
//        }else if(rescanFilePath.startsWith(usb_dir3)){
//                scan_path = usb_dir3;//"usb3"
//        }else if(rescanFilePath.startsWith(usb_dir4)){
//                scan_path = usb_dir4;//"usb4"
//        }else if(rescanFilePath.startsWith(usb_dir5)){
//                scan_path = usb_dir5;//"usb5"
        }else if((flash_dir != null) && rescanFilePath.startsWith(flash_dir)){
                scan_path = flash_dir;
    	}else{
    		return;
    	}
    	
        LOG("rescanStorage() ->scan_path=" + scan_path);
		LOG("rescanStorage() ->sendBroadcast(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)");
    	Intent intent = new Intent(VolumeInfo.ACTION_VOLUME_STATE_CHANGED,
 			   Uri.parse("file://" +  scan_path));
    	intent.putExtra("read-only", false);
		intent.putExtra("package", "RockExplorer");
    	sendBroadcast(intent);
    }
    

    public void enableButton(boolean enabled) {
		enableButton(enabled, false);
	}

	private void setToolBarText()
	{
		TextView home = (TextView) findViewById(R.id.tool_home_text);
		TextView levelUp = (TextView) findViewById(R.id.tool_levelup_text);
		TextView multi = (TextView) findViewById(R.id.tool_multichoice_text);
		TextView editor = (TextView) findViewById(R.id.tool_editor_text);
		TextView back = (TextView) findViewById(R.id.tool_back_text);
		TextView next = (TextView) findViewById(R.id.tool_next_text);

		home.setTextColor(Color.WHITE);
		levelUp.setTextColor(Color.WHITE);
		multi.setTextColor(Color.WHITE);
		editor.setTextColor(Color.WHITE);
		back.setTextColor(Color.WHITE);
		next.setTextColor(Color.WHITE);

		if(mPitSavePath <= 0)
		{
			home.setTextColor(Color.GRAY);
			levelUp.setTextColor(Color.GRAY);
			back.setTextColor(Color.GRAY);
			multi.setTextColor(Color.GRAY);
			editor.setTextColor(Color.GRAY);
		}
		
		if(mFileControl.currently_path != null){
			if(mFileControl.currently_path.startsWith(smb_dir+"/")){
				multi.setTextColor(Color.GRAY);
				editor.setTextColor(Color.GRAY);
			}
		}

		if(mPitSavePath == (mSavePath.size()-1))
		{
			next.setTextColor(Color.GRAY);
		}
	}
	
	public void enableButton(boolean enabled, boolean levelUpEnabled) {
		if(enabled||(!enabled)){
			LOG("Done Nothing, Return Directly!");
			return ;
		}
		
		View tmp_view = (View) findViewById(R.id.tool_editor);
		tmp_view.setEnabled(enabled);
		tmp_view = (View) findViewById(R.id.tool_multi_choice);
		tmp_view.setEnabled(enabled);
		tmp_view = (View) findViewById(R.id.tool_level_up);
		tmp_view.setEnabled(levelUpEnabled);
		tmp_view = (View) findViewById(R.id.tool_folder_back);
		tmp_view.setEnabled(true);
		tmp_view = (View) findViewById(R.id.tool_folder_next);
		tmp_view.setEnabled(true);
		tmp_view = (View) findViewById(R.id.tool_home);
		tmp_view.setEnabled(true);
		Log.d(TAG,"enableButton, mPitSavePath = "+mPitSavePath);
		if(mPitSavePath <= 0) 
		{
			mPitSavePath = 0;
			tmp_view = (View) findViewById(R.id.tool_level_up);
			tmp_view.setEnabled(false);
			tmp_view = (View) findViewById(R.id.tool_folder_back);
			tmp_view.setEnabled(false);
			tmp_view = (View) findViewById(R.id.tool_home);
			tmp_view.setEnabled(false);
			tmp_view = (View) findViewById(R.id.tool_multi_choice);
			tmp_view.setEnabled(false);
			tmp_view = (View) findViewById(R.id.tool_editor);
			tmp_view.setEnabled(false);
		}
		
		LOG("enablebutton cur_path:"+mFileControl.currently_path);
		if(mFileControl.currently_path != null){
			if(mFileControl.currently_path.startsWith(smb_dir+"/")){
				tmp_view = (View) findViewById(R.id.tool_multi_choice);
				tmp_view.setEnabled(false);
				tmp_view = (View) findViewById(R.id.tool_editor);
				tmp_view.setEnabled(false);
			}
		}
		
		if(mPitSavePath == (mSavePath.size()-1))
		{
			tmp_view = (View) findViewById(R.id.tool_folder_next);
			tmp_view.setEnabled(false);
		}
		setToolBarText();
    }

	private void showCopyFailDialog(int reson)
	{
		Dialog CopyFailDialog = new AlertDialog.Builder(this)
		    .setTitle(R.string.copy_fail)
			.setMessage(reson)
			.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog,int which) 
				{
					dialog.dismiss();
				}
			}).show();
	}

	private void resumeDefaultDevice(){
		Log.d(TAG,"resumeDefaultDevice and openDir(flash_dir");
		if(null!=mDeviceList){
		  mOldPosition = 0;
		  mLastDirectory = null;
		  //mDeviceList.forceLayout();
		  mDeviceList.setSelection(mOldPosition);
		  //mDeviceList.requestFocus();
		}
		if((0==mOldPosition)&&(null==mLastDirectory)){
			if(openDir(flash_dir)){
				enableButton(true,true);
			}			
		}
	}
	
	
	public Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case EnumConstent.MSG_ANI_ZOOM_IN:
					View view = (View) msg.obj;
					Animation ani = new ZoomAnimation(view, 1, 1.1f, 0, 100);
					view.startAnimation(ani);
					break;
				case EnumConstent.MSG_MOUNT_CHANGE:
					Log.d(TAG,"Handler->EnumConstent.MSG_MOUNT_CHANGE");
					changeDeviceStatus(msg);
					mDeviceAdapter.notifyDataSetChanged();
					mDeviceList.invalidate();
					sdcard_dir = StorageUtils.getSDcardDir(mStorageManager);
					usb_dir = StorageUtils.getUsbDir(mStorageManager);
					//重新获取所有SD卡路径
					mSDCardPaths = StorageUtils.getSdCardPaths(mStorageManager);
					//重新获取所有USB路径
					mUsbPaths = StorageUtils.getUsbPaths(mStorageManager);
					//if any external storage is removed, resume default internal storage
					mStorageType = "flash";
					FileControl.setStorageType(mStorageType);
					rebuildDeviceList();
					resumeDefaultDevice();
					/*if(msg.arg1 == EnumConstent.MSG_REMOVED){
						Log.d(TAG,"External Storeage is EnumConstent.MSG_REMOVED");
						resumeDefaultDevice();
					}*/
					break;
				case EnumConstent.MSG_NETWORK_CHANGE:
					Log.d(TAG,"Handler->EnumConstent.MSG_NETWORK_CHANGE");
					boolean hasNetWork = (msg.arg1==1);
					changeNetWorkStatus(hasNetWork);
					mDeviceAdapter.notifyDataSetChanged();
					mDeviceList.invalidate();
					break;
				case EnumConstent.MSG_OP_STOP_COPY:
					boolean disconnect = (msg.arg1==0);
					String mSource = mCopyFileUtils.getSourcePath();
					String mTarget = mCopyFileUtils.getTargetPath();
					if( disconnect && 
						(( mSource != null) && (mSource.startsWith(EnumConstent.mDirSmbMoutPoint))) ||
						(( mTarget != null) && (mTarget.startsWith(EnumConstent.mDirSmbMoutPoint))))
					{
						StopCopy();
						showCopyFailDialog(R.string.copy_interrupt);
					}
					
					Log.d(TAG,"mHandler,EnumConstent.MSG_OP_STOP_COPY = "+msg.arg1);
					if(msg.arg1 == CopyFileUtils.NO_SPACE)
					{
						StopCopy();
						mCopyFileUtils.mCopyResult = CopyFileUtils.COPY_OK;
						showCopyFailDialog(R.string.full);
					}
					break;

				case EnumConstent.MSG_DLG_COUNT:
					showGetFileCountDialog();
					mCopyingHandler.postDelayed(mCopyingRun, 10);
					break;
				case EnumConstent.MSG_DLG_SHOW:
					showWaitDialog();
					break;
				case EnumConstent.MSG_DLG_HIDE:
					if (mWaitDialog != null){
	    				LOG("wait dialog dismiss.....");
	    				mWaitDialog.dismiss();
	    			}
					break;
					
				case EnumConstent.MSG_DLG_LOGIN_FAIL:
					FileInfo smbinfo = (FileInfo) msg.obj;
					mCifsProxy.showLoginFailDialog(smbinfo);
					break;
					
				case EnumConstent.MSG_CLEAR_CONTENT:
					clearContentList();
					mSavePath.clear();            
					mSavePath.add(new Path(mDefaultPath,""));	
					mPitSavePath = mSavePath.size()-1;	
					break;
					
				case EnumConstent.MSG_OP_START_COPY:
					if(mEnableCopyInBg)
						copyInBackgournd();
					else
						copyInForeground();
					break;
			}
		}
	};

	private void StopCopy()
	{
		CopyFileUtils.is_enable_copy = false;
		CopyFileUtils.is_copy_finish = true;
		FileControl.is_enable_del = false;
		FileControl.is_enable_fill = false;
		
		mCopyFileUtils.setSourcePath("");
		mCopyFileUtils.setTargetPath("");
				
		if(mDialogCopy != null)
		{
			mDialogCopy.dismiss();
			mDialogCopy = null;
		}
	}
	
	private void changeNetWorkStatus(boolean connect)
	{
		Device device = getDevice("smb");
		if(device != null){
			device.setMountStatus(connect || isWifiApEnabled());
		}

		String currentPath = mFileControl.get_currently_path();
		if ((device !=null) && ((currentPath != null) && 
				(currentPath.startsWith(smb_dir) || currentPath.startsWith(smb_mountPoint))))
		{
			if (!device.IsMount()){
				clearContentList();
				mSavePath.clear();            
				mSavePath.add(new Path(mDefaultPath,""));	
				mPitSavePath = mSavePath.size()-1;
				enableButton(true, true);
			}
		}
		
		if (!device.IsMount()){
			mCifsProxy.clearSmbcache();
		}
	}
	private void deleteDiretion(String diretion)
	{
		if(diretion == null)
			return ;

		int count = mSavePath.size()-1;
		String dir = null;
		Log.d(TAG,"deleteDiretion,mPitSavePath = "+mPitSavePath);

		for(int i = count; i > 0; i --)
		{
			dir = mSavePath.get(i).getPathDirection();
			Log.d(TAG,"dir = "+dir+", path = "+mSavePath.get(i).getPath());
			if((dir != null) && (dir.equals(diretion)))
			{
				mSavePath.remove(i);
				if(i <= mPitSavePath)
				{
					mPitSavePath --;
					Log.d(TAG,"deleteDiretion,mPitSavePath = "+mPitSavePath);
				}
			}
		}
		
		count = mSavePath.size()-1;
		Path device0 = null;
		Path device1 = null;
		for(int i = count; i > 0; i --)
		{
			 device0 = mSavePath.get(i);
			 device1 = mSavePath.get(i-1);
			if((device0 != null) && (device1 != null) && 
				(device0.getPath().equals(device1.getPath())) &&
				(device0.getPathDirection().equals(device0.getPathDirection())))
			{
				mSavePath.remove(i);
				mPitSavePath --;
			}
		}
		
		if(mPitSavePath < 0)
			mPitSavePath = 0;
		
		if((mCurrentDir != null) && (mCurrentDir.equals(diretion)))
		{
			mContentList.setAdapter(null);
			mContentList.setBackgroundDrawable(null);
			mContentList.invalidate();
			mPitSavePath = 0;
		}
		enableButton(true,true);
	}

	private void deleteDiretion(String diretion,String mountPoint)
	{
		if(diretion == null || mountPoint == null)
			return ;
		if(mountPoint.startsWith("/")){
			Log.d(TAG,"##deleteDiretion! ignore '/' in mountPoint string..");
			mountPoint = mountPoint.substring(1);
		}
		int count = mSavePath.size()-1;
		String dir = null;
		String path = null;
		Log.d(TAG,"deleteDiretion,mPitSavePath = "+mPitSavePath);
		for(int i = count; i > 0; i --)
		{
			dir = mSavePath.get(i).getPathDirection();
			path = mSavePath.get(i).getPath();
			Log.d(TAG,"device = "+dir+", path = "+path);
			if(path.startsWith("/")){
				Log.d(TAG,"##deleteDiretion! ignore '/' in path string..");
				path = path.substring(1);
			}
			if(((dir != null) && (dir.equals(diretion))) && ((path!=null)&&(path.startsWith(mountPoint))))
			{
				mSavePath.remove(i);
				if(i <= mPitSavePath)
				{
					mPitSavePath --;
					Log.d(TAG,"deleteDiretion,mPitSavePath = "+mPitSavePath);
				}
			}
		}
		
		count = mSavePath.size()-1;
		Path device0 = null;
		Path device1 = null;
		for(int i = count; i > 0; i --)
		{
			 device0 = mSavePath.get(i);
			 device1 = mSavePath.get(i-1);
			if((device0 != null) && (device1 != null) && 
				(device0.getPath().equals(device1.getPath())) &&
				(device0.getPathDirection().equals(device0.getPathDirection())))
			{
				mSavePath.remove(i);
				mPitSavePath --;
			}
		}
		
		if(mPitSavePath < 0)
			mPitSavePath = 0;
		String currentPath = mFileControl.currently_path;
		if(currentPath.startsWith("/")){
			Log.d(TAG,"##deleteDiretion! ignore '/' in currentPath string..");
			currentPath = currentPath.substring(1);
		}

		Log.d(TAG,"delete, curDir:"+mCurrentDir+"-->deviceDir:"+diretion+" -- curPath:"+currentPath+"-->mountPoint:"+mountPoint+" ..");
		if(((mCurrentDir != null) && (mCurrentDir.equals(diretion)))
			&& ((currentPath!=null)&&(currentPath.startsWith(mountPoint))))
		{
			mContentList.setAdapter(null);
			mContentList.setBackgroundDrawable(null);
			mContentList.invalidate();
			mPitSavePath = 0;
		}
		enableButton(true,true);
	}

	private void changeDeviceStatus(Message msg)
	{
		String path = (String)msg.obj;
		if(path == null)
		{
			changeDeviceStatus();
		}
		else
		{
			boolean mount = (msg.arg1 == 0);
			Device device = null;
			if(path.equals(flash_dir))
			{mount = isMountFLASH();
				device = getDevice("flash");
				if(device != null)
				{
					device.setMountStatus(mount);
				}

				if(!mount)
				{
					deleteDiretion("flash");
				}
				else
				{
				}
			}
			else if(path.equals(sdcard_dir))
			{mount = isMountSD();
				device = getDevice("sdCard");
				if(device != null)// && (isMountSD()))
				{
					device.setMountStatus(mount);
				}

				if(!mount)
				{
					deleteDiretion("sdCard");
				}
			}
/*			else if(path.equals(EnumConstent.mDirSata))
			{mount = isSataMount();
				device = getDevice("sata");
				if(device != null)
				{
					device.setMountStatus(mount);
				}
				if(!mount)
				{
					deleteDiretion("sata");
				}

			}*/
			else if(path.equals(usb_dir))
			{mount = isMountUSB();
				device = getDevice("usb");
				if(device != null)
				{
					device.setMountStatus(mount);
				}

				if(!mount)
				{
					deleteDiretion("usb");
				}
				else if(mFileControl.currently_path != null)
				{
				    String usbRootPath = usb_dir;//Environment.getHostStorageDirectory().getPath();
					if(usbRootPath.startsWith("/")){
						Log.d(TAG,"##! ignore '/' in usbRootPath string..");
						usbRootPath = usbRootPath.substring(1);
					}
					String currentPath = mFileControl.currently_path;
					if(currentPath.startsWith("/")){
						Log.d(TAG,"##! ignore '/' in currentPath string..");
						currentPath = currentPath.substring(1);
					}
				    if((currentPath).equals(usbRootPath)){
						Log.d(TAG,"current dir is usb root dir, refresh list only..");
						clearContentList();
						openDir(mFileControl.currently_path);
				    }else{
					    mount = true;
					    if(path.equals(usb_dir)){
							Log.d(TAG,"current change device is --- usb0 ---");
							mount = isMountUSB();
//					    }else if(path.equals(usb_dir1)){
//							Log.d(TAG,"current change device is --- usb1 ---");
//					        mount = isMountUSB1();
//					    }else if(path.equals(usb_dir2)){
//							Log.d(TAG,"current change device is --- usb2 ---");
//					        mount = isMountUSB2();
//					    }else if(path.equals(usb_dir3)){
//							Log.d(TAG,"current change device is --- usb3 ---");
//					        mount = isMountUSB3();
//					    }else if(path.equals(usb_dir4)){
//							Log.d(TAG,"current change device is --- usb4 ---");
//					        mount = isMountUSB4();
//					    }else if(path.equals(usb_dir5)){
//							Log.d(TAG,"current change device is --- usb5 ---");
//					        mount = isMountUSB5();
					    }
						if(!mount){
							deleteDiretion("usb",path);
						}
				    }
				}
			}
		}
	}
	
	private void changeDeviceStatus(){
		boolean flashMount = isMountFLASH();
		Device device = getDevice("flash");
		if(device != null){
			device.setMountStatus(flashMount);
		}
		
		boolean sdCardMount = isMountSD();
		device = getDevice("sdCard");
		if(device != null){
			device.setMountStatus(sdCardMount);
		}

//		boolean sataMount = isSataMount();
//		device = getDevice("sata");
//		if(device != null)
//		{
//			device.setMountStatus(sataMount);
//		}

		boolean usbMount = isMountUSB();
		device = getDevice("usb");
		if(device != null){
			Log.d(TAG,"setusb mount status");
			device.setMountStatus(usbMount);
		}

		String dir = null;
		int count = mSavePath.size()-1;
		for(int i = count; i > 0; i --){
			dir = mSavePath.get(i).getPathDirection();
			if(dir != null){
				if(((!flashMount) && (dir.equals("flash"))) ||((!sdCardMount) && (dir.equals("sdCard"))) ||
//				   ((!sataMount) && (dir.equals("sata"))) ||
				   ((!usbMount) && (dir.equals("usb")))){
					Log.d(TAG,"remove path = "+mSavePath.get(i).getPath());
					mSavePath.remove(i);
					if(i <= mPitSavePath)
						mPitSavePath --;
				}
			}
		}

		count = mSavePath.size()-1;
		Path device0 = null;
		Path device1 = null;
		for(int i = count; i > 0; i --){
			 device0 = mSavePath.get(i);
			 device1 = mSavePath.get(i-1);
			if((device0 != null) && (device1 != null) && 
				(device0.getPath().equals(device1.getPath())) &&
				(device0.getPathDirection().equals(device1.getPathDirection())))
			{
				mSavePath.remove(i);
				mPitSavePath --;
			}
		}
		
		if(mPitSavePath < 0)
			mPitSavePath = 0;

		if((!flashMount && (mCurrentDir != null) && mCurrentDir.equals("flash")) ||
			(!sdCardMount && (mCurrentDir != null) && mCurrentDir.equals("sdCard")) ||
//			(!sataMount && (mCurrentDir != null) && mCurrentDir.equals("sata")) ||
			(!usbMount && (mCurrentDir != null) && mCurrentDir.equals("usb")))
		{
			Log.d(TAG,"changeDeviceStatus, device is delete, mPitSavePath = 0");
			mContentList.setAdapter(null);
			mContentList.setBackgroundDrawable(null);
			mContentList.invalidate();
			mPitSavePath = 0;
		}
		enableButton(true,true);
	}
	
    /*
     * ½ÓÊÕusb/sdcard umountÏûÏ¢
     * */
	
    private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
//      public void onStorageStateChanged(String path, String oldState, String newState) {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) { 
            Log.i(TAG, "Received storage state changed notification that " + vol.path +
                     " changed state from " + oldState +
                    " to " + newState);
            Message msg = new Message();
            msg.what = EnumConstent.MSG_MOUNT_CHANGE;
            msg.obj = vol.path;
            //usb_dir = StorageUtils.getUsbDir(mStorageManager);
           /* if ((vol.path != null) && (usb_dir!=null) && vol.path.startsWith(usb_dir))
                msg.obj = usb_dir;*/
            
            if ((newState == VolumeInfo.STATE_EJECTING)
                    || (newState == VolumeInfo.STATE_UNMOUNTED)
                    || (newState == VolumeInfo.STATE_REMOVED)
                    || (newState == VolumeInfo.STATE_BAD_REMOVAL))
            {
                msg.arg1 = EnumConstent.MSG_REMOVED;
                mHandler.sendMessage(msg);
            }
            else if (newState == VolumeInfo.STATE_MOUNTED)
            {
                msg.arg1 = EnumConstent.MSG_MOUNTED;
                mHandler.sendMessage(msg);
            }
        }
    };
	
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
		public void onReceive(Context context, Intent intent) {
			LOG("mScanListener BroadcastReceiver action" + intent.getAction());

			Uri uri = intent.getData();
			String path = null;
			if (uri != null) {
				path = uri.getPath();
			}
			if ((path != null)
					&& mFileControl.get_currently_path().equals(path)) {
				mFileControl.refill(path);
				setFileAdapter(false, false);
			}

			if (mEnableMove) {
				mEnableMove = false;
				mEnableCopy = false;
			}

			//rebuildDeviceList();
		}
    };
    
    

    public void registBroadcastRec(){
        IntentFilter f = new IntentFilter();
        //f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        //f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
//        f.addAction(Intent.ACTION_MEDIA_REMOVED);
//        f.addAction(Intent.ACTION_MEDIA_EJECT);
//        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
//		f.addAction(Intent.ACTION_MEDIA_MOUNTED);
		mCifsProxy = new CifsExplorerProxy(this);
		mCifsProxy.onCreate();

		f.addAction("com.rockchip.tv.reFillFile");
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);      
    }
    
    /*
     * ÓÃÓÚÅÐ¶Ïµ±Ç°ÊÇ·ñÓÐmount SD¿¨
     * */
    public boolean isMountSD(){
    	
//    	String status = StorageUtils.getSDcardState();
    	if (StorageUtils.isMountSD(mStorageManager)) {
    		return true;
    	}
    	return false; 	
    }
    
    /*
     * ÓÃÓÚÅÐ¶Ïµ±Ç°ÊÇ·ñÓÐmount USB
     * */
    public static final String MEDIA_REMOVED = "removed";
    public boolean isMountUSB(){
    	
//    	String status = SystemProperties.get("USB1_STORAGE_STATE", MEDIA_REMOVED);
//	String status = Environment.getHostStorage_Extern_0_State();    	
//    	LOG(" -------- the usb1     status = " + status);
    	if (StorageUtils.isMountUSB(mStorageManager)) {
    		return true;
    	}
    	return false;
    	
/*    	return true;*/
    }
	
    public boolean isMountFLASH(){
        String status = StorageUtils.getFlashState();
        LOG("Storage State ----> Flash=" + status);
        if ((status != null) && status.equals(Environment.MEDIA_MOUNTED)) {
                return true;
        }
        return false;
    }
    
//	public boolean isSataMount()
//	{
//		String status = Environment.getInterHardDiskStorageState();
//	        LOG(" -------- the sata status = " + status);
//	        if ((status != null) && status.equals(Environment.MEDIA_MOUNTED)) {
//	                return true;
//	        }
//	        return false;
//	}

	public boolean isNetworkAvailable() 
	{
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} 
		else 
		{
			NetworkInfo info = connectivity.getActiveNetworkInfo();     
			if (info == null || !info.isConnected()) 
			{
				return false;
			}
			return true;
		}
	}
	
	public boolean isWifiApEnabled(){
		WifiManager wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifimanager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED){
			return true;
		}
		return false;
	}

    public boolean isMultiMediaFile(FileInfo fileInfo){
    	if(fileInfo != null && !fileInfo.mIsDir){
	    	if(fileInfo.mFileType.equals("audio/*")
	    		|| fileInfo.mFileType.equals("video/*")
	    		|| fileInfo.mFileType.equals("image/*") )
	    		return true;
    	}
    	if(fileInfo != null && fileInfo.mIsDir){
    		return true;
    	}
    	return false;
    }
    
    public void showWaitDialog(){
    	LOG("show wait dialog.......");

		mWaitDialog = new ProgressDialog(RockExplorer.this);
		mWaitDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		mWaitDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				mFileControl.is_enable_fill = false;
				dialog.dismiss();
			}
			
		});
		mWaitDialog.setMessage(resources.getString(R.string.openning));
		mWaitDialog.setCancelable(false);
		
		mWaitDialog.show();
    	
    }
    
    public void showToast(String str){
    	final String msg = str;
    	mHandler.post(new Runnable(){
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(RockExplorer.this, msg, Toast.LENGTH_LONG).show();
			}
		});

    }
    
    public class smbfileThreadRun implements Runnable{
    	private FileInfo mListItem = null;
    	private int position = 0;
    	
    	public smbfileThreadRun(FileInfo mListItem, int position) {
			this.mListItem = mListItem;
			this.position = position;
		}
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String smbpath = parseMountDirToSmbpath(mCurrnetSmb);
			if(!cifsIsMountAndConnect(smbpath)){
				Message msg = new Message();
		    	msg.what = EnumConstent.MSG_DLG_HIDE;
		    	mHandler.sendMessage(msg);
			}else {
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (mWaitDialog!=null){
							LOG("wait dialog dismiss.....");
							mWaitDialog.dismiss();
						}
						mCifsProxy.openSmbfile(mListItem, position);
					}
				});
			}
			
	    	return;
		}
    	
    }
    
    public class smbThreadRun implements Runnable
    {
    	private FileInfo mListItem = null;
    	private String mSmbPath = null;
		/**
		 * @param mListItem
		 * @param mSmbPath
		 */
		public smbThreadRun(FileInfo mListItem, String mSmbPath) {
			this.mListItem = mListItem;
			this.mSmbPath = mSmbPath;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String path = mSmbPath;
			if (isItemClick){
				path = mCifsProxy.getMountPoint(mListItem,mSmbPath);
			}
					
			LOG("open smb path:"+path);
			if (path == null) {
				//mount failed
				Message msg = new Message();
		    	msg.what = EnumConstent.MSG_DLG_HIDE;
		    	mHandler.sendMessage(msg);
		    	
		    	if (isItemClick){
		    		if (mListItem.mFile.getPath().startsWith(smb_dir+"/")){
		    			msg = new Message();
						msg.what = EnumConstent.MSG_DLG_LOGIN_FAIL;
						msg.obj = mListItem;
						mHandler.sendMessage(msg);
					}
		    	}
			
				return;
			}else if (path.startsWith(smb_mountPoint)){
				
				if (mListItem.mFile.getPath().startsWith(smb_dir+"/")){
						// path is like "SMB/IP/sharefile"
					mCurrnetSmb = mListItem.mFile.getPath();
					mCurSmbAnonymous = mListItem.isMAnonymous();
					mCurSmbUsername = mListItem.getMUsername();
					mCurSmbPassword = mListItem.getMPassword();						
				}
				String curpath = mFileControl.get_currently_path();
				LOG("curpath:"+curpath+" fill path:"+path);
				if (!curpath.startsWith(smb_mountPoint)){
					FileInfo smbinfo = mCifsProxy.getSmbinfoMap(path.substring(0,24));
					if (smbinfo !=null){
						mCurrnetSmb = smbinfo.mFile.getPath();
						mCurSmbAnonymous = smbinfo.isMAnonymous();
						mCurSmbUsername = smbinfo.getMUsername();
						mCurSmbPassword = smbinfo.getMPassword();
					}
				}else if (!curpath.substring(0,24).equals(path.substring(0,24))){
					FileInfo smbinfo = mCifsProxy.getSmbinfoMap(path.substring(0,24));
					if (smbinfo !=null){
						mCurrnetSmb = smbinfo.mFile.getPath();
						mCurSmbAnonymous = smbinfo.isMAnonymous();
						mCurSmbUsername = smbinfo.getMUsername();
						mCurSmbPassword = smbinfo.getMPassword();
					}
				}
				
				LOG("Current smb******:"+mCurrnetSmb+" isAnony:"+mCurSmbAnonymous+" user:"+mCurSmbUsername
						+" pass:"+mCurSmbPassword);
				
				
				if (mCifsProxy.openSmbDir(path)){
					//open mount point dir
					if (isItemClick){
						if (!path.equals(mSavePath.get(mSavePath.size()-1).getPath())
	    						|| !mCurrentDir.equals(mSavePath.get(mSavePath.size()-1).getPathDirection())){
							mSavePath.add(new Path(path,mCurrentDir));
		    				mPitSavePath = mSavePath.size() - 1;
						}else{
	    					mPitSavePath = mSavePath.size() - 1;
	    				}
						
					}
					
				}else if (!isItemClick){
					Message msg = new Message();
					msg.what = EnumConstent.MSG_CLEAR_CONTENT;
					mHandler.sendMessage(msg);
				}
				
			}else if(path.startsWith(smb_dir+"/")){
				// path is like "SMB/IP"
				if(mCifsProxy.getCifsChildContent(path)){
	    			Message msg = new Message();
			    	msg.what = EnumConstent.MSG_DLG_HIDE;
			    	mHandler.sendMessage(msg);
			    	
			    	if (isItemClick){
			    		if (!path.equals(mSavePath.get(mSavePath.size()-1).getPath())
	    						|| !mCurrentDir.equals(mSavePath.get(mSavePath.size()-1).getPathDirection())){
			    			mSavePath.add(new Path(path,mCurrentDir));
		    				mPitSavePath = mSavePath.size() - 1;
			    		}else{
	    					mPitSavePath = mSavePath.size() - 1;
	    				}
					}
			    	
	    			return;
	    		}else{
	    			Message msg = new Message();
			    	msg.what = EnumConstent.MSG_DLG_HIDE;
			    	mHandler.sendMessage(msg);
			    	
			    	if(isItemClick){
			    		FileInfo smbinfo = mCifsProxy.getSmbinfoFromSmbPath(path);
			    		msg = new Message();
			    		msg.what = EnumConstent.MSG_DLG_LOGIN_FAIL;
			    		msg.obj = smbinfo;
			    		mHandler.sendMessage(msg);
			    	}else{
			    		msg = new Message();
			    		msg.what = EnumConstent.MSG_CLEAR_CONTENT;
						mHandler.sendMessage(msg);
			    	}
	    			return;
	    		}
			}
		}    	
    }
    
    
}
