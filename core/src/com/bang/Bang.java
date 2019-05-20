package com.bang;

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
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
    int gen = 1;

    // Objects
    float angle = 0;
    float LauncherX = 5;
    float LauncherY = 2;
    float power = 30;
    float targetX = 90;
    float targetY = 1;
    float weight = 50;
    float height = 0;

    float maxhight = 0;

    int count = -1;
    boolean shot = true;
    double[] arrAngles = new double[10];
    double[] arrPowers = new double[10];
    double[] arrHight = new double[10];
    double tx = 0;
    double ty = 0;
    double ang = 0;

    Body bodyGnd;
    Body[] bodyObj = new Body[waveTotal];
    Body bodyLnchr;
    Body bodyTarget;
    Body circle;
    Body bodyTwer;

    boolean[] collide = new boolean[waveTotal];
    List posMark = new ArrayList();

    // Form
    SpriteBatch batch;
    BitmapFont font1;
    BitmapFont font2;
    String status;

    boolean isCollideGround = false;
    boolean isCollideTarget = false;

    // NeuralNetwork
    DataSet trainingSet1 = new DataSet(1, 2);
    DataSet trainingSet2 = new DataSet(1, 2);
    String FileDataset = "DataSet.tset";
    String FileNetwork = "NewNeuralNetwork";
    String pathDataSet = "data/";
    String pathNetwork = "data/";

    float[] shots = new float[waveTotal];

    boolean neural = false;

    private void Rotate(float x, float y, float axe, float ang) {

        // Rotation
        float tx = (float) Math.sin((ang - 1.5f) * (-1));
        float ty = (float) Math.cos((ang - 1.5f) * (-1));

        // Angle
        float arctan = (float) Math.atan(tx / ty);

        bodyLnchr.setTransform((tx * axe) + x, (ty * axe) + y, arctan * (-1));
        circle.setTransform(x, y, arctan * (-1) + (1.5f));

    }

    private void Shot(int index, float x, float y, float power, float weight, float ang) {

        // Ramdom 10 shots defaults
        //DefaultShots();

        if(bodyObj[index] != null)
            collide[index] = true;

        if (collide[index]) {
            // Rotation
            float tx = (float) Math.sin((ang - 1.5f) * (-1));
            float ty = (float) Math.cos((ang - 1.5f) * (-1));

            // Angle
            float arctan = (float) Math.atan(tx / ty);

            Rotate(LauncherX, LauncherY, 4, angle);

            BodyShot(
                    index,
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

            collide[index] = false;
        }
    }

    private void BodyShot(int index, float w, float h, float inpulseX, float inpulseY, float transformX, float transformY, float angle, float x, float y, float weight) {
        BodyDef bodyObjDef = new BodyDef();
        bodyObjDef.type = BodyDef.BodyType.DynamicBody;
        bodyObjDef.position.set(x, y);
        bodyObj[index] = world.createBody(bodyObjDef);
        bodyObj[index].applyLinearImpulse(inpulseX, inpulseY, x, y, true);
        bodyObj[index].setTransform(transformX, transformY, angle);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBallDef = new FixtureDef();
        fixtureBallDef.density = weight;
        fixtureBallDef.friction = 5.0f;
        fixtureBallDef.restitution = 0.00f;
        fixtureBallDef.shape = shape;

        Fixture fixtureBall = bodyObj[index].createFixture(fixtureBallDef);

        bodyObj[index].setUserData("shot"+index);

        shape.dispose();
    }

    private boolean CollisionBox(String a, String b) {
        boolean touch = false;

        for (int i = 0; i < world.getContactCount(); i++) {
            Contact contact = world.getContactList().get(i);

            if (contact.isTouching()) {

                Body contactA = contact.getFixtureA().getBody();
                Body contactB = contact.getFixtureB().getBody();

                if (contactA.getUserData().equals(a) && contactB.getUserData().equals(b))
                    touch = true;
            }
        }

        return touch;
    }

    public float MaxValue(float[] trainingSet, float limit){
        float row = 0;
        float max = 0;

        for(int i=0; i<trainingSet.length; i++){
            float col = trainingSet[i];
            if(col > max && col <= limit){
                max = col;
                row = trainingSet[i];
            }
        }

        return row;
    }

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        box2DCamera = new OrthographicCamera();

        if (WIDTH == 1123) {
            // Desktop
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 3), (HEIGHT) / (PPM / 3));
            box2DCamera.position.set(55, 25, 0);

            pathDataSet = "NeurophProject_Bang/Training Sets/";
            pathNetwork = "NeurophProject_Bang/Neural Networks/";

            targetX = 90;
        } else {
            // Smartphone
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM) + 10, (HEIGHT) / (PPM) + 10);
            box2DCamera.position.set(PPM * 1.25f, PPM / 2 + 5, 0);

            pathDataSet = "/data/data/com.bang/files/";
            pathNetwork = "/data/data/com.bang/files/";

            targetX = 75;
        }

        box2DCamera.update();
        world = new World(new Vector2(0, -9.8f), true);
        debugRenderer = new Box2DDebugRenderer();

        Gdx.input.setInputProcessor(this);

        batch = new SpriteBatch();

        font1 = new BitmapFont(Gdx.files.internal("fonts/verdana20.fnt"));
        font1.setColor(Color.WHITE);
        font1.getData().setScale(1f, 1f);

        font2 = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
        font2.setColor(Color.WHITE);
        font2.getData().setScale(1f, 1f);

        // Ramdom 10 shots defaults
        DefaultShots();

        // Objects
        bodyTarget(1.0f, 1.0f, targetX, 5.0f, BodyDef.BodyType.StaticBody);
        BodyTower(2, LauncherY, LauncherX, LauncherY);
        BodyLauncher(1, 2, LauncherX, LauncherY, angle);
        BodyBase(LauncherX, LauncherY, 2);
        BodyGround(WIDTH / 2, 0, 0, 0, true);
        Rotate(LauncherX, LauncherY, 4, angle);

        for(int i=0; i<waveTotal; i++)
            collide[i] = true;

        for(int i=0; i<waveTotal; i++) {
            angle = (float) arrAngles[i];
            power = (float) arrPowers[i];
            Shot(i, LauncherX, LauncherY, power, weight, angle);
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(world, box2DCamera.combined);
        box2DCamera.update();

        bodyTarget.setTransform(targetX, targetY, 0);

        if(neural){

            MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, new int[]{1, 6, 2});
            myMlPerceptron.setLabel(FileNetwork);
            BackPropagation lr = new BackPropagation();
            lr.setLearningRate(0.01);
            lr.setMaxError(99999999);
            lr.setNeuralNetwork(myMlPerceptron);
            myMlPerceptron.setLearningRule(lr);
            myMlPerceptron.learn(trainingSet1);

            neural = false;
        }

        for(int i=0; i<waveTotal; i++) {
            isCollideGround = CollisionBox("ground", "shot" + i);
            isCollideTarget = CollisionBox("alvo", "shot" + i);

            if ( (isCollideGround || isCollideTarget) ) {
                if (!collide[i]) {
                    collide[i] = true;

                    shots[i] = bodyObj[i].getPosition().x;
                    trainingSet1.getRows().get(i).getInput()[0] = bodyObj[i].getPosition().x;
                    world.destroyBody(bodyObj[i]);
                    wave++;

                    if( (wave == waveTotal) )
                        neural = true;
                }
            }
        }

        PanelInfo();

        world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    private void BodyBase(float x, float y, float radius) {
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

    private void BodyTower(float w, float h, float x, float y) {
        BodyDef bodyTowerDef = new BodyDef();
        bodyTowerDef.type = BodyDef.BodyType.KinematicBody;
        bodyTowerDef.position.set(x, y / 2);

        bodyTwer = world.createBody(bodyTowerDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h / 2);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyTwer.createFixture(fixtureDef1);
    }

    private void BodyLauncher(float w, float h, float x, float y, float ang) {

        BodyDef bodyLauncherDef = new BodyDef();
        bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
        bodyLauncherDef.position.set(x, (y + w + h));

        bodyLnchr = world.createBody(bodyLauncherDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyLnchr.createFixture(fixtureDef1);
    }

    private void bodyTarget(float w, float h, float x, float y, BodyDef.BodyType bodytype) {
        BodyDef bodyTargetDef = new BodyDef();
        bodyTargetDef.type = bodytype;
        bodyTargetDef.position.set(x, (y * 2));

        bodyTarget = world.createBody(bodyTargetDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBoxDef = new FixtureDef();
        fixtureBoxDef.shape = shape;
        fixtureBoxDef.density = 10;

        Fixture fixtureBox = bodyTarget.createFixture(fixtureBoxDef);
        bodyTarget.setUserData("alvo");

        shape.dispose();
    }

    private void BodyGround(float w, float h, float x, float y, boolean collision) {
        BodyDef bodyGroundDef = new BodyDef();
        bodyGroundDef.type = BodyDef.BodyType.StaticBody;
        bodyGroundDef.position.set(x, y);

        bodyGnd = world.createBody(bodyGroundDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1;

        Fixture fixture = bodyGnd.createFixture(fixtureDef);

        if (collision)
            bodyGnd.setUserData("ground");

        shape.dispose();
    }

    private void PanelInfo() {
        batch.begin();
        font1.draw(batch, "Generation: " + gen, 10, HEIGHT - 10);
        font1.draw(batch, "Target: " + targetX, 10, HEIGHT - 40);
        font1.draw(batch, "Status: " + status, 10, HEIGHT - 70);
        font1.draw(batch, "Wave: " + waveTotal, 10, HEIGHT - 100);

        for(int i=0; i<waveTotal; i++){

            if(bodyObj[i]!=null && MaxValue(shots, targetX) == bodyObj[i].getPosition().x)
                font2.setColor(Color.RED);
            else
                font2.setColor(Color.WHITE);

            font2.draw(batch, "Shot " + i + ": " + String.format(Locale.US, "%09.6f", (bodyObj[i]!=null?trainingSet1.getRows().get(i).getInput()[0]:0)) + ", " + String.format(Locale.US, "%09.6f", trainingSet1.getRows().get(i).getDesiredOutput()[0]) + ", " + String.format(Locale.US, "%09.6f", trainingSet1.getRows().get(i).getDesiredOutput()[1]) , 875, (HEIGHT - 10 - (i*20)));
        }

        batch.end();
    }

    public float RamdomValues(float  min, float  max){
        Random b = new Random();
        return min + (max - min) * b.nextFloat();
    }

    private void DefaultShots() {
        trainingSet1.clear();

        /*
        arrAngles[0] = 00.17923162877559662;
        arrAngles[1] = 00.63603001832962040;
        arrAngles[2] = 00.43882015347480774;
        arrAngles[3] = 00.64072030782699580;
        arrAngles[4] = 01.21366596221923830;
        arrAngles[5] = 01.10968124866485600;
        arrAngles[6] = 00.82887417078018190;
        arrAngles[7] = 00.92093235254287720;
        arrAngles[8] = 00.05375795066356659;
        arrAngles[9] = 01.27366924285888670;

        arrPowers[0] = 34.91004180908203000;
        arrPowers[1] = 23.51820373535156200;
        arrPowers[2] = 09.61052036285400400;
        arrPowers[3] = 21.64569473266601600;
        arrPowers[4] = 23.34945297241211000;
        arrPowers[5] = 34.01906204223633000;
        arrPowers[6] = 31.24951171875000000;
        arrPowers[7] = 24.02785491943359400;
        arrPowers[8] = 27.48722457885742200;
        arrPowers[9] = 27.25409889221191400;
        */

        // Default values
        //System.out.printf(Locale.US, "------------------------+---------------------%n");
        //System.out.printf(Locale.US, "         ANGLE          |        POWER        %n");
        //System.out.printf(Locale.US, "------------------------+---------------------%n");

        for (int i = 0; i < waveTotal; i++){
            arrAngles[i] = RamdomValues(0, 1.3f);
            arrPowers[i] = RamdomValues(6.0f, 35);
            trainingSet1.addRow(new DataSetRow(new double[]{0}, new double[]{arrAngles[i], arrPowers[i]}));
        }

            //System.out.printf(Locale.US, "%02d %020.17f | %020.17f%n", i, arrAngles[i], arrPowers[i]);
        //System.out.println();
    }

    @Override
    public void resize(int width, int height) {

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
        font2.dispose();
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

        if (bodyTwer != null)
            world.destroyBody(bodyTwer);

        if (circle != null)
            world.destroyBody(circle);

        if (bodyLnchr != null)
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
        if (button == Input.Buttons.LEFT) {
            DefaultShots();

            /*
            if(wave < waveTotal){
                angle = (float) arrAngles[wave];
                power = (float) arrPowers[wave];
                Shot(wave, LauncherX, LauncherY, power, weight, angle);
                wave++;
            }

            if(wave==waveTotal)
                wave = 0;
            */

            for(int i=0; i<waveTotal; i++) {
                angle = (float) arrAngles[i];
                power = (float) arrPowers[i];
                Shot(i, LauncherX, LauncherY, power, weight, angle);
            }
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