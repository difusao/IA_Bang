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
    int wavetotal = 20;
    int wave = 0;
    int gen = 0;
    double mut = 0.05;
    float[] lstObjDown = new float[wavetotal];
    NeuralNetWork nn;
    String pathDataSet;
    String pathNetwork;
    int[] layers = new int[]{1, 6, 2};
    int weightstotal = 26;
    double[][] weights = new double[wavetotal][weightstotal];
    double[][] weightsTMP = new double[wavetotal][weightstotal];
    double[] weightsBest = new double[weightstotal];
    double ObjDownBest = 999999999;

    int count = 0;

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
    float targetX = 150;
    float targetY = 1;
    float weight = 50;
    boolean[] collide = new boolean[wavetotal];

    // Panel info
    BitmapFont[] font1 = new BitmapFont[wavetotal];
    String status = "";
    int contShotsOk = 0;
    boolean[] isCollideGround = new boolean[wavetotal];
    boolean[] isCollideTarget = new boolean[wavetotal];

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        float Box2DCamX;
        float Box2DCamY;
        float ViewportCamW;
        float ViewportCamH;
        float ViewportOrthoW;
        float ViewportOrthoH;

        float fontscaleX;
        float fontscaleY;

        batch = new SpriteBatch();

        if(WIDTH == 1123) {
            // NeuralNetwork
            pathDataSet = "NeurophProject_Bang/Training Sets/DataSet/DataSet.tset";
            pathNetwork = "NeurophProject_Bang/Neural Networks/NeuralNetwork.nnet";

            Box2DCamX = WIDTH/5;
            Box2DCamY = HEIGHT/5;
            ViewportCamW = WIDTH/5;
            ViewportCamH = HEIGHT/5;
            ViewportOrthoW = 110.0f;
            ViewportOrthoH = 53.0f;

            fontscaleX = 1.0f;
            fontscaleY = 1.0f;

            targetX = 200;
        }else {
            // NeuralNetwork
            pathDataSet = "/data/data/com.bang/files/DataSet.tset";
            pathNetwork = "/data/data/com.bang/files/NeuralNetwork.nnet";

            Box2DCamX = WIDTH/15;
            Box2DCamY = HEIGHT/15;
            ViewportCamW = WIDTH/15;
            ViewportCamH = HEIGHT/15;
            ViewportOrthoW = 73.0f;
            ViewportOrthoH = 35.0f;

            fontscaleX = 2.0f;
            fontscaleY = 2.0f;

            targetX = 125;
        }
        nn = new NeuralNetWork(pathDataSet, pathNetwork);

        // Create NeuralNetwork
        nn.CreateMLP(layers);

        // Load NeuralNetwork
        nn.LoadMLP();

        // Generate Weights Ramdom
        WeightsRamdom();

        for(int i=0; i<wavetotal; i++){
            // Get Weights and test Neural Network
            TestNN();

            // Collide
            collide[i] = true;
            isCollideGround[i] = false;
            isCollideTarget[i] = false;

            box2DCamera[i] = new OrthographicCamera(Box2DCamX, Box2DCamY);
            box2DCamera[i].setToOrtho(false, ViewportCamW, ViewportCamH);
            box2DCamera[i].position.set(ViewportOrthoW, ViewportOrthoH, 0.0f);
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
            //Shot(LauncherX, LauncherY, power[i], weight, angle[i], i);

            font1[i] = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
            font1[i].setColor(Color.WHITE);
            font1[i].getData().setScale(fontscaleX, fontscaleY);
        }

        TerminalLog();

        Gdx.input.setInputProcessor(this);

        nn.SaveMLP();
    }

    private void TerminalLog(){
        System.out.println("WEIGHTS RAMDOMS ------------------------------------------------------");
        for(int i=0; i<wavetotal; i++) {
            System.out.printf(Locale.US, "%02d)", i);
            for (int j = 0; j < weightstotal; j++)
                System.out.printf(Locale.US, " %20.17f,", weights[i][j]);
            System.out.println();
        }
        System.out.println();

        //System.out.println("OUTPUTS --------------------------------------------------------------");
        //for(int i=0; i<wavetotal; i++)
        //    System.out.printf(Locale.US, "%02d) %20.17f %20.17f%n",i , angle[i], power[i]);
        //System.out.println();

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

        if ( (isCollideTarget[i])) {
            weightsTMP[contShotsOk] = weights[i];
            contShotsOk++;

            if(contShotsOk == wavetotal) {
                targetX = RamdomValues(50, 200);
                gen = 1;
            }
        }

        if(collide[i] && count < wavetotal) {
            System.out.printf(Locale.US, "%02d) %s %20.17f%n", i, ((isCollideTarget[i])?"[x]":"[ ]"), lstObjDown[i]);
            status = "In target " + String.format(Locale.US, "%02d", contShotsOk) + " of " + wavetotal;
            count++;
        }

        if(count == wavetotal) {
            int bestID = BestObjDownID(lstObjDown, targetX);
            float bestValue =  BestObjDownValue(lstObjDown, targetX);

            if( Math.abs(bestValue-targetX) < (Math.abs(ObjDownBest-targetX)) && (Math.abs(ObjDownBest-targetX) > 0) ){
                // Update best shots
                ObjDownBest = bestValue;
                weightsBest = weights[bestID];
            }

            // update weights
            weights = CloneWeights(weightsTMP, weights[bestID], mut);

            System.out.println("BEST SHOTS -----------------------------------------------------------");
            System.out.printf(Locale.US, "%02d) %20.17f%n", bestID, bestValue);
            System.out.printf(Locale.US, "%02d) %s%n", bestID, Arrays.toString(weightsBest));
            System.out.println();
            System.out.printf(Locale.US, "Score shot: %20.17f%n", (ObjDownBest==999999999?0.0:ObjDownBest));
            System.out.printf(Locale.US, "Score weights: %s%n", (ObjDownBest==999999999?0:Arrays.toString(weightsBest)));
            System.out.println();

            System.out.println("CLONE BEST WEIGHTS ---------------------------------------------------");
            for(int k=0; k<wavetotal; k++) {
                System.out.printf(Locale.US, "%02d)", k);
                for (int j = 0; j < weightstotal; j++)
                    System.out.printf(Locale.US, " %20.17f", weights[k][j]);
                System.out.println();
            }
            System.out.println();

            System.out.println("SHOTS ----------------------------------------------------------------");

            GoShot();
        }
    }

    private double[][] CloneWeights(double[][] rWeights, double[] weights, double mut) {
        double[][] weightsTMP = new double[wavetotal][rWeights.length];

        // Add shots in target
        System.out.println();
        for(int i = 0; i < contShotsOk; i++)
            weightsTMP[i] = rWeights[i];

        // Clone left weights
        for(int i = contShotsOk; i < wavetotal; i++) {
            double[] row = new double[weights.length];

            for (int j = 0; j < weights.length; j++) {
                double rnd = Math.random();
                if (rnd > mut)
                    row[j] = weights[j];
                else
                    row[j] = RamdomValues(-1.0000000000f, 1.0000000000f);
            }

            weightsTMP[i] = row;
        }

        return weightsTMP;
    }

    private void TestNN(){
        for(int i=0; i<wavetotal; i++) {
            // Get weights and set NeuralNetwork on items
            for (int j = 0; j < weightstotal; j++) {
                // Define weights for shots
                nn.setWeights(weights[i]);

                // Test NeuralNetwork
                double[] output = nn.TestNN(new double[]{targetX/100});
                angle[i] = (float) output[0];
                power[i] = (float) output[1] * 100;
            }
        }
    }

    private void WeightsRamdom(){
        // Create ramdom weights for shots
        for(int i=0; i<wavetotal; i++)
            for(int j = 0; j< weightstotal; j++)
                weights[i][j] = RamdomValues(-1.0000000000f, 1.0000000000f);

        //for(int i=0; i<wavetotal; i++)
        //    System.out.printf(Locale.US, "%02d) %s%n", i, Arrays.toString(weights[i]));

        //for(int i=0; i<wavetotal; i++)
            //weights[i] =  new double[]{  };
            //weights[i] =  new double[]{ -0.516033411026001, 0.497334361076355, 0.5127986669540405, 0.06625854969024658, 0.25939440727233887, 0.5349220037460327, 0.3483976125717163, 0.8801760673522949, -0.3862677812576294, 0.012795329093933105, -0.523124098777771, -0.3990107774734497, 0.8353897333145142, -0.09325134754180908, -0.002365589141845703, -0.767318844795227, -0.4648704528808594, -0.13404035568237305, -0.8132435083389282, -0.06044185161590576, 0.9873210191726685, -0.8146086931228638, -0.6286664009094238, -0.7939869165420532, -0.28446531295776367, 0.4184170961380005 };
            //weights[i] =  new double[]{ 0.4297689199447632, -0.06293034553527832, 0.40840625762939453, 0.01166534423828125, -0.5065199136734009, -0.5891550779342651, -0.6165467500686646, 0.9321153163909912, 0.3323153257369995, -0.8587610721588135, 0.20369112491607666, 0.48814094066619873, -0.494551420211792, -0.6173491477966309, -0.9936074018478394, 0.9305453300476074, -0.5579774379730225, 0.8062552213668823, -0.35338687896728516, 0.6824487447738647, -0.7807424068450928, -0.07200789451599121, 0.4872884750366211, -0.9437826871871948, 0.2532517910003662, -0.811790943145752 };
            //weights[i] =  new double[]{ -0.5306599140167236, -0.944368839263916, 0.49415338039398193, 0.9817030429840088, 0.4562723636627197, 0.8086578845977783, -0.19939017295837402, -0.8012961149215698, 0.7533892393112183, 0.5949645042419434, -0.751994252204895, -0.0941230058670044, 0.6486148834228516, -0.5716716051101685, 0.45661818981170654, 0.9156582355499268, 0.40384674072265625, -0.1956801414489746, -0.7426458597183228, 0.25793421268463135, -0.35682547092437744, -0.594407320022583, -0.40703368186950684, 0.3741365671157837, 0.8117971420288086, -0.5331063270568848 };
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

            if(WIDTH == 1123)
                PanelScoreDesktop(i);
            else
                PanelScoreAndroid(i);

            world[i].step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }

    private void PanelScoreDesktop(int i){
        float targX;
        float targY;
        float objX;
        float objY;
        float corrX;
        float corrY;

        targX = targetX*5;
        targY = targetY*5+25;
        objX = 5;
        objY = 5;
        corrX = -1.7f;
        corrY = 5.0f;

        batch.begin();

        font1[i].setColor(Color.WHITE);
        font1[i].draw(batch, "Gen: " + String.format(Locale.US, "%03d", gen), 40, (HEIGHT - 10));
        font1[i].draw(batch, "Score: " + (ObjDownBest==999999999?0:String.format(Locale.US, "%9.7f",ObjDownBest)), 145, (HEIGHT - 10));
        font1[i].draw(batch, "Status: " + status, 285, (HEIGHT - 10));

        if(BestObjDownID(lstObjDown, targetX) == i)
            font1[i].setColor(Color.GREEN);
        else
            font1[i].setColor(Color.DARK_GRAY);

        font1[i].draw(batch, String.format(Locale.US, "%02d",i ) + ") Angle: " + String.format(Locale.US, "%9.8f", angle[i]), 10, (HEIGHT - 30) - (i * 20));
        font1[i].draw(batch, " Power: " + String.format(Locale.US, "%10.9f", power[i]), 140, (HEIGHT - 30) - (i * 20));
        font1[i].draw(batch, " Distance: " + (lstObjDown[i]==0&&bodyObj[i]!=null?String.format(Locale.US, "%10.9f", bodyObj[i].getPosition().x):String.format(Locale.US, "%10.9f", lstObjDown[i])), 270, (HEIGHT - 30) - (i * 20));

        font1[i].draw(batch, String.valueOf(i) , (bodyObj[i]!=null?(bodyObj[i].getPosition().x-corrX)*objX:0), (bodyObj[i]!=null?(bodyObj[i].getPosition().y+corrY)*objY:0));
        font1[i].setColor(Color.YELLOW);
        font1[i].draw(batch, String.format(Locale.US, "%01.0f", targetX) , targX, targY);

        batch.end();
    }

    private void PanelScoreAndroid(int i){
        float targX;
        float targY;
        float objX;
        float objY;
        float corrX;
        float corrY;

        targX = targetX*15-32;
        targY = targetY*15+55;
        objX = 15;
        objY = 15;
        corrX = 1.0f;
        corrY = 4.0f;

        batch.begin();

        font1[i].setColor(Color.WHITE);
        font1[i].draw(batch, "Generat:  " + String.format(Locale.US, "%03d", gen), 10, (HEIGHT - 30));
        font1[i].draw(batch, "Score: " + (ObjDownBest==999999999?0:String.format(Locale.US, "%10.8f",ObjDownBest)), 285, (HEIGHT - 30));
        font1[i].draw(batch, "Status: " + status, 550, (HEIGHT - 30));

        if(BestObjDownID(lstObjDown, targetX) == i)
            font1[i].setColor(Color.GREEN);
        else
            font1[i].setColor(Color.WHITE);

        font1[i].draw(batch, i + ") Angle: " + String.format(Locale.US, "%10.9f", angle[i]), 10, (HEIGHT - 60) - (i * 30));
        font1[i].draw(batch, " Power: " + String.format(Locale.US, "%10.9f", power[i]), 275, (HEIGHT - 60) - (i * 30));
        font1[i].draw(batch, " Distance: " + (lstObjDown[i]==0&&bodyObj[i]!=null?String.format(Locale.US, "%10.9f", bodyObj[i].getPosition().x):String.format(Locale.US, "%10.9f", lstObjDown[i])), 520, (HEIGHT - 60) - (i * 30));

        font1[i].draw(batch, String.valueOf(i) , (bodyObj[i]!=null?(bodyObj[i].getPosition().x-corrX)*objX:0), (bodyObj[i]!=null?(bodyObj[i].getPosition().y+corrY)*objY:0));
        font1[i].setColor(Color.YELLOW);
        font1[i].draw(batch, String.format(Locale.US, "%01.0f", targetX) , targX, targY);

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

    private void GoShot(){
        contShotsOk = 0;
        count = 0;

        TestNN();

        for(int i=0; i<wavetotal; i++)
            lstObjDown[i] = 0;

        for(int i=0; i<wavetotal; i++) {
            Shot(LauncherX, LauncherY, power[i], weight, angle[i], i);
        }

        gen++;
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

        GoShot();

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
