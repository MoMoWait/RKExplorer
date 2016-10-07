/*
 * Copyright (C) 2009 The Rockchip Android MID Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.rockchip;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.rockchip.FileInfo;
import com.android.rockchip.R;
import com.android.rockchip.RockExplorer;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.widget.ListView;
import android.widget.Toast;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import android.os.Environment;
import android.os.storage.StorageManager;

public class FileControl {	
	final String TAG = "FileControl";
	final boolean DEBUG = true;	//true;
	private void LOG(String str)
	{
		if(DEBUG)
		{
			Log.d(TAG,str);
		}
	}
	public ArrayList<FileInfo> folder_array;	
	Resources resources;	
	Context context_by;
	ListView main_ListView;
	ListView mDeviceList;
	public String currently_parent = null;	
	public String currently_path = null;

	public String flash_dir = StorageUtils.getFlashDir();
    public String sdcard_dir;
    private static String mStorageType = "flash";
    //	public String usb_dir0 = Environment.getHostStorage_Extern_0_Directory().getPath();
//	public String usb_dir1 = Environment.getHostStorage_Extern_1_Directory().getPath();
//	public String usb_dir2 = Environment.getHostStorage_Extern_2_Directory().getPath();
//	public String usb_dir3 = Environment.getHostStorage_Extern_3_Directory().getPath();
//	public String usb_dir4 = Environment.getHostStorage_Extern_4_Directory().getPath();
//	public String usb_dir5 = Environment.getHostStorage_Extern_5_Directory().getPath();

	public String usb_dir;//"/mnt/udisk/usb";
//	public String sata_dir =  Environment.getInterHardDiskStorageDirectory().getPath();
	public String smb_dir = "SMB";
	public String smb_mountPoint = "/data/smb";
	
	int currently_state;
	final int AZ_COMPOSITOR = 0;	/* sort by name*/
	final int TIME_COMPOSITOR = 1;	/* sort by time*/
	final int SIZE_COMPOSITOR = 2;	/* sort by size */ 
	final int TYPE_COMPOSITOR = 3;	/* sort by type */
	
	String[] music_postfix = {".mp3", ".ogg", ".wma", ".wav", ".ape", 
								".mid", ".flac", ".mp3PRO", ".au", ".avi"};
	int size_postfix[] = new int[music_postfix.length];
	int pit_postfix[] = new int[music_postfix.length];
	ArrayList<FileInfo> type_compositor_file;
	
	public String str_audio_type = "audio/*";
	public String str_video_type = "video/*";
	public String str_image_type = "image/*";
	public String str_txt_type = "text/plain";
	public String str_pdf_type = "application/pdf";
	public String str_epub_type = "application/epub+zip";
	public String str_apk_type = "application/vnd.android.package-archive";
	
	static boolean is_enable_fill = true;
	static boolean is_finish_fill = false;
	
	static boolean is_first_path = true;

	public static String str_last_path = null;
	public static int last_item;
	public static RockExplorer mRockExplorer;	

	public static boolean is_enable_del = true;
	public static boolean is_finish_del = true;	
	public static ArrayList<FileInfo> deleteFileInfo = new ArrayList<FileInfo>();
	private StorageManager mStorageManager = null;
	
    public FileControl(Context context, String path, ListView tmp_main_listview, StorageManager storageManager) {
        
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
        mStorageManager = storageManager;
        sdcard_dir = StorageUtils.getSDcardDir(storageManager);
        usb_dir = StorageUtils.getUsbDir(storageManager);
        currently_parent = path;
        currently_path = path;
        currently_state = SIZE_COMPOSITOR;    		
        main_ListView = tmp_main_listview;
        resources = context.getResources();
        context_by = context;
        first_fill();
		
    }
       
    private void fill(File files) {
    	is_first_path = false;
    	folder_array = new ArrayList<FileInfo>();	

    	if(mRockExplorer.mWaitDialog != null){
    		mRockExplorer.mWaitDialog.setCancelable(true);
    	}
    	List<String> sdCardPaths = StorageUtils.getSdCardPaths((StorageManager)mRockExplorer.getSystemService(Context.STORAGE_SERVICE));
    	List<String> usbPaths = StorageUtils.getUsbPaths((StorageManager)mRockExplorer.getSystemService(Context.STORAGE_SERVICE));
    	if(mStorageType.equals("sdCard") && files.getPath().equals(sdcard_dir)){
    		Log.i(TAG, "enter sd file run");
        	for (File file : files.listFiles()){
    			if(!is_enable_fill)
    				break;
    			Log.i(TAG, "filePath:" + file.getPath());
    			if(file.canRead()  && !file.isHidden()){
    				if(sdCardPaths.contains(file.getPath())){
    					if(file.isDirectory()){		
    						folder_array.add(0, changeFiletoFileInfo(file)); 
    					}else{
    						folder_array.add(changeFiletoFileInfo(file)); 
    					}	
    				}
    			}
    		}
    	}else if(mStorageType.equals("usb") && files.getPath().equals(usb_dir)){
    		Log.i(TAG, "enter usb file run");
        	for (File file : files.listFiles()){
    			if(!is_enable_fill)
    				break;
    			Log.i(TAG, "filePath:" + file.getPath());
    			if(file.canRead()  && !file.isHidden()){
    				if(usbPaths.contains(file.getPath())){
    					if(file.isDirectory()){		
    						folder_array.add(0, changeFiletoFileInfo(file)); 
    					}else{
    						folder_array.add(changeFiletoFileInfo(file)); 
    					}	
    				}
    			}
    		}
    	}else{
    		Log.i(TAG, "enter other file run");
        	for (File file : files.listFiles()){
    			if(!is_enable_fill)
    				break;
    			Log.i(TAG, "filePath:" + file.getPath());
    			if(file.canRead()  && !file.isHidden()){
					if(file.isDirectory()){		
						folder_array.add(0, changeFiletoFileInfo(file)); 
					}else{
						folder_array.add(changeFiletoFileInfo(file)); 
					}	
				
    			}
    		}
    	}

    	FileInfo item = new FileInfo();
		item.mIcon = resources.getDrawable(R.drawable.icon_folder);
		item.mIsDir = false;
		LOG("FileControl Fill() NullPath:" + currently_path+ (new String("/..")));
		item.mFile = new File(currently_path+ (new String("/..")));
		item.mFileType = "..";	
		folder_array.add(0,item);
    }
    
    public void refill(String path){
    	final File files = new File(path);
    	is_finish_fill = false;
		
		currently_path = new String(files.getPath());
		LOG("FileControl Refill() NewPath:" + currently_path);

		//parser files in folder
    	fill(files);
	    
	    if(currently_path.equals(flash_dir) || 
	    		currently_path.equals(sdcard_dir) || currently_path.equals(usb_dir) ||
	    		currently_path.equals(smb_dir))
	    	currently_parent = null;
	    else{
	    	if(files.getParent().equals(smb_mountPoint)){
	    		currently_parent = new File(mRockExplorer.mCurrnetSmb).getParent();
	    	}else{
	    		currently_parent = new String(files.getParent());
	    	}
	    }
		LOG("FileControl Refill() NewPathParent:" + currently_parent);
	    		    
	    is_finish_fill = true;
	    is_enable_fill = true;
    }
    
    public void refillwithThread(String path){
    	final File files = new File(path); 
    	is_finish_fill = false;
    	new Thread(){
    		public void run(){
    			
				currently_path = new String(files.getPath());
				LOG("FileControl refillwithThread() NewPath:" + currently_path);

				//parser files in folder
		    	fill(files);
				
			    if(currently_path.equals("/"))
			    	currently_parent = new String("/");
			    else{
			    	if(files.getParent().equals(smb_mountPoint)){
			    		currently_parent = new File(mRockExplorer.mCurrnetSmb).getParent();
			    	}else{
			    		currently_parent = new String(files.getParent());
			    	}
			    }

				LOG("FileControl refillwithThread() NewPathParent:" + currently_parent);
			    
			    is_finish_fill = true;
			    is_enable_fill = true;
    		}
    	}.start();
    }
    
    public ArrayList<FileInfo> get_folder_array(){
    	return folder_array;
    }
    public void set_folder_array(ArrayList<FileInfo> tmp_set_array){
    	folder_array = tmp_set_array;
    }
    
    
    public String get_currently_parent(){
    	return currently_parent;
    }
    
    public String get_currently_path(){
    	return currently_path;
    }
    
    public void set_main_ListView(ListView tmp_listview){
    	main_ListView = tmp_listview;
    	if(main_ListView == null){
    		LOG("in set_main_ListView,------------main_ListView = null");
    	}
    }
    
    public void setMainAdapter(){
    	LOG("in setMainAdapter,----11----folder_array size = " + folder_array.size());
    	NormalListAdapter tempAdapter = new NormalListAdapter(context_by, folder_array, mStorageManager);       	
    	LOG("in setMainAdapter,----22----folder_array size = " + folder_array.size());
    	main_ListView.setAdapter(tempAdapter);    	
    }
    
    public String getMIMEType(File f) 
    { 
      String type="";
      String fName=f.getName();
      String end=fName.substring(fName.lastIndexOf(".")+1,
                                 fName.length()).toLowerCase(); 
      
      /* get type name  by MimeType */
      if(end.equalsIgnoreCase("mp3")||end.equalsIgnoreCase("wma")
         ||end.equalsIgnoreCase("mp1")||end.equalsIgnoreCase("mp2")
    	 ||end.equalsIgnoreCase("ogg")||end.equalsIgnoreCase("oga")
    	 ||end.equalsIgnoreCase("flac")||end.equalsIgnoreCase("ape")
    	 ||end.equalsIgnoreCase("wav")||end.equalsIgnoreCase("aac")
    	 ||end.equalsIgnoreCase("m4a")||end.equalsIgnoreCase("m4r")
	 ||end.equalsIgnoreCase("amr")||end.equalsIgnoreCase("mid")
	 ||end.equalsIgnoreCase("asx"))
      {
        type = str_audio_type; 
      }
      else if(end.equalsIgnoreCase("3gp")||end.equalsIgnoreCase("mp4")
    		  ||end.equalsIgnoreCase("rmvb")||end.equalsIgnoreCase("3gpp")
    		  ||end.equalsIgnoreCase("avi")||end.equalsIgnoreCase("rm")
    		  ||end.equalsIgnoreCase("mov")||end.equalsIgnoreCase("flv")
    		  ||end.equalsIgnoreCase("mkv")||end.equalsIgnoreCase("wmv")
		  ||end.equalsIgnoreCase("divx")||end.equalsIgnoreCase("bob")
		  ||end.equalsIgnoreCase("mpg") || end.equalsIgnoreCase("mpeg")
		  ||end.equalsIgnoreCase("ts") || end.equalsIgnoreCase("dat")
		  ||end.equalsIgnoreCase("m2ts")||end.equalsIgnoreCase("vob")
		  ||end.equalsIgnoreCase("asf")||end.equalsIgnoreCase("evo")
		  ||end.equalsIgnoreCase("iso"))
      {
        type = str_video_type;
        if(end.equalsIgnoreCase("3gpp")){
        	if(isVideoFile(f)){
        		type = str_video_type;
        	}else{
        		type = str_audio_type; 
        	}
        }
      }
      else if(end.equalsIgnoreCase("jpg")||end.equalsIgnoreCase("gif")
    		  ||end.equalsIgnoreCase("png")||end.equalsIgnoreCase("jpeg")
    		  ||end.equalsIgnoreCase("bmp"))
      {
        type = str_image_type;
      }
      else if(end.equalsIgnoreCase("txt"))
      {
        type = str_txt_type;
      }
     else if(end.equalsIgnoreCase("epub") || end.equalsIgnoreCase("pdb") || end.equalsIgnoreCase("fb2") || end.equalsIgnoreCase("rtf") || end.equalsIgnoreCase("txt"))
      {
        type = str_epub_type;
      }
      else if(end.equalsIgnoreCase("pdf"))
      {
        type = str_pdf_type;
      }
      else if(end.equalsIgnoreCase("apk"))
      {
    	type = str_apk_type;  
      }
      else
      {
        type="*/*";
      }
      
      return type; 
    }
    

    private String fname;
    public void deleteFileInfo(final ArrayList<FileInfo> file_paths){
    	is_enable_del = true;
	is_finish_del = false;
    	new Thread(){
    		public void run(){
    			for(int file_num = 0; file_num < file_paths.size(); file_num ++){
				boolean del_successful = true;
				if(file_paths.get(file_num).mFile.canWrite()){
	    				if(file_paths.get(file_num).mFile.isDirectory()){
    						del_successful = deleteDirectory(file_paths.get(file_num).mFile);
    					}else{
    						if(!file_paths.get(file_num).mFile.delete()){
							Log.e(TAG, "  ------- :   Delete file " + file_paths.get(file_num).mFile.getPath() + " fail~~");
							fname = file_paths.get(file_num).mFile.getName();
							mRockExplorer.mHandler.post(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(context_by, context_by.getString(R.string.edit_delete) + fname + context_by.getString(R.string.del_error), Toast.LENGTH_SHORT).show();
								}
								
							});
							is_enable_del = false;
						}
						if(!file_paths.get(file_num).mFile.canWrite())
							del_successful = false;
    					}
				}
				if(!is_enable_del){
					is_finish_del = true;
					return;
				}
				if(del_successful)
					deleteFileInfo.add(file_paths.get(file_num));				
    			}
			is_finish_del = true;
    		}
    	}.start();	
    }
	
    public boolean deleteDirectory(File dir){
	boolean ret = true;
	if(!is_enable_del)
		return false;
    	File[] file = dir.listFiles();
    	for (int i = 0; i < file.length; i++) {
    		if (file[i].isFile()){
			if(!is_enable_del)
				return false;
    			if(!file[i].delete()){
				Log.e(TAG, "  ------- :    Delete file " + file[i].getPath() + " fail~~");
			}
			if(!file[i].canWrite())
				ret = false;
    		}else{
			if(!is_enable_del)
				return false;
    			deleteDirectory(file[i]);
		}
    	}
    	dir.delete();
	return ret;
    }
    
    public void deleteFile(File file){
    	if(file == null)
    		return;
    	LOG(" delete file:%s" + file.getPath());
    	File tmp_file = null;
    	if((tmp_file = new File(currently_path+File.separator+file.getName())).exists()){
    		tmp_file.delete();
    	}
    }
    
    public FileInfo changeFiletoFileInfo(File file){
    	FileInfo temp = new FileInfo();
		temp.mFile = file;
		//temp.musicType = isMusicFile(temp.name);
		if(file.isDirectory()){		/* file is folder*/
			temp.mIcon = resources.getDrawable(R.drawable.icon_folder);
			temp.mIsDir = true;
		}	
		else {
			temp.mIsDir = false;
			/* get type name*/
			temp.mFileType = getMIMEType(file);	
			temp.mIcon = getDrawable(temp.mFileType);
		}
		return temp;
    }
    
    public Drawable getDrawable(String tmp_type){
    	Drawable d = null;
    	if(tmp_type.equals(str_audio_type)){
    		d = resources.getDrawable(R.drawable.icon_audio);
    	}else if(tmp_type.equals(str_video_type)){
    		d = resources.getDrawable(R.drawable.icon_video);
    	}else if(tmp_type.equals(str_image_type)){
    		d = resources.getDrawable(R.drawable.icon_photo);
	}else if(tmp_type.equals(str_txt_type) || tmp_type.equals(str_pdf_type) || tmp_type.equals(str_epub_type)){
                d = resources.getDrawable(R.drawable.icon_folder);
    	}else if(tmp_type.equals(str_apk_type)){
    		d = resources.getDrawable(R.drawable.icon_apk);
    	}else {
    		d = resources.getDrawable(R.drawable.icon_other);
    	}
    	return d;
    }
    
    public boolean isFileInFolder(File file){
    	for(int i = 0; i < folder_array.size(); i ++){
    		if(folder_array.get(i).mFile.getPath().equals(file.getPath())){
    			return true;
    		}
    	}
    	return false;
    }
	
    void first_fill(){
    	is_first_path = true;
    	folder_array = new ArrayList<FileInfo>();
		mRockExplorer = (RockExplorer)context_by;

    	currently_path = null;
    	currently_parent = null;
    }
    
    public boolean isVideoFile(File tmp_file){
    	String path = tmp_file.getPath();
    	ContentResolver resolver = context_by.getContentResolver();
    	String[] audiocols = new String[] {
    			MediaStore.Video.Media._ID,
    			MediaStore.Video.Media.DATA,
    			MediaStore.Video.Media.TITLE
        };  
    	LOG("in getFileUri --- path = " + path);
    	StringBuilder where = new StringBuilder();
    	where.append(MediaStore.Video.Media.DATA + "=" + "'" + path + "'");
      try{
    	Cursor cur = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        		audiocols,
        		where.toString(), null, null);
    	if(cur.moveToFirst()){
    		return true;
    	}
    	return false;
      }catch(Exception e){
        return false;
      }
    }

    private FileInfo createDLNAVirtualFile(String path){
    	FileInfo dlnaItem = new FileInfo();
    	dlnaItem.mFile = new File(path);
    	dlnaItem.mIsDir = true;
    	return dlnaItem;
    }
    
    private FileInfo createSMBVirtualFile(String path){
    	FileInfo smbItem = new FileInfo();
    	smbItem.mFile = new File(path);
    	smbItem.mIsDir = true;
    	return smbItem;
    }
    
    public static void setStorageType(String storageType) {
		FileControl.mStorageType = storageType;
	}
}
