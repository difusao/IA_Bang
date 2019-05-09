package com.bang;

import com.Tests.TestNN;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class Bang extends ApplicationAdapter {

	// Time Elapsed
	private long start = 0;
	private long finish = 0;
	private float timeElapsed = 0;

	@Override
	public void create() {
		new TestNN().StartTestNN();
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
}
