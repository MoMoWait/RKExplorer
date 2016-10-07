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
import android.graphics.drawable.Drawable;

/**
 * Represents a file information. An application is made of a name (or title),
 * an intent and an icon.
 */
public class FileInfo {

    public File mFile;

    public Drawable mIcon;
    public boolean mIsDir;
    
    public String mFileType;
    
    public boolean mIsSelected;
    
    public String mDescription;
	public String mUri;
	public int mSelectedIndex;
	
	//cifs
	private String mUsername;
	private String mPassword;
	private boolean mAnonymous;
	private String mMountpoint;
	private boolean mIsMount;
	
    
    public FileInfo() {
    	mIsDir = true;
    	mFile = null;
    	mIcon = null;
    	mFileType = null;
    	mIsSelected = false;
    	
    	mUsername = null;
		mPassword = null;
		mAnonymous = true;
		mMountpoint = null;
		mIsMount = false;
    }
    
    public FileInfo(FileInfo info) {    	
        mIcon = info.mIcon;
        mIsDir = info.mIsDir;
        mFile = info.mFile;
        mFileType = info.mFileType;
        mIsSelected = info.mIsSelected;
		mSelectedIndex = info.mSelectedIndex;
        
        mUsername = info.mUsername;
		mPassword = info.mPassword;
		mAnonymous = info.mAnonymous;
		mMountpoint = info.mMountpoint;
		mIsMount = info.mIsMount;
    }
    
    public String toString() {
        return mFile.getPath();
    }
    
    public String getMUsername() {
		return mUsername;
	}

	public void setMUsername(String username) {
		mUsername = username;
	}

	public String getMPassword() {
		return mPassword;
	}

	public void setMPassword(String password) {
		mPassword = password;
	}

	public boolean isMAnonymous() {
		return mAnonymous;
	}

	public void setMAnonymous(boolean anonymous) {
		mAnonymous = anonymous;
	}

	public String getMMountpoint() {
		return mMountpoint;
	}

	public void setMMountpoint(String mountpoint) {
		mMountpoint = mountpoint;
	}

	public boolean isMIsMount() {
		return mIsMount;
	}

	public void setMIsMount(boolean isMount) {
		mIsMount = isMount;
	}
}
