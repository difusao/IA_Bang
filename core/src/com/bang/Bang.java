package com.bang;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.nn.NetworkUtils;
import com.nn.NeurophStudio;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.TransferFunctionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Bang extends ApplicationAdapter {

	int WIDTH;
	int HEIGHT;

	// Real World
	public static final int PPM = 30;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;

	OrthographicCamera box2DCamera;
	Box2DDebugRenderer debugRenderer;
	World world;

	// Time Elapsed
	long start = 0;
	long finish = 0;
	float timeElapsed = 0;

	// AG
	int waveTotal = 5;
	int wave = 0;

	// Objects
	float angle = 15.708f;
	float power = 0.01f;
	float target = 130.0f;
	float shotW = 50;
	boolean shot = false;
	double[] arrAngles = new double[waveTotal];
	double[] arrPowers = new double[waveTotal];

	// Inputs NN
	float inAngle;
	float inPower;
	float inObjDown;
	float inTarget;

	double tx = 0;
	double ty = 0;
	float ang = 18;

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
	Stage stage;
	Slider sldAngle;
	Slider sldPower;
	Slider sldTarget;
	CheckBox chkAuto;

	TextField txtAngleValue;
	TextField txtPowerValue;
	TextField txtTargetValue;

	// NeuralNetwork
	DataSet trainingSet = new DataSet(1, 2);
	String FileDataset = "DataSet.tset";
	String FileNetwork = "NewNeuralNetwork";
	String pathDataSet = "data/";
	String pathNetwork = "data/";

	NeurophStudio rna = new NeurophStudio(FileDataset, FileNetwork, pathDataSet, pathNetwork);
	NetworkUtils nnu = new NetworkUtils();

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void create() {
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();

        box2DCamera = new OrthographicCamera();

        if(WIDTH == 1123) {
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 4), (HEIGHT) / (PPM / 4) );
            box2DCamera.position.set(70, 30, 0);

			pathDataSet = "";
			pathNetwork = "";
        }else{
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM) + 10, (HEIGHT) / (PPM) + 10 );
            box2DCamera.position.set(PPM, PPM / 2, 0);

			pathDataSet = "/data/data/com.bang/files/";
			pathNetwork = "/data/data/com.bang/files/";
        }

        box2DCamera.update();
        world = new World(new Vector2(0,-9.8f),true);
        debugRenderer = new Box2DDebugRenderer();

        // Default values
		for(int i=0; i<waveTotal; i++) {
			//arrAngles[i] = 16.0f + (i * 1.4f);
			arrAngles[i] = nnu.RamdomValues(20, 30);
			//arrAngles[i] = 24.000f;

			//arrPowers[i] = 1.0f * (i * 1.0f);
			arrPowers[i] = nnu.RamdomValues(2.0f, 8);
			//arrPowers[i] = 02.0900f;
		}

		stage = new Stage();

		ShowPanelOptions(10, this.HEIGHT - 150,  300, 100);
		BodyGround(WIDTH / 2, 0f, HEIGHT / 2 , -1f, true);
		bodyTarget(1.0f,1.0f, target,-5.0f, BodyDef.BodyType.StaticBody);
		BodyLauncher(1.0f, 3.0f, 0, 0, angle);

		// Start Shot
		Shot(shotW);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		debugRenderer.render(world, box2DCamera.combined);
		box2DCamera.update();

		stage.draw();

		// Elevate laucher
		ElevateLauncher();

		// Position target
		bodyTarget.setTransform(target, 0.0f, 0);

		if ((CollisionBox("ground", "shot") || CollisionBox("alvo", "shot"))) {
			if (!collide) {
				wave++;
				collide = true;
				inObjDown = bodyObj.getPosition().x;
				posMark.add(inObjDown);

				createAlgoritm7(inAngle, power, inObjDown, inTarget, shotW);

				if (chkAuto.isChecked() && wave < waveTotal) {
					inAngle = (float) arrAngles[wave];
					inPower = (float) arrPowers[wave];

					sldAngle.setValue(inAngle);
					sldPower.setValue(inPower);

					Shot(shotW);
				}
			}
		}


		world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
		//batch.dispose();
		//font1.dispose();
		//shapeRenderer.dispose();
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

	private void ShowPanelOptions(float x, float y, float w, float h) {
		Skin skin = new Skin(Gdx.files.internal("themes/default/skin/uiskin.json"));

		final Label lblAngle = new Label("Angle:", skin);
		txtAngleValue = new TextField(String.format(Locale.US,"%06.3f°", ang), skin);

		final Label lblPower = new Label("Power:", skin);
		txtPowerValue = new TextField(String.format(Locale.US,"%06.3f°", power), skin);

		final Label lblTarget = new Label("Target:", skin);
		txtTargetValue = new TextField(String.format(Locale.US,"%06.3f°", target), skin);
		txtTargetValue.setText(String.format(Locale.US, "%06.3f°", target));

		final Label lblAuto = new Label("Auto:", skin);

		final Label lblNull = new Label(null, skin);

		sldAngle = new Slider(16.0f, 30.0f, 0.01f, false, skin);
		sldAngle.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				angle = sldAngle.getValue();
				txtAngleValue.setText(String.format(Locale.US,"%06.3f°", angle));
			}
		});

		sldPower = new Slider(0.50f, 10.0f, 0.01f, false, skin);
		sldPower.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				power = sldPower.getValue();
				txtPowerValue.setText(String.format(Locale.US, "%06.3f°", power));
			}
		});

		sldTarget = new Slider(10.00f, 140.0f, 1.0f, false, skin);
		sldTarget.setValue(target);
		sldTarget.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				target = sldTarget.getValue();
				txtTargetValue.setText(String.format(Locale.US, "%06.3f°", target));
			}
		});

		txtAngleValue.addListener(new FocusListener(){
			public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
				if(!focused) {
					sldAngle.setValue(Float.parseFloat(txtAngleValue.getText()));
				}
			}
		});

		txtAngleValue.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER) {
					sldAngle.setValue(Float.parseFloat(txtAngleValue.getText()));
					stage.setKeyboardFocus(txtPowerValue);
					txtPowerValue.selectAll();
				}
				return false;
			}
		});

		txtPowerValue.addListener(new FocusListener(){
			public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
				if(!focused) {
					sldPower.setValue(Float.parseFloat(txtPowerValue.getText()));
				}
			}
		});

		txtPowerValue.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER) {
					sldPower.setValue(Float.parseFloat(txtPowerValue.getText()));
					stage.setKeyboardFocus(txtTargetValue);
					txtTargetValue.selectAll();
				}
				return false;
			}
		});

		txtTargetValue.addListener(new FocusListener(){
			public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
				if(!focused) {
					sldTarget.setValue(Float.parseFloat(txtTargetValue.getText()));
					target = sldTarget.getValue();
				}
			}
		});


		txtTargetValue.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER) {
					sldTarget.setValue(Float.parseFloat(txtTargetValue.getText()));
					target = sldTarget.getValue();
					stage.setKeyboardFocus(txtAngleValue);
					txtAngleValue.selectAll();
				}
				return false;
			}
		});

		chkAuto = new CheckBox(null, skin);
		chkAuto.setChecked(true);

		Table table = new Table();
		//table.debug();
		stage.addActor(table);
		table.setSize(w, h);
		table.setPosition(x, y);

		table.add(lblAngle).width(80).height(30);
		table.add(sldAngle).width(100);
		table.add(txtAngleValue).width(100);
		table.row();

		table.add(lblPower).width(80).height(30);
		table.add(sldPower).width(100);
		table.add(txtPowerValue).width(100);
		table.row();

		table.add(lblTarget).width(80).height(30);
		table.add(sldTarget).width(100);
		table.add(txtTargetValue).width(100);
		table.row();

		table.add(lblAuto).width(80).height(30);
		table.add(chkAuto).width(100);
		table.add().width(100);
		table.row();

		table.add(lblNull).colspan(3).height(30);
		table.row();

		TextButton button1 = new TextButton("Launch", skin);
		button1.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Shot(shotW);
				return false;
			}
		});

		TextButton button2 = new TextButton("Restart", skin);
		button2.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {

				wave = 0;
				posMark.clear();
				//listaShots.clear();
				trainingSet.clear();

				return false;
			}
		});

		table.add(button1);
		table.add(button2).colspan(2).height(30);

		//inAngle = (float)arrAngles[wave];
		//inPower = (float)arrPowers[wave];

		//sldAngle.setValue(inAngle);
		//sldPower.setValue(inPower);

		stage.setKeyboardFocus(txtAngleValue);
		txtAngleValue.selectAll();

		Gdx.input.setInputProcessor(stage);
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
		//bodyLnchr.setTransform(10, 10, 0);

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
		ang = getAngle(tx, ty);

		// Elevate Laucher
		bodyLnchr.setTransform(0, 0, arctan);
	}

	private void createAlgoritm7(double inAngle, double inPower, double inObjDown, double inTarget, double inShotW) {
		// Meural Network
		String[] inputsLabel = new String[]{"Target"};
		String[] outputsLabel = new String[]{"Angle", "Power"};
		double[] inputs = new double[]{ (inObjDown / 100)};
		double[] outputs = new double[]{ (inAngle / 100), (inPower / 10) };

		trainingSet.addRow(new DataSetRow(inputs, outputs));

		// Inputs
		System.out.print((wave - 1) + " Inputs: ");
		for(int i=0; i<inputs.length; i++)
			System.out.printf(Locale.US,"%012.8f ", inputs[i]);

		for(int i=0; i<outputs.length;i++)
			System.out.printf(Locale.US,"Outputs: %012.8f ", outputs[i]);

		System.out.println();

		if(wave == waveTotal && chkAuto.isChecked()) {

			// Disable check Auto
			chkAuto.setChecked(false);

			//Clear NeuronNetwork and Dataset
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
					0.00001f,
					0.2f,
					0.7f,
					100000000,
					inputsLabel,
					outputsLabel);

			System.out.println("Iterações: " + iterations);

			finish = System.currentTimeMillis();
			timeElapsed = ((finish - start));
			System.out.printf(Locale.US, "\nTime Elapsed:   %03.2fs (%f)%n%n", (timeElapsed / 1000 / 60), timeElapsed );

			target = new NetworkUtils().RamdomValues(20, 120);

			// Clear Test
			wave = 0;
			posMark.clear();

			// Launcher
			trainingSet.clear();;
			trainingSet.addRow(new DataSetRow(new double[]{ (target / 100) }, new double[]{ 0, 0 }));
			double[] TestOutputs = rna.TestNetworkMl(FileNetwork + ".nnet", trainingSet);
			angle = (float) TestOutputs[0] * 100;
			power = (float) TestOutputs[1] * 10;

			inAngle = angle;
			inPower = power;
			inTarget = target;

			sldAngle.setValue((float)inAngle);
			sldPower.setValue((float)inPower);
			sldTarget.setValue((float)inTarget);

			shot = true;
		}
	}
}
