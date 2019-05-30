package com.bang;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Bang4 extends ApplicationAdapter{

    // Real World
    public static final int PPM = 30;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    OrthographicCamera box2DCamera;
    Box2DDebugRenderer debugRenderer;
    World world;

    int WIDTH;
    int HEIGHT;

    // AG
    int waveTotal = 3;
    int wave = -1;
    int gen = 1;
    double mut = 0.05;

    // Objects
    float angle = 01.10968124866485600f;
    float LauncherX = 5;
    float LauncherY = 2;
    float power = 34.01906204223633000f;
    float targetX = 90;
    float targetY = 1;
    float weight = 100;

    // Objects
    Body bodyObj;
    Body bodyLnchr;
    Body bodyTarget;
    Body circle;

    boolean collide = true;

    // Form
    SpriteBatch batch;
    BitmapFont font1;
    BitmapFont font2;
    String status = "Trainnering...";

    // NeuralNetwork
    String FileDataset = "DataSet.tset";
    String FileNetwork = "NewNeuralNetwork";
    String pathDataSet = "data/";
    String pathNetwork = "data/";

    NeurophStudio rna;
    NetworkUtils nnu;

    boolean isCollideGround = false;
    boolean isCollideTarget = false;
    boolean test = false;
    double[][] rndWeights;
    int layers[] = new int[]{1, 6, 2};
    boolean fitness = false;
    List lstTMPObjDown = new ArrayList();
    double score = 999999999;
    double[] bestweight = new double[26];
    float posmark = 9999999999f;

    private void PanelInfo(){
        batch.begin();

        font1.draw(batch, "Angle: " + angle, 10, HEIGHT - 10);
        font1.draw(batch, "Power: " + power, 10, HEIGHT - 40);
        font1.draw(batch, "Target: " + targetX, 10, HEIGHT - 70);
        font1.draw(batch, "Status: " + status, 10, HEIGHT - 100);
        font1.draw(batch, "Wave: " + (wave) + "/" + waveTotal, 10, HEIGHT - 130);
        font1.draw(batch, "Generations: " + gen, 10, HEIGHT - 160);

        batch.end();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(world, box2DCamera.combined);

        box2DCamera.update();

        isCollideGround = CollisionBox("ground", "shot");
        isCollideTarget = CollisionBox("alvo", "shot");

        bodyTarget.setTransform(targetX, targetY, 0);

        if(wave > 0)
            status = "Learning...";

        if ( wave < waveTotal && (isCollideGround || isCollideTarget) && !test) {
            if (!collide) {
                collide = true;

                Trainner();

                if(isCollideTarget){
                    status = "Successful!!!";
                    fitness = true;
                    rna.setWeightsX(bestweight);
                    wave = 0;
                    waveTotal = 1;
                }
            }
        }

        PanelInfo();

        SquareMark(2,2, posmark, .3f);

        world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    private void Trainner(){
        System.out.println(wave + ")  ObjDown: " + (bodyObj.getPosition().x / 100) + " Angle: " + angle + " Power: " + power + " Weights: " + Arrays.toString(rndWeights[wave]));
        lstTMPObjDown.add(Math.abs(bodyObj.getPosition().x/100-0.9));
        //posmark = bodyObj.getPosition().x;

        wave++;

        if(wave == waveTotal) {
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            // Best shot and weights
            System.out.println("Best shots generation (rowWeights) ### Score = " + score + " 1o. Weight = " + bestweight[0] + " ###");
            Object minval = Collections.min(lstTMPObjDown);

            int bestID = lstTMPObjDown.indexOf(minval);
            double[] tmpWeight = new double[26];

            if(Math.abs(new Double(minval.toString())) < score) {
                System.out.println("############################################ Record! ############################################ " +  Math.abs(new Double(minval.toString())) + " (" + score + ")");
                score = Math.abs(new Double(minval.toString()));
                posmark = Math.abs((new Float(minval.toString())-0.9f)*100);
                bestweight = rndWeights[bestID];
                tmpWeight = bestweight;
                rna.setWeightsX(bestweight);
            }else{
                tmpWeight = rndWeights[bestID];
                rna.setWeightsX(rndWeights[0]);
            }

            System.out.println(bestID +") " + Arrays.toString(rndWeights[bestID]));
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

            // Clone weights
            System.out.println("Clone " + waveTotal + " Best Weights (rndWeights)");
            rndWeights = CloneWeights(waveTotal, tmpWeight, mut);

            for(int i=0; i< rndWeights.length; i++) {
                System.out.printf(Locale.US, "%01d)", i);
                for (int j = 0; j < rndWeights[i].length; j++)
                    System.out.printf(Locale.US, " %20.17f", rndWeights[i][j]);
                System.out.println();
            }

            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Shots generation " + (gen+1));
            rna.setWeightsX(rndWeights[0]);
            lstTMPObjDown.clear();

            if(!fitness)
                gen++;

            wave=0;
        }

        rna.setWeightsX(rndWeights[wave]);
        // Test Weights in shots
        double[] outputNewWeight = rna.TestX(new double[]{(targetX / 100)});
        angle = (float) outputNewWeight[0];
        power = (float) outputNewWeight[1] * 100;
        Shot(LauncherX, LauncherY, power, weight, angle);
    }

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        box2DCamera = new OrthographicCamera();

        //if(WIDTH == 1123) {
        if(WIDTH == 1350) {
            // Desktop
            //box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 6), (HEIGHT) / (PPM / 6) );
            //box2DCamera.position.set(130, 73, 0);

            box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 3), (HEIGHT) / (PPM / 3) );
            box2DCamera.position.set(70, 37.0f, 0);

            pathDataSet = "NeurophProject_Bang/Training Sets/";
            pathNetwork = "NeurophProject_Bang/Neural Networks/";

            targetX = 90;
        }else{
            // Smartphone
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM) + 10, (HEIGHT) / (PPM) + 10 );
            box2DCamera.position.set(PPM*1.25f+6, PPM / 2+7, 0);

            pathDataSet = "/data/data/com.bang/files/";
            pathNetwork = "/data/data/com.bang/files/";

            targetX = 75;
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

        font2 = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
        font2.setColor(Color.WHITE);
        font2.getData().setScale(1f, 1f);

        // Clear NeuronNetwork and Dataset
        new NetworkUtils().DeleteFileNN(pathDataSet + FileDataset);
        new NetworkUtils().DeleteFileNN(pathNetwork + FileNetwork + ".nnet");

        // Create Neural Network
        rna.CreateMLP(layers, FileNetwork + ".nnet");

        // Objects
        bodyTarget(1.0f,1.0f, targetX,5.0f, BodyDef.BodyType.StaticBody);
        BodyTower(2, LauncherY, LauncherX, LauncherY);
        BodyLauncher(1, 2, LauncherX, LauncherY, angle);
        BodyBase(LauncherX, LauncherY, 2);
        BodyGround(WIDTH, 0, 0, 0, true);
        Rotate(LauncherX, LauncherY, 4, angle);

        // Defaults weights ramdoms
        rndWeights = RamdomWeights(waveTotal,26);

        // Start Shots
        //rna.setWeights(FileNetwork + ".nnet", rndWeights[0]);
        rna.setWeightsX(rndWeights[0]);

        //double[] outputNewWeight = rna.Test(FileNetwork + ".nnet", new double[]{(targetX/100)});
        double[] outputNewWeight = rna.TestX(new double[]{(targetX/100)});
        angle = (float) outputNewWeight[0];
        power = (float) outputNewWeight[1] * 100;
        Shot(LauncherX, LauncherY, power, weight, angle);

        System.out.println("Ramdom Weights (rndWeights)");
        for(int i=0; i< rndWeights.length; i++) {
            System.out.printf(Locale.US, "%01d)", i);
            for (int j = 0; j < rndWeights[i].length; j++)
                System.out.printf(Locale.US, " %20.17f", rndWeights[i][j]);
            System.out.println();
        }

        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Shots generation " + gen);
        wave = 0;
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

    private double[][] RamdomWeights(int rows, int cols){
        double[][] weights = new double[rows][cols];

        for(int i=0; i<rows; i++)
            for(int j=0; j<cols; j++)
                weights[i][j] = nnu.RamdomValues(-1.0000000000f, 1.0000000000f);

        return weights;
    }

    private double[][] CloneWeights(int wave, double[] rWeights, double mut) {
        double[][] weights = new double[wave][rWeights.length];

        for(int i=0; i<wave; i++)
            weights[i] = rWeights;

        for(int i=1; i<wave; i++) {
            double[] row = new double[rWeights.length];
            for (int j = 0; j<rWeights.length; j++)
                if (Math.random() > mut)
                    row[j] = rWeights[j];
                else
                    row[j] = nnu.RamdomValues(-1.0000000000f, 1.0000000000f);

            weights[i] = row;
        }

        return weights;
    }

    private void SquareMark(float w, float h, float x, float y){
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(box2DCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x-1, y-0.05f, w, h);
        shapeRenderer.end();
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
        BodyDef bodyObjDef = new BodyDef();
        bodyObjDef.type = BodyDef.BodyType.DynamicBody;
        bodyObjDef.position.set(x, y);
        bodyObj = world.createBody(bodyObjDef);
        bodyObj.applyLinearImpulse(inpulseX, inpulseY, x, y, true);
        bodyObj.setTransform( transformX, transformY, angle );

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBallDef = new FixtureDef();
        fixtureBallDef.density = weight;
        fixtureBallDef.friction = 5.0f;
        fixtureBallDef.restitution = 0.00f;
        fixtureBallDef.shape = shape;

        Fixture fixtureBall = bodyObj.createFixture(fixtureBallDef);

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
        BodyDef bodyTowerDef = new BodyDef();
        bodyTowerDef.type = BodyDef.BodyType.KinematicBody;
        bodyTowerDef.position.set(x, y/2);

        Body bodyTwer = world.createBody(bodyTowerDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h/2);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyTwer.createFixture(fixtureDef1);
    }

    private void BodyLauncher(float w, float h, float x, float y, float ang) {

        BodyDef bodyLauncherDef = new BodyDef();
        bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
        bodyLauncherDef.position.set(x, (y+w+h));

        bodyLnchr = world.createBody(bodyLauncherDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyLnchr.createFixture(fixtureDef1);
    }

    private boolean CollisionBox(String a, String b) {
        boolean touch = false;

        for (int i = 0; i < world.getContactCount(); i++) {
            Contact contact = world.getContactList().get(i);

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

    private void bodyTarget(float w, float h,float x, float y, BodyDef.BodyType bodytype){
        BodyDef bodyTargetDef = new BodyDef();
        bodyTargetDef.type = bodytype;
        bodyTargetDef.position.set(x, (y*2));

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

        Body bodyGnd = world.createBody(bodyGroundDef);

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
}
