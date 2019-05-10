package com.bang;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.nn.NetworkUtils;
import com.nn.NeurophStudio;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.TransferFunctionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Bang extends ApplicationAdapter {

	// Real World
	public static final int PPM = 30;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;

	OrthographicCamera box2DCamera;
	Box2DDebugRenderer debugRenderer;
	World world;

	int WIDTH;
	int HEIGHT;

	// Time Elapsed
	long start = 0;
	long finish = 0;
	float timeElapsed = 0;

	// AG
	int waveTotal = 10;
	int wave = 0;

	// Objects
	float angle = 18;
	float power = 2;
	float target = 100;
	float shotW = 50;
	int count = -1;
	boolean shot = true;
	double[] arrAngles = new double[waveTotal];
	double[] arrPowers = new double[waveTotal];
	double tx = 0;
	double ty = 0;

	// Objects
	BodyDef bodyGroundDef;
	BodyDef bodyTargetDef;
	BodyDef bodyObjDef;
	BodyDef bodyLauncherDef;
	Body bodyGnd;
	Body bodyObj;
	Body bodyLnchr;
	Body bodyTarget;
	FixtureDef fixtureBallDef;
	FixtureDef fixtureBoxDef;
	Fixture fixtureBall;
	Fixture fixtureBox;
	Fixture fixtureTank;

	boolean collide = true;
	List posMark = new ArrayList();

	// Form
	SpriteBatch batch;
	BitmapFont font1;
	String status;

	// NeuralNetwork
	DataSet trainingSet = new DataSet(1, 2);
	String FileDataset = "DataSet.tset";
	String FileNetwork = "NewNeuralNetwork";
	String pathDataSet = "data/";
	String pathNetwork = "data/";

	NeurophStudio rna;
	NetworkUtils nnu;

	@Override
	public void create() {
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();

        box2DCamera = new OrthographicCamera();

        if(WIDTH == 1123) {
        	// Desktop
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 3), (HEIGHT) / (PPM / 3) );
            box2DCamera.position.set(55, 25, 0);

			pathDataSet = "data/";
			pathNetwork = "data/";

			target = 109;
        }else{
        	// Smartphone
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM) + 10, (HEIGHT) / (PPM) + 10 );
            box2DCamera.position.set(PPM*1.25f, PPM / 2 + 5, 0);

			pathDataSet = "/data/data/com.bang/files/";
			pathNetwork = "/data/data/com.bang/files/";

			target = 75;
        }

		rna = new NeurophStudio(FileDataset, FileNetwork, pathDataSet, pathNetwork);
		nnu = new NetworkUtils();

        box2DCamera.update();
        world = new World(new Vector2(0,-9.8f),true);
        debugRenderer = new Box2DDebugRenderer();

		batch = new SpriteBatch();

		font1 = new BitmapFont(Gdx.files.internal("fonts/verdana20.fnt"));
		font1.setColor(Color.WHITE);
		font1.getData().setScale(1f, 1f);

        // Default values
		for(int i=0; i<waveTotal; i++) {
			arrAngles[i] = nnu.RamdomValues(18, 30);
			arrPowers[i] = nnu.RamdomValues(2.0f, 6);
		}

		angle = (float)arrAngles[0];
		power = (float)arrPowers[0];

		// Objects
		BodyGround(WIDTH / 2, 0f, HEIGHT / 2 , -1f, true);
		bodyTarget(1.0f,1.0f, target,-5.0f, BodyDef.BodyType.StaticBody);
		BodyLauncher(1.0f, 3.0f, 0, 0, angle);

		status = "Treinnering...";

		// Start Shot
		Shot(shotW);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
		batch.dispose();
		font1.dispose();
	}

	@Override
	public void resize(int width, int height) {

	}

	private void BodyShot(float w, float h, float x, float y, float inpulseX, float inpulseY, float weight) {
		bodyObjDef = new BodyDef();
		bodyObjDef.type = BodyDef.BodyType.DynamicBody;
		bodyObjDef.position.set(x, y);
		bodyObj = world.createBody(bodyObjDef);
		bodyObj.applyLinearImpulse(inpulseX, inpulseY, x, y, true);

		//CircleShape shape = new CircleShape();
		//shape.setRadius(0.5f);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(w, h);

		fixtureBallDef = new FixtureDef();
		fixtureBallDef.density = weight;
		fixtureBallDef.friction = 5.0f;
		fixtureBallDef.restitution = 0.00f;
		fixtureBallDef.shape = shape;

		fixtureBall = bodyObj.createFixture(fixtureBallDef);

		bodyObj.setUserData("shot");

		shape.dispose();
	}

	private void Shot(float weight){
		if(wave < waveTotal) {
			if(bodyObj != null) {
				world.destroyBody(bodyObj);
				collide = true;
			}

			if(collide) {
				BodyShot(1f, 1f, (float) (tx), (float) (ty), (float) (tx) * (power),(float) (ty) * (power), weight);
				collide = false;
			}
		}
	}

	private boolean CollisionBox(String a, String b) {
		boolean touch = false;

		for (int i = 0; i < world.getContactCount(); i++) {
			Contact contact = world.getContactList().get(i);

			if (contact.isTouching()) {

				Body contactA = contact.getFixtureA().getBody();
				Body contactB = contact.getFixtureB().getBody();

				if(contactA.getUserData() == a && contactB.getUserData() == b) {
					touch = true;
				}
			}
		}

		return touch;
	}

	private void BodyLauncher(float w, float h, float x, float y, float ang) {

		bodyLauncherDef = new BodyDef();
		bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
		bodyLauncherDef.position.set(x, y);
		//bodyLauncherDef.position.set(100, 50);
		//bodyLauncherDef.fixedRotation = false;
		//bodyLauncherDef.angle = 0;
		//bodyLauncherDef.position.x = x;
		//bodyLauncherDef.position.y = y;

		bodyLnchr = world.createBody(bodyLauncherDef);

		//bodyLnchr.setUserData(this);
		bodyLnchr.setTransform(x, y, ang);

		PolygonShape shape1 = new PolygonShape();
		shape1.setAsBox(w, h);

		FixtureDef fixtureDef1 = new FixtureDef();
		fixtureDef1.shape = shape1;
		fixtureDef1.density = 1;

		fixtureTank = bodyLnchr.createFixture(fixtureDef1);
	}

	private void bodyTarget(float w, float h,float x, float y, BodyDef.BodyType bodytype){
		bodyTargetDef = new BodyDef();
		bodyTargetDef.type = bodytype;
		bodyTargetDef.position.set(x+(w), y+(h));

		bodyTarget = world.createBody(bodyTargetDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(w, h);

		fixtureBoxDef = new FixtureDef();
		fixtureBoxDef.shape = shape;
		fixtureBoxDef.density = 10;

		fixtureBox = bodyTarget.createFixture(fixtureBoxDef);
		bodyTarget.setUserData("alvo");

		shape.dispose();
	}

	private void BodyGround(float w, float h, float x, float y, boolean collision) {
		bodyGroundDef = new BodyDef();
		bodyGroundDef.type = BodyDef.BodyType.StaticBody;
		bodyGroundDef.position.set(x, y);

		bodyGnd = world.createBody(bodyGroundDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(w, h);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;

		Fixture fixture = bodyGnd.createFixture(fixtureDef);

		if(collision)
			bodyGnd.setUserData("ground");

		shape.dispose();
	}

	public float getAngle(double x, double y){
		double arctan = Math.atan(x / y);
		float ang = (360 * (float)arctan) / (2 * (float)Math.PI);

		return ang;
	}

	private void ElevateLauncher() {
		int x = 0;
		int y = 0;

		// Rotation
		tx = x + ( 5 * Math.sin( ( (double) angle) / 10) );
		ty = y - ( 5 * Math.cos( ( (double) angle) / 10) );

		// Angle
		float arctan = (float)Math.atan(tx / ty) * (-1);
		//ang = getAngle(tx, ty);

		// Elevate Laucher
		bodyLnchr.setTransform(0, 0, arctan);
	}

	private void Treinner(double inAngle, double inPower, double inObjDown, double inTarget, double inShotW) {

		// Meural Network
		String[] inputsLabel = new String[]{"Target"};
		String[] outputsLabel = new String[]{"Angle", "Power"};
		double[] inputs = new double[]{ (inObjDown / 100)};
		double[] outputs = new double[]{ (inAngle / 100), (inPower / 10) };

		// DataSet
		trainingSet.addRow(new DataSetRow(inputs, outputs));

		// Inputs
		System.out.print((wave - 1) + " Inputs: ");
		for(int i=0; i<inputs.length; i++)
			System.out.printf(Locale.US,"%012.8f ", inputs[i]);

		for(int i=0; i<outputs.length;i++)
			System.out.printf(Locale.US,"Outputs: %012.8f ", outputs[i]);

		System.out.println();

		if(wave < waveTotal){
			status = "Learning...";
			Shot(shotW);
		}

		if(wave == waveTotal && shot) {

			// Clear NeuronNetwork and Dataset
			new NetworkUtils().DeleteFileNN(pathDataSet + FileDataset);
			new NetworkUtils().DeleteFileNN(pathNetwork + ".nnet" + FileNetwork);

			start = System.currentTimeMillis();

			System.out.print("\nTreinando... ");

			int iterations = rna.PerceptronMLSave(
					TransferFunctionType.SIGMOID,
					trainingSet,
					new int[]{inputs.length, 10, 10, 10, outputs.length},
					FileDataset,
					FileNetwork + ".nnet",
					0.001f,
					0.2f,
					0.7f,
					100000000,
					inputsLabel,
					outputsLabel);

			System.out.println("Iterações: " + iterations);

			finish = System.currentTimeMillis();
			timeElapsed = ((finish - start));
			System.out.printf(Locale.US, "\nTime Elapsed:   %03.2fs (%f)%n%n", (timeElapsed / 1000 / 60), timeElapsed );

			target = new NetworkUtils().RamdomValues(20, target);

			// Clear Test
			wave = 0;

			// Launcher
			trainingSet.clear();;
			trainingSet.addRow(new DataSetRow(new double[]{ (target / 100) }, new double[]{ 0, 0 }));
			double[] TestOutputs = rna.TestNetworkMl(FileNetwork + ".nnet", trainingSet);
			angle = (float) TestOutputs[0] * 100;
			power = (float) TestOutputs[1] * 10;

			shot = false;

			status = "Outputs: " + Arrays.toString(TestOutputs)+ ", Time: " + (timeElapsed / 1000 / 60) + " Epochs = " + iterations;

			count = 0;
		}
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		debugRenderer.render(world, box2DCamera.combined);
		box2DCamera.update();

		// Position target
		bodyTarget.setTransform(target, 0.0f, 0);

		if ((CollisionBox("ground", "shot") || CollisionBox("alvo", "shot")) && shot) {
			if (!collide) {
				wave++;
				collide = true;

				angle = (float)arrAngles[wave-1];
				power = (float)arrPowers[wave-1];

				float inAngle = angle;
				float inObjDown = bodyObj.getPosition().x;
				float inPower = power;
				float inTarget = target;
				float inWeight = shotW;

				Treinner(inAngle, inPower, inObjDown, inTarget, inWeight);

				// Elevate launcher
				ElevateLauncher();
			}
		}

		if(count > -1){
			count++;

			if(count > 180){
				count = -1;
				Shot(shotW);
			}
		}

		PnelInfo();

		world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	}

	private void PnelInfo(){
		batch.begin();
		font1.draw(batch, "Angle: " + angle, 10, HEIGHT - 10);
		font1.draw(batch, "Power: " + power, 10, HEIGHT - 40);
		font1.draw(batch, "Target: " + target, 10, HEIGHT - 70);
		font1.draw(batch, "Status: " + status, 10, HEIGHT - 100);
		font1.draw(batch, "Wave: " + (wave+1) + " / " + waveTotal, 10, HEIGHT - 130);
		batch.end();
	}
}
