package com.bang;

import com.ag.AlgoritmoGenetico;
import com.ag.Shot;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.nn.NetworkUtils;
import com.nn.NeurophStudio;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.TransferFunctionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Bang extends ApplicationAdapter implements InputProcessor {

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
	float angle = 0;
	float LauncherX = 5;
	float LauncherY = 2;
	float power = 30;
	float target = 100;
	float weight = 50;
	float height = 0;

	float maxhight = 0;

	int count = -1;
	boolean shot = true;
	double[] arrAngles = new double[waveTotal];
	double[] arrPowers = new double[waveTotal];
	double[] arrHight = new double[waveTotal];
	double tx = 0;
	double ty = 0;
	double ang = 0;

	// Objects
	BodyDef bodyGroundDef;
	BodyDef bodyTargetDef;
	BodyDef bodyObjDef;
	BodyDef bodyLauncherDef;
	BodyDef bodyTowerDef;

	Body bodyGnd;
	Body bodyObj;
	Body bodyLnchr;
	Body bodyTarget;
	Body circle;
	Body bodyTwer;

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
	DataSet trainingSet2 = new DataSet(1, 2);
	String FileDataset = "DataSet.tset";
	String FileNetwork = "NewNeuralNetwork";
	String pathDataSet = "data/";
	String pathNetwork = "data/";

	NeurophStudio rna;
	NetworkUtils nnu;
	private boolean neural = false;

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

	private void Rotate(float x, float y, float axe, float ang){

		// Rotation
		float tx = (float)Math.sin((ang - 1.5f) * (-1));
		float ty = (float)Math.cos((ang - 1.5f) * (-1));

		// Angle
		float arctan = (float)Math.atan(tx / ty);

		bodyLnchr.setTransform((tx * axe) + x, (ty * axe) + y, arctan * (-1));
		circle.setTransform( x, y, arctan * (-1) + (1.5f) );

	}

	private void Shot(float x, float y, float power, float weight, float ang){

		if(wave <= waveTotal) {
			if(bodyObj != null) {
				world.destroyBody(bodyObj);
				collide = true;
			}

			if(collide) {
				// Rotation
				float tx = (float)Math.sin((ang - 1.5f) * (-1));
				float ty = (float)Math.cos((ang - 1.5f) * (-1));

				// Angle
				float arctan = (float)Math.atan(tx / ty);

				Rotate(LauncherX, LauncherY, 4, angle);

				BodyShot(
						1,
						1,
						(tx) * power,
						(ty) * power,
						(tx * 7.5f) + x,
						(ty * 7.5f) + y,
						arctan * (-1),
						x + 5,
						y + 5,
						weight);

				collide = false;
			}
		}
	}

	private void BodyShot(float w, float h, float inpulseX, float inpulseY, float transformX, float transformY, float angle, float x, float y, float weight) {
		bodyObjDef = new BodyDef();
		bodyObjDef.type = BodyDef.BodyType.DynamicBody;
		//bodyObjDef.type = BodyDef.BodyType.StaticBody;
		bodyObjDef.position.set(x, y);
		bodyObj = world.createBody(bodyObjDef);
		bodyObj.applyLinearImpulse(inpulseX, inpulseY, x, y, true);
		bodyObj.setTransform( transformX, transformY, angle );

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

	private void BodyBase(float x, float y, float radius){
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(x, y);
		circle = world.createBody(bodyDef);

		CircleShape dynamicCircle = new CircleShape();
		dynamicCircle.setRadius(radius);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicCircle;
		fixtureDef.density = 0.4f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.6f;

		circle.createFixture(fixtureDef);


		dynamicCircle.dispose();
	}

	private void BodyTower(float w, float h, float x, float y){
		bodyTowerDef = new BodyDef();
		bodyTowerDef.type = BodyDef.BodyType.KinematicBody;
		bodyTowerDef.position.set(x, y/2);

		bodyTwer = world.createBody(bodyTowerDef);

		PolygonShape shape1 = new PolygonShape();
		shape1.setAsBox(w, h/2);

		FixtureDef fixtureDef1 = new FixtureDef();
		fixtureDef1.shape = shape1;
		fixtureDef1.density = 1;

		fixtureTank = bodyTwer.createFixture(fixtureDef1);
	}

	private void BodyLauncher(float w, float h, float x, float y, float ang) {

		bodyLauncherDef = new BodyDef();
		bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
		bodyLauncherDef.position.set(x, (y+w+h));

		bodyLnchr = world.createBody(bodyLauncherDef);

		PolygonShape shape1 = new PolygonShape();
		shape1.setAsBox(w, h);

		FixtureDef fixtureDef1 = new FixtureDef();
		fixtureDef1.shape = shape1;
		fixtureDef1.density = 1;

		fixtureTank = bodyLnchr.createFixture(fixtureDef1);
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

	private void bodyTarget(float w, float h,float x, float y, BodyDef.BodyType bodytype){
		bodyTargetDef = new BodyDef();
		bodyTargetDef.type = bodytype;
		bodyTargetDef.position.set(x, (y*2));

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

	private void PanelInfo(){
		batch.begin();
		font1.draw(batch, "Angle: " + angle, 10, HEIGHT - 10);
		font1.draw(batch, "Power: " + power, 10, HEIGHT - 40);
		font1.draw(batch, "Target: " + target, 10, HEIGHT - 70);
		font1.draw(batch, "Status: " + status, 10, HEIGHT - 100);
		font1.draw(batch, "Wave: " + (wave) + "/" + waveTotal, 10, HEIGHT - 130);
		batch.end();
	}

	private DataSet AG(DataSet trainingSet){
		//Genetic Alghoritm
		List<Shot> listShots = new ArrayList();
		for(int i=0; i<trainingSet.getRows().size(); i++) {
			listShots.add(new Shot(
					i,
					trainingSet.getRows().get(i).getInput()[0],
					(target / 100) - trainingSet.getRows().get(i).getInput()[0]
			));

			System.out.println( (target / 100) + " / " + trainingSet.getRows().get(i).getInput()[0]);
		}

		List spaces = new ArrayList();
		List values = new ArrayList();
		List ids = new ArrayList();

		for (Shot shot: listShots) {
			spaces.add(shot.getSpace());
			values.add(shot.getValue());
			ids.add(shot.getId());
		}

		Double limite = 3.0;
		Double taxaMutacao = 0.05;
		int tamanhoPopulacao = 10;
		int numeroGeracoes = 100;

		AlgoritmoGenetico ag = new AlgoritmoGenetico(tamanhoPopulacao);
		List resultado = ag.resolver(taxaMutacao, numeroGeracoes, spaces, values, limite);

		int totalTraining = trainingSet.getRows().size();
		for(int i=0; i<totalTraining; i++)
			if (resultado.get(i).equals("0")) {
				trainingSet.remove(i);
				totalTraining = trainingSet.getRows().size();
			}

		return trainingSet;
	}

	private void PreTrainner(){
		// Sequency full
		trainingSet.addRow(new DataSetRow(new double[]{ 000.15382284 }, new double[]{ 000.10000000, 000.08200000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.18968008 }, new double[]{ 000.20000000, 000.11400000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.24485250 }, new double[]{ 000.30000001, 000.14600000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.32825493 }, new double[]{ 000.40000001, 000.17799999 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.44173283 }, new double[]{ 000.50000000, 000.21000000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.58407043 }, new double[]{ 000.60000002, 000.24200001 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.74968674 }, new double[]{ 000.69999999, 000.27400000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.92264603 }, new double[]{ 000.80000001, 000.30600000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.07553398 }, new double[]{ 000.90000004, 000.33800003 }));

		// sequency fix angle
		/*trainingSet.addRow(new DataSetRow(new double[]{ 000.15382284 }, new double[]{ 000.35825493, 000.08200000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.18968008 }, new double[]{ 000.35825493, 000.11400000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.24485250 }, new double[]{ 000.35825493, 000.14600000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.32825493 }, new double[]{ 000.35825493, 000.17799999 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.44173283 }, new double[]{ 000.35825493, 000.21000000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.58407043 }, new double[]{ 000.35825493, 000.24200001 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.74968674 }, new double[]{ 000.35825493, 000.27400000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.92264603 }, new double[]{ 000.35825493, 000.30600000 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.07553398 }, new double[]{ 000.35825493, 000.33800003 }));*/

		//Ramdom angle fix
		/*trainingSet.addRow(new DataSetRow(new double[]{ 000.52571178 }, new double[]{ 000.35825493, 000.34525303 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.25414192 }, new double[]{ 000.35825493, 000.14275503 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.25942253 }, new double[]{ 000.35825493, 000.09295855 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.13521318 }, new double[]{ 000.35825493, 000.09910114 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.13765844 }, new double[]{ 000.35825493, 000.20074352 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.40865543 }, new double[]{ 000.35825493, 000.21583134 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.62865990 }, new double[]{ 000.35825493, 000.21921360 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.57100391 }, new double[]{ 000.35825493, 000.20749729 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.42771488 }, new double[]{ 000.35825493, 000.16952791 }));*/

		// Ramdom full
		/*trainingSet.addRow(new DataSetRow(new double[]{ 000.45959141 }, new double[]{ 000.67028874, 000.30644588 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.09588509 }, new double[]{ 000.64899182, 000.16062449 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.30127117 }, new double[]{ 001.26450837, 000.31918386 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.55558407 }, new double[]{ 001.05931032, 000.25987432 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.64508682 }, new double[]{ 000.59274757, 000.08821141 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.22835463 }, new double[]{ 000.21328776, 000.13846851 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.28797295 }, new double[]{ 000.41088295, 000.23817520 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.65809425 }, new double[]{ 000.86217695, 000.17242796 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.42642300 }, new double[]{ 000.23241945, 000.15341789 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.32160751 }, new double[]{ 000.19333835, 000.10510767 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.23060301 }, new double[]{ 001.10853362, 000.21803848 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.44847355 }, new double[]{ 000.82467955, 000.23822594 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.70740677 }, new double[]{ 000.00743688, 000.13910162 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.21721832 }, new double[]{ 001.09103382, 000.17250492 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.32927853 }, new double[]{ 000.91557282, 000.29969929 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.97569893 }, new double[]{ 000.53713620, 000.32496628 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.18679283 }, new double[]{ 000.28894845, 000.15287192 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.33946640 }, new double[]{ 000.58733255, 000.34000702 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.31071136 }, new double[]{ 000.52603889, 000.08801929 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.22967712 }, new double[]{ 000.94604683, 000.32809113 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.10177864 }, new double[]{ 000.41595006, 000.18500301 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.46770779 }, new double[]{ 001.08737183, 000.09527779 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.17181334 }, new double[]{ 000.37072465, 000.19174831 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.47295815 }, new double[]{ 001.21575189, 000.11543597 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.16187805 }, new double[]{ 000.19320028, 000.11204786 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.24117201 }, new double[]{ 000.60671377, 000.22644302 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.67592026 }, new double[]{ 001.07659316, 000.28596573 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.73630920 }, new double[]{ 000.85572445, 000.18607023 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.47672829 }, new double[]{ 000.24717534, 000.17707533 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.37957794 }, new double[]{ 000.97633225, 000.28013802 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.81848793 }, new double[]{ 000.88372654, 000.34668243 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.29434982 }, new double[]{ 000.54363608, 000.16840744 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.44125805 }, new double[]{ 000.60265291, 000.30238163 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.06676338 }, new double[]{ 000.11955529, 000.33184021 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.63386642 }, new double[]{ 000.67885453, 000.17300467 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.46119747 }, new double[]{ 001.25661242, 000.06517340 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.10004046 }, new double[]{ 000.23087646, 000.20787630 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.44528984 }, new double[]{ 001.06230974, 000.10618064 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.19624872 }, new double[]{ 000.56456703, 000.32328442 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.19005173 }, new double[]{ 001.24289858, 000.12292812 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.16151594 }, new double[]{ 001.05263484, 000.29505432 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.80737160 }, new double[]{ 000.92917681, 000.15944736 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.36394981 }, new double[]{ 000.82027793, 000.31247086 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.11178459 }, new double[]{ 001.23606479, 000.14717314 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.19054502 }, new double[]{ 000.21649753, 000.07219234 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.19112398 }, new double[]{ 000.37487775, 000.26894444 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.76072701 }, new double[]{ 001.28648734, 000.11510741 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.13533297 }, new double[]{ 000.30754060, 000.18217434 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.41851555 }, new double[]{ 000.49803624, 000.07477890 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.20558220 }, new double[]{ 001.11446345, 000.11282589 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.19248365 }, new double[]{ 000.37348193, 000.23900892 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.63894539 }, new double[]{ 001.17556465, 000.12581738 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.18865715 }, new double[]{ 000.19355074, 000.28326391 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.61851330 }, new double[]{ 000.20818901, 000.06011982 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.17700384 }, new double[]{ 000.82410312, 000.15558278 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.38061321 }, new double[]{ 000.29032436, 000.32329185 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.90126923 }, new double[]{ 000.26595795, 000.11995030 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.26788820 }, new double[]{ 000.09238939, 000.19806698 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.32905781 }, new double[]{ 000.61173147, 000.09518163 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.24229883 }, new double[]{ 000.03464509, 000.28014359 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.38917896 }, new double[]{ 001.26257122, 000.12388315 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.15445996 }, new double[]{ 000.43258020, 000.12668566 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.30802692 }, new double[]{ 000.46885568, 000.13498516 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.33414032 }, new double[]{ 000.96386623, 000.10489333 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.21812374 }, new double[]{ 001.21800339, 000.20510599 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.32022346 }, new double[]{ 000.20303914, 000.14997758 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.30497606 }, new double[]{ 000.32828161, 000.34176765 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.04766693 }, new double[]{ 000.23589988, 000.32382271 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.82041267 }, new double[]{ 000.40066704, 000.14169532 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.33981876 }, new double[]{ 000.66433799, 000.22306896 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.66516663 }, new double[]{ 000.91771126, 000.11392435 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.24741709 }, new double[]{ 001.24310648, 000.08229156 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.11741192 }, new double[]{ 000.49088821, 000.07854007 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.20794281 }, new double[]{ 000.62381893, 000.18144855 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.49297726 }, new double[]{ 000.70930552, 000.07244575 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.19239120 }, new double[]{ 000.05564481, 000.13559632 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.23199766 }, new double[]{ 000.00357823, 000.26566761 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.33215076 }, new double[]{ 000.90880239, 000.25738260 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.75847122 }, new double[]{ 001.08199596, 000.21461729 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.45855362 }, new double[]{ 001.29376161, 000.13690344 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.15596062 }, new double[]{ 000.05527459, 000.08120103 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.18207182 }, new double[]{ 000.51634920, 000.30555208 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.05717964 }, new double[]{ 000.14631742, 000.08736097 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.20133490 }, new double[]{ 000.28250036, 000.30209536 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.80049347 }, new double[]{ 000.27891779, 000.30925152 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.82685532 }, new double[]{ 000.65562475, 000.06847314 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.18878588 }, new double[]{ 000.04085932, 000.20312212 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.29927446 }, new double[]{ 000.41681707, 000.28545486 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.87262810 }, new double[]{ 001.12690008, 000.34647408 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.30057993 }, new double[]{ 000.55373406, 000.14022909 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.35516464 }, new double[]{ 000.56723541, 000.30630169 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 001.07146965 }, new double[]{ 000.48253143, 000.25518637 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.77216599 }, new double[]{ 000.70559448, 000.28456003 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.98297287 }, new double[]{ 000.86148620, 000.25966866 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.79795135 }, new double[]{ 000.47852772, 000.11069248 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.27600538 }, new double[]{ 000.25493449, 000.34399994 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.93488739 }, new double[]{ 001.08038676, 000.29644695 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.77798355 }, new double[]{ 000.93099535, 000.21478134 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.55673985 }, new double[]{ 000.26546732, 000.21218929 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.47784130 }, new double[]{ 001.29596937, 000.30568989 }));
		trainingSet.addRow(new DataSetRow(new double[]{ 000.45781956 }, new double[]{ 000.78035259, 000.26524124 }));*/

		String[] inputsLabel = new String[]{ "Target"};
		String[] outputsLabel = new String[]{ "Angle", "Power" };

		new NetworkUtils().DeleteFileNN(pathDataSet + FileDataset);
		new NetworkUtils().DeleteFileNN(pathNetwork + ".nnet" + FileNetwork);

		start = System.currentTimeMillis();

		// Order by best aprouch target.
		//System.out.println("\nOrder...");
		//trainingSet = rna.Order(trainingSet);
		//for(int i=0; i<trainingSet.getRows().size(); i++)
		//	System.out.println(i + " " + Arrays.toString(trainingSet.getRows().get(i).getInput()) + ", " + Arrays.toString(trainingSet.getRows().get(i).getDesiredOutput()));
		//System.out.println();

		//trainingSet = rna.Order(trainingSet);
		//for(int i=0; i<trainingSet.getRows().size(); i++)
		//	System.out.println(i + " " + Arrays.toString(trainingSet.getRows().get(i).getInput()) + ", " + Arrays.toString(trainingSet.getRows().get(i).getDesiredOutput()));
		//System.out.println();

		System.out.print("\nLearning... ");

		int iterations = rna.PerceptronMLSave(
				TransferFunctionType.SIGMOID,
				trainingSet,
				new int[]{1, 10, 2},
				FileDataset,
				FileNetwork + ".nnet",
				0.001f,
				0.2f,
				0.7f,
				10000000,
				inputsLabel,
				outputsLabel);

		System.out.println("Iterações: " + iterations);
		rna.TestNetworkMl(FileNetwork + ".nnet", trainingSet);

		finish = System.currentTimeMillis();
		timeElapsed = ((finish - start));
		System.out.printf(Locale.US, "Time Elapsed of training:   %03.2fs (%f)%n%n", (timeElapsed / 1000 / 60), timeElapsed );
	}

	private void DefaultShots(){
		// Default values
		for(int i=0; i<waveTotal; i++) {
			arrAngles[i] = nnu.RamdomValues(0, 1.3f);	// Random Angle
			//arrAngles[i] = i * 0.1f;					// Incremental Angle
			//arrAngles[i] = 0.35f;						// Fix Angle

			arrPowers[i] = nnu.RamdomValues(6.0f, 35);	// Random power
			//arrPowers[i] = 5.0f + i * 3.2f;			// Incremental power

			//arrHight[i] = nnu.RamdomValues(2.0f, 35); // Random elevate
		}
	}

	private void Trainner(double inAngle, double inPower, double inObjDown, double inTarget, double inShotW, double inHight) {

		// Meural Network
		String[] inputsLabel = new String[]{ "Target"};
		String[] outputsLabel = new String[]{ "Angle", "Power" };
		double[] inputs = new double[]{ (inObjDown / 100) };
		double[] outputs = new double[]{ (inAngle), (inPower / 100) };

		// DataSet
		trainingSet.addRow(new DataSetRow(inputs, outputs));

		// Inputs
		System.out.printf(Locale.US,"%02d Inputs: ", (wave - 1));
		for(int i=0; i<inputs.length; i++)
			System.out.printf(Locale.US,"%012.8f ", inputs[i]);
		for(int i=0; i<outputs.length;i++)
			System.out.printf(Locale.US,"Outputs: %012.8f ", outputs[i]);
		System.out.println();

		//System.out.print("trainingSet.addRow(new DataSetRow(new double[]{ ");
		//for(int i=0; i<inputs.length; i++) {
		//	System.out.printf(Locale.US, "%012.8f", inputs[i]);
		//	if(i<inputs.length-1)
		//		System.out.print(", ");
		//	else
		//		System.out.print(" }, new double[]{ ");
		//}
		//for(int i=0; i<outputs.length;i++) {
		//	System.out.printf(Locale.US, "%012.8f", outputs[i]);
		//	if(i<outputs.length-1)
		//		System.out.print(", ");
		//}
		//System.out.println(" }));");

		if(wave < waveTotal)
			Shot( LauncherX, LauncherY, power, weight, angle);

		if(wave == waveTotal && shot) {
			// Clear Test
			wave = 0;
			shot = false;

			// Order by best aprouch target.
			//System.out.println("\nOrder...");
			//trainingSet = rna.Order(trainingSet);
			//for(int i=0; i<trainingSet.getRows().size(); i++)
			//	System.out.println(i + " " + Arrays.toString(trainingSet.getRows().get(i).getInput()) + ", " + Arrays.toString(trainingSet.getRows().get(i).getDesiredOutput()));

			AG(trainingSet);

			for(int i=0; i<trainingSet.getRows().size(); i++)
				System.out.println(i + " " + Arrays.toString(trainingSet.getRows().get(i).getInput()) + ", " + Arrays.toString(trainingSet.getRows().get(i).getDesiredOutput()));

			start = System.currentTimeMillis();

			System.out.print("\nLearning... ");

			int iterations = rna.PerceptronMLSave(
					TransferFunctionType.SIGMOID,
					trainingSet,
					new int[]{inputs.length, 10, outputs.length},
					FileDataset,
					FileNetwork + ".nnet",
					0.01f,
					0.2f,
					0.7f,
					10000000,
					inputsLabel,
					outputsLabel);

			System.out.println("Epochs: " + iterations);
			rna.TestNetworkMl(FileNetwork + ".nnet", trainingSet);

			finish = System.currentTimeMillis();
			timeElapsed = ((finish - start));
			System.out.printf(Locale.US, "Time Elapsed of training:   %03.2fs (%f)%n%n", (timeElapsed / 1000 / 60), timeElapsed );

			//target = new NetworkUtils().RamdomValues(50, 100);

			// Start Neural Network
			neural = true;
			count = 0;
		}
	}

	public void TestingNN(double inAngle, double inPower, double inObjDown, double inTarget, double inShotW, double inHight){
		double[] inputs;
		double[] outputs;

		if(wave < waveTotal) {
			status = "Trying...";

			target = new NetworkUtils().RamdomValues(50, 100);

			// Launcher
			double[] TestOutputs = rna.Test(FileNetwork + ".nnet", new double[]{ (target / 100) });
			System.out.println("Outputs: " + Arrays.toString(TestOutputs) + " ");

			angle = (float) TestOutputs[0];
			power = (float) TestOutputs[1] * 100;

			// Meural Network
			inputs = new double[]{ (target / 100) };
			outputs = new double[]{ (angle), (power / 100) };

			// DataSet
			trainingSet.addRow(new DataSetRow(inputs, outputs));

			// Shot object with output results
			Shot(LauncherX, LauncherY, power, weight, angle);
		}

		if(wave == waveTotal) {
			String[] inputsLabel = new String[]{ "Target"};
			String[] outputsLabel = new String[]{ "Angle", "Power" };

			int iterations = rna.PerceptronMLSave(
					TransferFunctionType.SIGMOID,
					trainingSet,
					new int[]{1, 10, 2},
					FileDataset,
					FileNetwork + ".nnet",
					0.0001f,
					0.2f,
					0.7f,
					10000000,
					inputsLabel,
					outputsLabel);

			System.out.println("Iterações: " + iterations);
			rna.TestNetworkMl(FileNetwork + ".nnet", trainingSet);

			wave = 0;
		}
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		debugRenderer.render(world, box2DCamera.combined);
		box2DCamera.update();

		// Position target
		bodyTarget.setTransform(target, 1.0f, 0);

		if(bodyObj != null && bodyObj.getPosition().y > maxhight)
			maxhight = bodyObj.getPosition().y;

		if ((CollisionBox("ground", "shot") || CollisionBox("alvo", "shot")) && shot) {
			if (!collide) {
				wave++;
				collide = true;

				//target = new NetworkUtils().RamdomValues(50, 100);

				angle = (float)arrAngles[wave-1];
				power = (float)arrPowers[wave-1];
				height = (float)arrHight[wave-1];

				float inAngle = angle;
				float inObjDown = bodyObj.getPosition().x;
				float inPower = power;
				float inTarget = target;
				float inWeight = weight;
				float inHight = height;

				if(bodyTwer != null)
					world.destroyBody(bodyTwer);

				if(circle != null)
					world.destroyBody(circle);

				if(bodyLnchr != null)
					world.destroyBody(bodyLnchr);

				BodyTower(2, LauncherY, LauncherX, LauncherY);
				BodyLauncher(1, 2, LauncherX, LauncherY, angle);
				BodyBase(LauncherX, LauncherY, 2);

				Trainner(inAngle, inPower, inObjDown, inTarget, inWeight, inHight);
			}
		}

		if ((CollisionBox("ground", "shot") || CollisionBox("alvo", "shot")) && neural){
			if (!collide) {
				wave++;
				collide = true;

				float inAngle = angle;
				float inObjDown = bodyObj.getPosition().x;
				float inPower = power;
				float inTarget = target;
				float inWeight = weight;
				float inHight = height;

				//TestingNN(inAngle, inPower, inObjDown, inTarget, inWeight, inHight);
			}
		}

		if(count > -1){
			count++;
			System.out.print(".");

			if(count > 100){
				count = -1;
				System.out.println();

				float inAngle = angle;
				float inObjDown = bodyObj.getPosition().x;
				float inPower = power;
				float inTarget = target;
				float inWeight = weight;
				float inHight = height;

				//TestingNN(inAngle, inPower, inObjDown, inTarget, inWeight, inHight);
			}
		}

		PanelInfo();

		world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	}

	@Override
	public void create() {
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();

		box2DCamera = new OrthographicCamera();

		if(WIDTH == 1123) {
			// Desktop
			box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 3), (HEIGHT) / (PPM / 3) );
			box2DCamera.position.set(55, 25, 0);

			pathDataSet = "NeurophProject_Bang/Training Sets/";
			pathNetwork = "NeurophProject_Bang/Neural Networks/";

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

		Gdx.input.setInputProcessor(this);

		batch = new SpriteBatch();

		font1 = new BitmapFont(Gdx.files.internal("fonts/verdana20.fnt"));
		font1.setColor(Color.WHITE);
		font1.getData().setScale(1f, 1f);

		// Clear NeuronNetwork and Dataset
		new NetworkUtils().DeleteFileNN(pathDataSet + FileDataset);
		new NetworkUtils().DeleteFileNN(pathNetwork + ".nnet" + FileNetwork);

		// Defaults values for shots
		DefaultShots();

		angle = (float)arrAngles[0];
		power = (float)arrPowers[0];
		height = (float)arrHight[0];

		//PreTrainner();
		//shot = false;
		//neural = true;

		// Objects
		bodyTarget(1.0f,1.0f, target,5.0f, BodyDef.BodyType.StaticBody);
		BodyTower(2, LauncherY, LauncherX, LauncherY);
		BodyLauncher(1, 2, LauncherX, LauncherY, angle);
		BodyBase(LauncherX, LauncherY, 2);
		BodyGround(WIDTH / 2, 0, 0, 0, true);
		Rotate(LauncherX, LauncherY, 4, angle);

		status = "Please, tap or click in to the screen to start learn...";

		// Start Shot
		Shot( LauncherX, LauncherY, power, weight, angle);

		//wave++;
		//System.out.println("\nShots...");
	}

	@Override
	public boolean keyDown(int keycode) {

		if (keycode == Input.Keys.LEFT) {
			angle -= .1;
			//Rotate(LauncherX, LauncherY, 6, angle);
			System.out.println(angle);
		}

		if (keycode == Input.Keys.RIGHT) {
			angle += .1;
			//Rotate(LauncherX, LauncherY, 6, angle);
			System.out.println(angle);

		}

		if (keycode == Input.Keys.UP) {
			LauncherY += 1;
			//BodyTower(2, LauncherY, LauncherX, LauncherY);
			//Rotate(LauncherX, LauncherY, 6, angle);
			System.out.println(LauncherY);

		}

		if (keycode == Input.Keys.DOWN) {
			LauncherY -= 1;
			//BodyTower(2, LauncherY, LauncherX, LauncherY);
			//Rotate(LauncherX, LauncherY, 6, angle);
			System.out.println(LauncherY);

		}

		if(bodyTwer != null)
			world.destroyBody(bodyTwer);

		if(circle != null)
			world.destroyBody(circle);

		if(bodyLnchr != null)
			world.destroyBody(bodyLnchr);

		BodyTower(2, LauncherY, LauncherX, LauncherY);
		BodyLauncher(1, 2, LauncherX, LauncherY, angle);
		BodyBase(LauncherX, LauncherY, 2);

		Rotate(LauncherX, LauncherY, 4, angle);

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(button == Input.Buttons.LEFT){
			status = "Ramdom Shots...";
			Shot( LauncherX, LauncherY, power, weight, angle);
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
