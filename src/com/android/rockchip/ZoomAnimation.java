package com.android.rockchip;

import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

class ZoomAnimation extends Animation {
	private float mFrom;
	private float mTo;
	private float mOffsetY;
	private int mPivotX;
	private int mPivotY;
	private float mInterpolatedTime;

	public ZoomAnimation(View v, float from, float to, float offsetY,
			int duration) {
		super();
		mFrom = from;
		mTo = to;
		mOffsetY = offsetY * v.getHeight();
		setDuration(duration);
		setFillAfter(true);
		mPivotX = v.getWidth() / 2;
		mPivotY = v.getHeight() / 2;
	}

	public void resetForZoomOut() {
		reset();
		mOffsetY = 0;
		mFrom = mFrom + (mTo - mFrom) * mInterpolatedTime;
		mTo = 1;
	}

	@Override
	protected void applyTransformation(float interpolatedTime,
			Transformation t) {

		float s = mFrom + (mTo - mFrom) * interpolatedTime;
		Matrix matrix = t.getMatrix();
		matrix.preScale(s, s, mPivotX, mPivotY);
	}
}
