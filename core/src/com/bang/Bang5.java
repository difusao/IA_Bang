package com.bang;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
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
import com.nn.NeuralNetWork;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class Bang5 implements ApplicationListener, InputProcessor {

    // Real World
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    // GA & NN
    int wavetotal = 3;
    int wave = 0;
    int gen = 0;
    double mut = 0.05;
    float[] lstObjDown = new float[wavetotal];
    NeuralNetWork nn;
    String pathDataSet;
    String pathNetwork;

    // World
    OrthographicCamera[] box2DCamera = new OrthographicCamera[wavetotal];
    Box2DDebugRenderer[] debugRenderer = new Box2DDebugRenderer[wavetotal];
    World[] world = new World[wavetotal];
    SpriteBatch batch;
    int WIDTH;
    int HEIGHT;

    // Objects
    Body[] bodyObj = new Body[wavetotal];
    Body[] bodyLnchr = new Body[wavetotal];
    Body[] bodyTarget = new Body[wavetotal];
    Body[] circle = new Body[wavetotal];
    float[] angle = new float[wavetotal];
    float[] power = new float[wavetotal];
    float LauncherX = 5;
    float LauncherY = 2;
    float targetX = 100;
    float targetY = 1;
    float weight = 50;
    boolean[] collide = new boolean[wavetotal];

    // Panel info
    BitmapFont[] font1 = new BitmapFont[wavetotal];

    boolean[] isCollideGround = new boolean[wavetotal];
    boolean[] isCollideTarget = new boolean[wavetotal];
    int[] layers = new int[]{1, 6, 2};
    int weightstotal = 26;
    double[][] weights = new double[wavetotal][weightstotal];
    double[][] weightsBest = new double[wavetotal][weightstotal];
    double[] ObjDownBest = new double[wavetotal];

    int count = 0;

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        batch = new SpriteBatch();

        // NeuralNetwork
        pathDataSet = "NeurophProject_Bang/Training Sets/DataSet/DataSet.tset";
        pathNetwork = "NeurophProject_Bang/Neural Networks/NeuralNetwork.nnet";
        nn = new NeuralNetWork(pathDataSet, pathNetwork);

        // Create NeuralNetwork
        nn.CreateMLP(layers);

        // Load NeuralNetwork
        nn.LoadMLP();

        // Create ramdom weights for shots
        for(int i=0; i<wavetotal; i++)
            for(int j = 0; j< weightstotal; j++)
                weights[i][j] = RamdomValues(-1.0000000000f, 1.0000000000f);

        for(int i=0; i<wavetotal; i++){
            // Get weights and set NeuralNetwork on items
            for(int j = 0; j< weightstotal; j++) {
                // Define weights for shots
                nn.setWeights(weights[i]);

                // Test NeuralNetwork
                double[] output = nn.TestNN(new double[]{targetX/100});
                angle[i] = (float) output[0];
                power[i] = (float) output[1] * 100;
            }

            // Collide
            collide[i] = true;
            isCollideGround[i] = false;
            isCollideTarget[i] = false;

            box2DCamera[i] = new OrthographicCamera(WIDTH/10, HEIGHT/10);
            box2DCamera[i].setToOrtho(false, WIDTH/5, HEIGHT/5);
            box2DCamera[i].position.set(110.0f, 53.0f, 0.0f);
            box2DCamera[i].update();

            world[i] = new World(new Vector2(0.0f,-9.8f),true);

            debugRenderer[i] = new Box2DDebugRenderer();

            // Objects
            BodyTarget(1.0f, 1.0f, targetX, targetY, BodyDef.BodyType.StaticBody, i);
            BodyTower(2.0f, LauncherY, LauncherX, LauncherY, i);
            BodyLauncher(1.0f, 2.0f, LauncherX, LauncherY, angle[i], i);
            BodyBase(LauncherX, LauncherY, 2.0f, i);
            BodyGround(WIDTH, 0.0f, 0.0f, 0.0f, true, i);
            Rotate(LauncherX, LauncherY, 4.0f, angle[i], i);
            Shot(LauncherX, LauncherY, power[i], weight, angle[i], i);

            font1[i] = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
            font1[i].setColor(Color.WHITE);
            font1[i].getData().setScale(1.0f, 1.0f);
        }

        TerminalLog();

        Gdx.input.setInputProcessor(this);
    }

    private void TerminalLog(){
        System.out.println("WEIGHTS RAMDOMS ------------------------------------------------------");
        for(int i=0; i<wavetotal; i++) {
            System.out.printf(Locale.US, "%01d)", i);
            for (int j = 0; j < weightstotal; j++)
                System.out.printf(Locale.US, " %20.17f", weights[i][j]);
            System.out.println();
        }

        System.out.println("OUTPUTS --------------------------------------------------------------");
        for(int i=0; i<wavetotal; i++)
            System.out.printf(Locale.US, "%01d) %20.17f %20.17f%n",i , angle[i], power[i]);
        System.out.println("SHOTS ----------------------------------------------------------------");
    }

    private int BestObjDownID(float[] objsdown, float ref){
        float v = 999999999;
        int id = 0;

        for(int i=0; i<objsdown.length; i++)
            if(Math.abs(objsdown[i] - ref)< v) {
                v = Math.abs(objsdown[i] - ref);
                id = i;
            }

        return id;
    }

    private float BestObjDownValue(float[] objsdown, float ref){
        float v = 999999999;
        float value = 0.0f;

        for(int i=0; i<objsdown.length; i++)
            if(Math.abs(objsdown[i] - ref)< v) {
                v = Math.abs(objsdown[i] - ref);
                value = objsdown[i];
            }

        return value;
    }

    private void Trainner(int i){
        lstObjDown[i] = bodyObj[i].getPosition().x;

        //
        if(collide[i] && count < wavetotal) {
            System.out.printf(Locale.US, "%01d) %20.17f%n",i , lstObjDown[i]);
            count++;
        }

        if(count == wavetotal) {
            System.out.println("BEST SHOTS -----------------------------------------------------------");
            System.out.printf(Locale.US, "%01d) %20.17f%n", BestObjDownID(lstObjDown, targetX), BestObjDownValue(lstObjDown, targetX));
            System.out.printf(Locale.US, "%01d) %s%n", BestObjDownID(lstObjDown, targetX), Arrays.toString(weights[BestObjDownID(lstObjDown, targetX)]));
            System.out.println("CLONE BEST WEIGHTS ---------------------------------------------------");
            weights = CloneWeights(weights, mut);

            for(int k=0; k<wavetotal; k++) {
                System.out.printf(Locale.US, "%01d)", k);
                for (int j = 0; j < weightstotal; j++)
                    System.out.printf(Locale.US, " %20.17f", weights[k][j]);
                System.out.println();
            }
        }
    }

    private double[][] CloneWeights(double[][] rWeights, double mut) {
        double[][] weightsTMP = new double[rWeights.length][rWeights[0].length];

        for(int i=0; i<rWeights.length; i++) {
            double[] row = new double[rWeights[i].length];
            for (int j=0; j<rWeights[i].length; j++)
                if (Math.random() > mut)
                    row[j] = rWeights[i][j];
                else
                    row[j] = RamdomValues(-1.0000000000f, 1.0000000000f);

            weightsTMP[i] = row;
        }

        return weightsTMP;
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        for(int i=0; i<wavetotal; i++) {
            debugRenderer[i].render(world[i], box2DCamera[i].combined);
            box2DCamera[i].update();

            isCollideGround[i] = CollisionBox("ground", "shot", i);
            isCollideTarget[i] = CollisionBox("alvo", "shot", i);

            bodyTarget[i].setTransform(targetX, targetY, 0);

            if ( (isCollideGround[i])) {
                if (!collide[i]) {
                    collide[i] = true;

                    // Trainner
                    Trainner(i);
                }
            }

            if ( (isCollideTarget[i])) {
                if (!collide[i]) {
                    collide[i] = true;

                    // Trainner
                    Trainner(i);
                }
            }

            PanelScore(i);
            //System.out.println("Best value: " + BestObjDownID(lstObjDown, targetX) + ") " + BestObjDownValue(lstObjDown, targetX));

            world[i].step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }

    private void PanelScore(int i){
        batch.begin();

        if(BestObjDownID(lstObjDown, targetX) == i)
            font1[i].setColor(Color.GREEN);
        else
            font1[i].setColor(Color.WHITE);

        font1[i].draw(batch, i + ") Angle: " + String.format(Locale.US, "%10.9f", angle[i]), 10, (HEIGHT - 10) - (i * 20));
        font1[i].draw(batch, " Power: " + String.format(Locale.US, "%10.9f", power[i]), 140, (HEIGHT - 10) - (i * 20));
        font1[i].draw(batch, " Distance: " + (lstObjDown[i]==0?String.format(Locale.US, "%10.9f", bodyObj[i].getPosition().x):String.format(Locale.US, "%10.9f", lstObjDown[i])), 270, (HEIGHT - 10) - (i * 20));
        font1[i].setColor(Color.DARK_GRAY);
        font1[i].draw(batch, String.valueOf(i) , bodyObj[i].getPosition().x*5+8, bodyObj[i].getPosition().y*5+23);
        font1[i].draw(batch, String.format(Locale.US, "%01.0f", targetX) , targetX*5+8, targetY*5+23);

        batch.end();
    }

    public float RamdomValues(float  min, float  max){
        Random b = new Random();
        return min + (max - min) * b.nextFloat();
    }

    private void Shot(float x, float y, float power, float weight, float ang, int id){
            if(bodyObj[id] != null) {
                world[id].destroyBody(bodyObj[id]);
                collide[id] = true;
            }

            if(collide[id]) {
                // Rotation
                float tx = (float)Math.sin((ang - 1.5f) * (-1));
                float ty = (float)Math.cos((ang - 1.5f) * (-1));

                // Angle
                float arctan = (float)Math.atan(tx / ty);

                Rotate(LauncherX, LauncherY, 4, angle[id], id);

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
                        weight,
                        id,
                        world[id]);

                collide[id] = false;
            }
    }

    private void Rotate(float x, float y, float axe, float ang, int id){

        // Rotation
        float tx = (float)Math.sin((ang - 1.5f) * (-1));
        float ty = (float)Math.cos((ang - 1.5f) * (-1));

        // Angle
        float arctan = (float)Math.atan(tx / ty);

        bodyLnchr[id].setTransform((tx * axe) + x, (ty * axe) + y, arctan * (-1));
        circle[id].setTransform( x, y, arctan * (-1) + (1.5f) );
    }

    private void BodyTarget(float w, float h, float x, float y, BodyDef.BodyType bodytype, int id){
        BodyDef bodyTargetDef = new BodyDef();
        bodyTargetDef.type = bodytype;
        bodyTargetDef.position.set(x, (y*2));

        bodyTarget[id] = world[id].createBody(bodyTargetDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBoxDef = new FixtureDef();
        fixtureBoxDef.shape = shape;
        fixtureBoxDef.density = 10;

        Fixture fixtureBox = bodyTarget[id].createFixture(fixtureBoxDef);
        bodyTarget[id].setUserData("alvo");

        shape.dispose();
    }

    private void BodyGround(float w, float h, float x, float y, boolean collision, int id) {
        BodyDef bodyGroundDef = new BodyDef();
        bodyGroundDef.type = BodyDef.BodyType.StaticBody;
        bodyGroundDef.position.set(x, y);

        Body bodyGnd = world[id].createBody(bodyGroundDef);

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

    private void BodyShot(float w, float h, float inpulseX, float inpulseY, float transformX, float transformY, float angle, float x, float y, float weight, int id, World world) {
        BodyDef bodyObjDef = new BodyDef();
        bodyObjDef.type = BodyDef.BodyType.DynamicBody;
        bodyObjDef.position.set(x, y);

        bodyObj[id] = world.createBody(bodyObjDef);
        bodyObj[id].applyLinearImpulse(inpulseX, inpulseY, x, y, true);
        bodyObj[id].setTransform( transformX, transformY, angle );

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureObjDef = new FixtureDef();
        fixtureObjDef.density = weight;
        fixtureObjDef.friction = 5.0f;
        fixtureObjDef.restitution = 0.00f;
        fixtureObjDef.shape = shape;

        Fixture fixtureBall = bodyObj[id].createFixture(fixtureObjDef);

        bodyObj[id].setUserData("shot");

        shape.dispose();
    }

    private void BodyBase(float x, float y, float radius, int id){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        circle[id] = world[id].createBody(bodyDef);

        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 0.4f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.6f;

        circle[id].createFixture(fixtureDef);


        dynamicCircle.dispose();
    }

    private void BodyTower(float w, float h, float x, float y, int id){
        BodyDef bodyTowerDef = new BodyDef();
        bodyTowerDef.type = BodyDef.BodyType.KinematicBody;
        bodyTowerDef.position.set(x, y/2);

        Body bodyTwer = world[id].createBody(bodyTowerDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h/2);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyTwer.createFixture(fixtureDef1);
    }

    private void BodyLauncher(float w, float h, float x, float y, float ang, int id) {

        BodyDef bodyLauncherDef = new BodyDef();
        bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
        bodyLauncherDef.position.set(x, (y+w+h));

        bodyLnchr[id] = world[id].createBody(bodyLauncherDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyLnchr[id].createFixture(fixtureDef1);
    }

    private boolean CollisionBox(String a, String b, int id) {
        boolean touch = false;

        for (int i = 0; i < world[id].getContactCount(); i++) {
            Contact contact = world[id].getContactList().get(i);

            if (contact.isTouching()) {

                Body contactA = contact.getFixtureA().getBody();
                Body contactB = contact.getFixtureB().getBody();

                if(contactA.getUserData().equals(a) && contactB.getUserData().equals(b)) {
                    touch = true;
                }
            }
        }

        return touch;
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

        for(int i=0; i<wavetotal; i++)
            font1[i].dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
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

        count = 0;

        for(int i=0; i<wavetotal; i++)
            lstObjDown[i] = 0;

        for(int i=0; i<wavetotal; i++) {
            Shot(LauncherX, LauncherY, power[i], weight, angle[i], i);
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
