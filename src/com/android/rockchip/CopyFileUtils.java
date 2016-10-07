package com.android.rockchip;

import java.io.*; 
import java.util.ArrayList;

import android.util.Log;
import android.util.TypedValue; 
import android.widget.TextView;

import android.os.Environment;
import android.os.StatFs;
import java.lang.IllegalArgumentException;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;

public class CopyFileUtils { 
	final String TAG = "CopyFileUtils.java";
	final boolean DEBUG = true;
	private void LOG(String str)
	{
		if(DEBUG)
		{
			Log.d(TAG,str);
		}
	}

	public String flash_dir = StorageUtils.getFlashDir();
	public String sdcard_dir;
	public String usb_dir;
//  public String usb_dir0 = Environment.getHostStorage_Extern_0_Directory().getPath();
//	public String usb_dir1 = Environment.getHostStorage_Extern_1_Directory().getPath();
//	public String usb_dir2 = Environment.getHostStorage_Extern_2_Directory().getPath();
//	public String usb_dir3 = Environment.getHostStorage_Extern_3_Directory().getPath();
//	public String usb_dir4 = Environment.getHostStorage_Extern_4_Directory().getPath();
//	public String usb_dir5 = Environment.getHostStorage_Extern_5_Directory().getPath();
//	public String sata_dir =  Environment.getInterHardDiskStorageDirectory().getPath();
	public String smb_mountPoint = "/data/smb";
	
	private final int BUFFERE_SIZE = 1024*8;

	static boolean pathError = false; 
	static boolean is_copy_finish = false;	
	static boolean is_enable_copy = true;
	static boolean is_same_path = false;
	static boolean is_not_free_space = false;
	static File mInterruptFile = null;
	
	private ArrayList<FileInfo> has_copy_path = null;
	
	public TextView source_text = null;
	public TextView target_text = null;
	
	static File cope_now_sourceFile = null;
	static File cope_now_targetFile = null;
	static int mhascopyfilecount = 0;
	static int mallcopyfilecount = 0;
	private String mSource = null;
	private String mTarget = null;
	

	static boolean is_wait_choice_recover = true;
	static boolean is_recover = true;
	static File mRecoverFile = null;
	
	static FileControl mFileControl = null;
	public static long mHasCopytargetFileSize = 0;

	public static int SAME_PATH = -1000;
	public static int NO_SPACE = SAME_PATH-1;
	public static int COPY_OK = 0;
	public int mCopyResult = COPY_OK;

	private Handler mEventHandler = null;
    private StorageManager mStorageManager = null;

    public CopyFileUtils(StorageManager storageManager) {
        mStorageManager = storageManager;
        sdcard_dir = StorageUtils.getSDcardDir(storageManager);
        usb_dir = StorageUtils.getUsbDir(storageManager);
    }

	public String getSourcePath()
	{
		return mSource;
	}

	public String getTargetPath()
	{
		return mTarget;
	}
	
	public void setSourcePath(String path)
	{
		mSource = path;
	}

	public void setTargetPath(String path)
	{
		mTarget = path;
	}

	public void setEventHandler(Handler handler)
	{
		mEventHandler = handler;
	}

	public void getCopyFileCount(ArrayList<FileInfo> mSelectedPathList)
	{
		mallcopyfilecount = getfilenum(mSelectedPathList);
	}
	
    public int CopyFile(File sourceFile,File targetFile)  throws IOException
    { 
    	if(sourceFile.getPath().equals(targetFile.getPath())){
    		is_same_path = true;
			if(mEventHandler != null)
			{
				Message msg = new Message();
				msg.what = EnumConstent.MSG_OP_STOP_COPY;
				msg.arg1 = SAME_PATH;
				mEventHandler.sendMessage(msg);
			}
    		return SAME_PATH;
    	}
		
		mSource = sourceFile.getPath();
		mTarget = targetFile.getPath();
		
        LOG(" -CopyFile  cope_now_sourceFile = " + sourceFile.toString() + "    cope_now_targetFile = " + targetFile.toString());
		if(!ishavefreespace(sourceFile, targetFile)){
    		is_not_free_space = true;
    		is_enable_copy = false;
			Log.d(TAG,"CopyFile,no space to copy file");
			if(mEventHandler != null)
			{
				Message msg = new Message();
				msg.what = EnumConstent.MSG_OP_STOP_COPY;
				msg.arg1 = NO_SPACE;
				mEventHandler.sendMessage(msg);
			}
    		return NO_SPACE;
    	}
    	is_recover = true;
    	mRecoverFile = null;
    	if(targetFile.exists()){
    		mRecoverFile = targetFile;
    		is_wait_choice_recover = true;
    		while(is_wait_choice_recover){
    			;
    		}
    	}
    	
    	cope_now_sourceFile = sourceFile;
    	cope_now_targetFile = targetFile;
        
        mHasCopytargetFileSize = 0;
        if(is_recover){
            FileInputStream input = new FileInputStream(sourceFile); 
            BufferedInputStream inBuff=new BufferedInputStream(input); 

            FileOutputStream output = new FileOutputStream(targetFile); 
            BufferedOutputStream outBuff=new BufferedOutputStream(output); 
            
	        byte[] b = new byte[BUFFERE_SIZE]; 
	        int len; 
	        while ((len =inBuff.read(b)) != -1) 
	        { 
	        	if(is_enable_copy){
	        		mHasCopytargetFileSize +=len;
	        		outBuff.write(b, 0, len);
	        	}else{    		
	        		break;
	        	}
	        } 
	        outBuff.flush(); 
	         
	        inBuff.close(); 
	        outBuff.close(); 
	        output.close(); 
	        input.close();
    	}        
        if(is_enable_copy)
        	mhascopyfilecount ++;      
		return COPY_OK;
    }
    
    public int CopyDirectiory(String sourceDir, String targetDir) throws IOException 
    {    
    	LOG(" -CopyDirectiory  sourceDir = " + sourceDir.toString() + ",targetDir = " + targetDir.toString());
		is_not_free_space = false; 
    	if(sourceDir.equals(targetDir)){
    		is_same_path = true;
    		return SAME_PATH;
    	}
    	if(!(new File(targetDir)).exists())
    	{      
    		(new File(targetDir)).mkdirs(); 
    	}

        File[] file = (new File(sourceDir)).listFiles();
		Log.d(TAG,"file length = "+file.length);
        for (int i = 0; i < file.length; i++) 
        { 
            if (file[i].isFile()) 
            { 
                File sourceFile=file[i]; 
                File targetFile=new File(new File(targetDir).getAbsolutePath()+File.separator+file[i].getName());
                int result = CopyFile(sourceFile,targetFile);
				if(COPY_OK != result)
				{
					mInterruptFile = targetFile;
					return result;
				}
            } 
            if(!is_enable_copy){
            	break;
            }
            if (file[i].isDirectory()) 
            { 
                String dir1=sourceDir + "/" + file[i].getName(); 
                String dir2=targetDir + "/"+ file[i].getName(); 
                int result = CopyDirectiory(dir1, dir2); 
				if(COPY_OK != result)
					return result;
            } 
        } 

		return COPY_OK;
    } 
    
    public void CopyFileInfoArray(final ArrayList<FileInfo> multi_path, final String targetDir){
    	new Thread(){
    		public void run(){
    			is_copy_finish = false;
    			is_enable_copy = true;
    			has_copy_path = new ArrayList<FileInfo>();
    			mhascopyfilecount = 1;	
    			mallcopyfilecount = getfilenum(multi_path);
    			Log.d(TAG,"CopyFileInfoArray, all need to copy = "+mallcopyfilecount);
    	    	for(int i = 0; i < multi_path.size(); i ++)
				{
    	    		if(multi_path.get(i).mFile.isDirectory())
					{
    	    			try
						{
    	    				mCopyResult = CopyDirectiory(multi_path.get(i).mFile.getPath(), 
    	    						new File(targetDir).getAbsolutePath()+File.separator+multi_path.get(i).mFile.getName());
							if(COPY_OK != mCopyResult)
								return;
    	    			}
						catch(IOException e)
						{
    	    				Log.e(TAG,"CopyDirectiory error!");
    	    			}    			
    	    		}
					else
					{
    	    			try
						{
    	    				File targetFile=new File(new File(targetDir).getAbsolutePath()+File.separator+multi_path.get(i).mFile.getName()); 
    	    	            mCopyResult = CopyFile(multi_path.get(i).mFile, targetFile);
    	    	            if(!is_enable_copy)
							{	
    	    	    			mInterruptFile = targetFile;
    	    	    			LOG("CopyFileInfoArray---mFile- mInterruptFile = " + mInterruptFile.getPath());
    	    	    		}
							if(COPY_OK != mCopyResult)
								return;
    	    			}
						catch(IOException e)
						{
    	    				Log.e(TAG,"CopyDirectiory error!");
    	    			} 
    	    		}
    	    		LOG("CopyFileInfoArray----" + multi_path.get(i).mFile.getPath());
    	    		LOG("CopyFileInfoArray---- is_enable_copy = " + is_enable_copy);
					
    	    		if(is_enable_copy)
					{
    	    			has_copy_path.add(multi_path.get(i));
    	    		}
					else
					{
    	    			break;
    	    		}
    	    	}
    	    	if(is_enable_copy){
    	    		mInterruptFile = null;
    	    	}else{
    	    		mFileControl.deleteFile(mInterruptFile);
    	    	}
    	    	is_copy_finish = true;
    		}
    	}.start();
    }
    
    public ArrayList<FileInfo> get_has_copy_path(){
    	return has_copy_path;
    }
    

    public int getfilenum(ArrayList<FileInfo> multi_path){
    	if(multi_path == null)
		{
			Log.d(TAG,"getfilenum, multi_path = null");
			return 0;
		}
		
    	int num = 0;
		Log.d(TAG,"getfilenum, multi_path size = "+multi_path.size());
    	for(int i = 0; i < multi_path.size(); i ++){
    		if(multi_path.get(i).mFile.isDirectory()){
				Log.d(TAG,"multi_path.get("+i+").mFile = " +multi_path.get(i).mFile.toString());
    			num = num + getDirfilenum(multi_path.get(i).mFile);
    		}else{
    			num ++;
    		}
    	}

		Log.d(TAG,"getfilenum, need copy number = "+num);
    	return num;
    }

    public int getDirfilenum(File dir){
    	Log.d(TAG,"getDirfilenum,dir = "+dir.toString());
    	if(dir == null)
			return 0;
		
    	File[] file = dir.listFiles();
		if(file == null)
			return 0;
		
    	int tmp_num = 0;
		Log.d(TAG,"getDirfilenum,file = "+file.length);
    	for (int i = 0; i < file.length; i++) {
    		if (file[i].isFile()) 
    			tmp_num ++;
    		else
    			tmp_num = tmp_num + getDirfilenum(file[i]);
    	}
    	return tmp_num;
    }
    public boolean ishavefreespace(File sourcefile, File targetfile){
    	boolean tmp_re = false;
   
		String mPath = null;

		pathError = false;
    	if(targetfile.getPath().startsWith(flash_dir))
		{
    		if ((sdcard_dir != null) && targetfile.getPath().startsWith(sdcard_dir)){
    			if (StorageUtils.isMountSD(mStorageManager)){
    				mPath = sdcard_dir;
    			}else{
    				mPath = flash_dir;
    			}
    		}else {
    			mPath = flash_dir;
    		}
    	}
		else if((sdcard_dir != null) && targetfile.getPath().startsWith(sdcard_dir))
		{
			if (StorageUtils.isMountSD(mStorageManager)){
				mPath = sdcard_dir;
			}else{
				mPath = flash_dir;
			}
    	}
		else if((usb_dir != null) && targetfile.getPath().startsWith(usb_dir))
    	{
			int position = targetfile.getPath().lastIndexOf("/",targetfile.getPath().length());
			mPath = targetfile.getPath().substring(0,position+1);
    	}
//		else if(targetfile.getPath().startsWith(usb_dir1))
//		{
//				int position = targetfile.getPath().lastIndexOf("/",targetfile.getPath().length());
//				mPath = targetfile.getPath().substring(0,position+1);
//		}
//		else if(targetfile.getPath().startsWith(usb_dir2))
//		{
//				int position = targetfile.getPath().lastIndexOf("/",targetfile.getPath().length());
//				mPath = targetfile.getPath().substring(0,position+1);
//		}
//		else if(targetfile.getPath().startsWith(usb_dir3))
//		{
//			int position = targetfile.getPath().lastIndexOf("/",targetfile.getPath().length());
//			mPath = targetfile.getPath().substring(0,position+1);
//		}
//		else if(targetfile.getPath().startsWith(usb_dir4))
//		{
//				int position = targetfile.getPath().lastIndexOf("/",targetfile.getPath().length());
//			mPath = targetfile.getPath().substring(0,position+1);
//		}
//		else if(targetfile.getPath().startsWith(usb_dir5))
//		{
//				int position = targetfile.getPath().lastIndexOf("/",targetfile.getPath().length());
//			mPath = targetfile.getPath().substring(0,position+1);
//		}
//		else if(targetfile.getPath().startsWith(usb_dir5))
//		{
//			mPath = usb_dir5;
//		}
//		else if(targetfile.getPath().startsWith(sata_dir))
//		{
//				int position = targetfile.getPath().lastIndexOf("/",targetfile.getPath().length());
//			mPath = targetfile.getPath().substring(0,position+1);
//		}
		else if(targetfile.getPath().startsWith(smb_mountPoint)){
    		LOG("targetfile path = "+targetfile.getPath());
    		return true;
    	}
	
    	LOG("path = "+mPath);

        StatFs stat = null;
        try
       {
        stat = new StatFs(mPath);
       }
       catch(IllegalArgumentException e)
       {
       		pathError = true;
       		return false;
       }
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
	 long totalSize = stat.getBlockCount();
	 
        long space = availableBlocks * blockSize;
        LOG("--- in ishavemorespace(), blockSize = " + blockSize + ",  availableBlocks = " + availableBlocks 
        		+ ",  space = " + space+"totalSize = "+totalSize);

	if(totalSize == 0)
 	{
 		pathError = true;
		LOG("pathError = "+(pathError?"True":"false"));
		return false;
 	}
        tmp_re = space > sourcefile.length();
		Log.d(TAG,"ishavefreespace, have space "+tmp_re);
    	return tmp_re;
    }
} 


 

