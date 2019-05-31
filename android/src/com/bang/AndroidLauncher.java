package com.bang;

/*
adb shell screencap -p /sdcard/myfile.jpg
adb pull /sdcard/myfile.jpg

adb shell screenrecord /sdcard/video.mp4
adb pull /sdcard/video.mp4
*/

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		initialize(new Bang5(), config);
	}
}
