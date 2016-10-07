package com.android.rockchip;

import android.provider.MediaStore;
import android.media.MediaFile;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public class DBHelper{
	private static final String TAG="FileHelper";

	public static boolean deleteMediaRecord(Context context, String path){
		if(isVideoFile(path)){
			long id = queryDBFileId(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, path);
			Log.e(TAG, "File is VIDEO; id="+id +";path="+path);
			if(-1!=id){
				deleteRecord(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);	
			}
			return true;
		}
		if(isAudioFile(path)){
			long id = queryDBFileId(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, path);
			Log.e(TAG, "File is AUDIO; id="+id +";path="+path);
			if(-1!=id){
				deleteRecord(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);	
			}
			return true;

		}
		if(isImageFile(path)){
			long id = queryDBFileId(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, path);
			Log.e(TAG, "File is IMAGE; id="+id +";path="+path);
			if(-1!=id){
				deleteRecord(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);	
			}
			return true;
		}
		return false;
	}

	private static void deleteRecord(Context context, Uri baseUri, long id){
        //deleteRecord(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);	
		//deleteRecord(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);	
		//deleteRecord(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);	
		if(null!=baseUri){
	        ContentResolver contentResolver = context.getContentResolver();
	        contentResolver.delete(baseUri, "_id=?",
	                new String[]{String.valueOf(id)});
		}
	}
	
	public static long queryDBFileId(Context context, Uri baseUri, String path)
	{
		ContentResolver resolver = context.getContentResolver();
    	String[] cols = new String[] {
    			MediaStore.Video.Media._ID,
    			MediaStore.Video.Media.DATA,
        };  
    	StringBuilder where = new StringBuilder();
    	where.append(MediaStore.Video.Media.DATA + "=" + "'" + path + "'");
		try{
			Cursor cur = resolver.query(baseUri, cols, where.toString(), null, null);
			if(cur.moveToFirst()){
				long id = cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				cur.close();
				return id;
    		}
			cur.close();
    		return -1;
      }catch(Exception e){
        return -1;
      } 
	}

	private static boolean isImageFile(String path){
        
        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = (mediaFileType == null ? 0 : mediaFileType.fileType);
		return MediaFile.isImageFileType(fileType);
	}

	private static boolean isAudioFile(String path){
        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = (mediaFileType == null ? 0 : mediaFileType.fileType);
		return MediaFile.isAudioFileType(fileType);
	}

    private static boolean isVideoFile(String path)
    {	
        if(path == null)
            return false;
        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = (mediaFileType == null ? 0 : mediaFileType.fileType);
		return MediaFile.isVideoFileType(fileType);
		/*	
	        String fileName = file.getName();
	        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
	        if(suffix == null)
	            return false;
			
	        if(suffix.equalsIgnoreCase("3gp")||suffix.equalsIgnoreCase("mp4")
	                ||suffix.equalsIgnoreCase("rmvb")||suffix.equalsIgnoreCase("3gpp")
	                ||suffix.equalsIgnoreCase("avi")||suffix.equalsIgnoreCase("rm")
	                ||suffix.equalsIgnoreCase("mov")||suffix.equalsIgnoreCase("flv")
	                ||suffix.equalsIgnoreCase("mkv")||suffix.equalsIgnoreCase("wmv")
	                ||suffix.equalsIgnoreCase("divx")||suffix.equalsIgnoreCase("bob")
	                ||suffix.equalsIgnoreCase("mpg") || suffix.equalsIgnoreCase("mpeg")
	                ||suffix.equalsIgnoreCase("ts") || suffix.equalsIgnoreCase("dat")
	                ||suffix.equalsIgnoreCase("m2ts") || suffix.equalsIgnoreCase("iso"))
	            return true;

	        return false;
	        */
    }

}
