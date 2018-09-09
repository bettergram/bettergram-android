package ru.johnlife.lifetools.tools;

import java.util.UUID;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;

public class UniqueId {
	private static String id = null;
	
	public static String get(Context context) {
		if (null == id) {
			id = generate(context);
		}
		return id;
	}
	
	private static String generate(Context context) {
		String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		long buildId = 0;
		buildId = buildId << 2 | Build.BOARD.hashCode()%100;
		buildId = buildId << 2 | Build.BRAND.hashCode()%100;
		buildId = buildId << 2 | Build.CPU_ABI.hashCode()%100;
		buildId = buildId << 2 | Build.DEVICE.hashCode()%100;
		buildId = buildId << 2 | Build.DISPLAY.hashCode()%100;
		buildId = buildId << 2 | Build.HOST.hashCode()%100;
		buildId = buildId << 2 | Build.ID.hashCode()%100;
		buildId = buildId << 2 | Build.MANUFACTURER.hashCode()%100;
		buildId = buildId << 2 | Build.MODEL.hashCode()%100;
		buildId = buildId << 2 | Build.PRODUCT.hashCode()%100;
		buildId = buildId << 2 | Build.TAGS.hashCode()%100;
		buildId = buildId << 2 | Build.TYPE.hashCode()%100;
		buildId = buildId << 2 | Build.USER.hashCode()%100; 
	    UUID deviceUuid = new UUID(androidId.hashCode(), buildId);
	    return deviceUuid.toString();
	}
}
