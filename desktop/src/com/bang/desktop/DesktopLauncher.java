package com.bang.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.bang.Bang;
import com.bang.Bang2;
import com.bang.Bang3;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 1123;
		config.height = 540;
		config.x = 0;
		config.y = 0;

		new LwjglApplication(new Bang3(), config);
		//new LwjglApplication(new Giro(), config);
	}
}
