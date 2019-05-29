package com.bang.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.bang.Bang5;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 2246 / 2;
		config.height = 1080 / 2;
		config.x = 0;
		config.y = 260;

		new LwjglApplication(new Bang5(), config);
		//new LwjglApplication(new Bang4(), config);
		//new LwjglApplication(new Giro(), config);
	}
}
