package com.android.rockchip;

import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;

public class AutoSize {
	private static AutoSize mInstance=null;
	private int mWinWidth;
	private int mWinHeight;
	private float mTextSize=0.06f;
	private float mMargin=0.02f;
	private int mDensity = 160;
	public static synchronized AutoSize getInstance(){
		if(null==mInstance){
			mInstance= new AutoSize();
		}
		return mInstance;
	}
	public void setWinSize(int width, int height, int density){
		mWinWidth = width;
		mWinHeight = height;
		mDensity = density;
	}

	public float getDensityFactor(){
		if(0==mDensity){
			mDensity=160;
		}
		return 160.0f/mDensity;
	}

	public int getTextSize(float factor){
		return (int)(mWinHeight*factor);
	}

	public int getMargin(float factor){
		return (int)(mWinWidth*factor);
	}

	public int getScaleHeight(float factor){
		return (int)(mWinHeight*factor);
	}

	public int getScaleWidth(float factor){
		return (int)(mWinWidth*factor);
	}



	public int getWidth(){return mWinWidth;}

	public int getHeight(){return mWinHeight;}
	
	public LayoutParams getLayoutParams(float factor_w, float factor_h){
		LayoutParams layout = new LayoutParams((int)(mWinWidth*factor_w),(int)(mWinHeight*factor_h));
		layout.gravity = Gravity.CENTER_VERTICAL;
		return layout;
	}
}

