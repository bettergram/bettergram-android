package ru.johnlife.lifetools.tools;

import android.content.Context;
import android.util.DisplayMetrics;

public class DeviceScreen {

	private int width;
	private int height;
	private int widthDpScaled;
	private int heightDpScaled;
	private float widthIn;
	private float heightIn;
	private int widthDp;
	private int heightDp;
	private float density;

	public DeviceScreen(Context context) {
		DisplayMetrics m = context.getResources().getDisplayMetrics();
		width = m.widthPixels;
		height = m.heightPixels;
		density = m.density;
		widthDp = (int)Math.ceil(((float)width)/density);
		heightDp = (int)Math.ceil(((float)height)/density);
		density = m.scaledDensity;
		widthDpScaled = (int)Math.ceil(((float)width)/density);
		heightDpScaled = (int)Math.ceil(((float)height)/density);
		widthIn = width / m.xdpi;
		heightIn = height / m.ydpi;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getSmallestWidth() {
		return Math.min(width, height);
	}
	
	public int getLargestWidth() {
		return Math.max(width, height);
	}

	public int getWidthDpScaled() {
		return widthDpScaled;
	}

	public int getHeightDpScaled() {
		return heightDpScaled;
	}

	public int getWidthDp() {
		return widthDp;
	}

	public int getHeightDp() {
		return heightDp;
	}

	public float getWidthIn() {
		return widthIn;
	}

	public float getHeightIn() {
		return heightIn;
	}

	public float getDensity() {
		return density;
	}


	public boolean isLandscape() {
		return width > height;
	}

	public boolean isPortrait() {
		return width < height;
	}
}
