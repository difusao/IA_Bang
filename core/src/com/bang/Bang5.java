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
    String status = "";
    int[] layers = new int[]{1, 6, 2};
    int weightstotal = 26;
    double[][] weights = new double[wavetotal][weightstotal];
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

            targetX = 150;
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
            System.out.printf(Locale.US, "%01d)", i);
            for (int j = 0; j < weightstotal; j++)
                System.out.printf(Locale.US, " %20.17f", weights[i][j]);
            System.out.println();
        }
        System.out.println();

        System.out.println("OUTPUTS --------------------------------------------------------------");
        for(int i=0; i<wavetotal; i++)
            System.out.printf(Locale.US, "%01d) %20.17f %20.17f%n",i , angle[i], power[i]);
        System.out.println();

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
            int bestID = BestObjDownID(lstObjDown, targetX);
            float bestValue =  BestObjDownValue(lstObjDown, targetX);

            // Compare with best general
            //System.out.println();
            //System.out.println("bestValue - targetX = " + (bestValue-targetX) + " | ObjDownBest - targetX = " + (ObjDownBest-targetX));
            //System.out.println(bestValue + " - " + targetX + " = " + (bestValue-targetX) + " | " + ObjDownBest + " - " + targetX + " = " + (ObjDownBest-targetX));

            if( Math.abs(bestValue-targetX) < (Math.abs(ObjDownBest-targetX)) && (Math.abs(ObjDownBest-targetX) > 0) ){
                // Update best shots
                ObjDownBest = bestValue;
                weightsBest = weights[bestID];

                // Clone best weights
                weights = CloneWeights(weights[bestID], mut);

                // Test all weights
                TestNN();
            }else {
                weights = CloneWeights(weightsBest, mut);

                // Test all weights
                TestNN();
            }

            System.out.println();
            System.out.println("BEST SHOTS -----------------------------------------------------------");
            System.out.printf(Locale.US, "%01d) %20.17f%n", bestID, bestValue);
            System.out.printf(Locale.US, "%01d) %s%n", bestID, Arrays.toString(weights[bestID]));
            System.out.println();
            System.out.printf(Locale.US, "Score shot: %20.17f%n", (ObjDownBest==999999999?0.0:ObjDownBest));
            System.out.printf(Locale.US, "Score weights: %s%n", (ObjDownBest==999999999?0:Arrays.toString(weightsBest)));
            System.out.println();

            System.out.println("CLONE BEST WEIGHTS ---------------------------------------------------");

            for(int k=0; k<wavetotal; k++) {
                System.out.printf(Locale.US, "%01d)", k);
                for (int j = 0; j < weightstotal; j++)
                    System.out.printf(Locale.US, " %20.17f", weights[k][j]);
                System.out.println();
            }
            System.out.println();

            // -------------------------------------------------------------------------------------
            /*
            count = 0;

            TestNN();

            for(int x=0; x<wavetotal; x++)
                lstObjDown[x] = 0;

            for(int x=0; x<wavetotal; x++) {
                Shot(LauncherX, LauncherY, power[x], weight, angle[x], x);
            }

            gen++;
            */
        }
    }

    private double[][] CloneWeights(double[] rWeights, double mut) {
        double[][] weightsTMP = new double[rWeights.length][rWeights.length];

        // Best weights in first row
        weightsTMP[0] = rWeights;

        // Clone left weights
        for(int i=1; i<rWeights.length; i++) {
            double[] row = new double[rWeights.length];
            for (int j = 0; j < rWeights.length; j++) {
                double rnd = Math.random();
                if (rnd > mut)
                    row[j] = rWeights[j];
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

        //weights[0] =  new double[]{ -0.4463709592819214, 0.2449253797531128, -0.14493107795715332, -0.5159571170806885, -0.4090670347213745, -0.1841285228729248, -0.5661183595657349, -0.3934938907623291, -0.25599372386932373, -0.5934962034225464, -0.12035763263702393, 0.17912280559539795, -0.6707326173782349, 0.9899841547012329, -0.3817044496536255, 0.02834618091583252, 0.8339242935180664, -0.7566311359405518, -0.05399513244628906, -0.5518976449966431, 0.9628150463104248, -0.052072882652282715, 0.04367518424987793, 0.3512105941772461, -0.1395416259765625, -0.8761434555053711 };
        //weights[1] =  new double[]{ -0.34687936305999756, -0.7705357074737549, -0.04880654811859131, 0.4655367136001587, 0.34830963611602783, -0.47219419479370117, -0.559154748916626, 0.36501479148864746, -0.7416129112243652, 0.3097078800201416, 0.49428486824035645, 0.10481154918670654, 0.2189873456954956, 0.3437645435333252, 0.5658566951751709, 0.18786346912384033, 0.782800555229187, 0.6564323902130127, -0.7282423973083496, -0.8264597654342651, -0.8296927213668823, -0.9665534496307373, -0.5727014541625977, 0.43430209159851074, 0.2939335107803345, 0.16794133186340332 };
        //weights[2] =  new double[]{ 0.6304073333740234, 0.38968372344970703, -0.29929399490356445, 0.1691126823425293, 0.08539557456970215, -0.7208151817321777, -0.48671889305114746, -0.038854360580444336, -0.09654045104980469, -0.044333577156066895, -0.5532248020172119, -0.40785670280456543, 0.8765264749526978, 0.02179431915283203, 0.2530500888824463, 0.2506457567214966, -0.43646693229675293, -0.8606793880462646, 0.8073511123657227, -0.2561739683151245, -0.7836271524429321, -0.4089261293411255, -0.4318528175354004, 0.8324657678604126, -0.685043454170227, -0.433635950088501 };
        //weights[2] =  new double[]{ -0.8251053094863892, 0.6633611917495728, -0.12159419059753418, -0.869695782661438, 0.09445083141326904, 0.8083107471466064, 0.018887639045715332, 0.907712459564209, 0.5822018384933472, 0.38585686683654785, -0.3244287967681885, 0.2829592227935791, -0.5728764533996582, 0.737586498260498, -0.39510416984558105, -0.23418796062469482, 0.8692783117294312, 0.3129338026046753, 0.6934159994125366, 0.058949828147888184, -0.04349362850189209, 0.4722421169281006, 0.1377427577972412, -0.7508710622787476, -0.9066689014434814, -0.3610299825668335 };
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

                    // clone all best weights
                    //weights = CloneWeights(weightsBest, 0);
                    for(int x=0; x<wavetotal; x++)
                        weights[x] = weights[0];
                    status = "Success!";
                    //targetX = RamdomValues(50f, 150.0f);
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
        font1[i].draw(batch, "Gen: " + String.format(Locale.US, "%03d", gen), 33, (HEIGHT - 10));
        font1[i].draw(batch, "Score: " + (ObjDownBest==999999999?0:String.format(Locale.US, "%10.8f",ObjDownBest)), 145, (HEIGHT - 10));
        font1[i].draw(batch, "Status: " + status, 285, (HEIGHT - 10));

        if(BestObjDownID(lstObjDown, targetX) == i)
            font1[i].setColor(Color.GREEN);
        else
            font1[i].setColor(Color.WHITE);

        font1[i].draw(batch, i + ") Angle: " + String.format(Locale.US, "%10.9f", angle[i]), 10, (HEIGHT - 30) - (i * 20));
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
        font1[i].draw(batch, "Score: " + (ObjDownBest==999999999?0:String.format(Locale.US, "%10.8f",ObjDownBest)), 275, (HEIGHT - 30));
        font1[i].draw(batch, "Status: " + status, 520, (HEIGHT - 30));

        if(BestObjDownID(lstObjDown, targetX) == i)
            font1[i].setColor(Color.GREEN);
        else
            font1[i].setColor(Color.WHITE);

        font1[i].draw(batch, i + ") Angle: " + String.format(Locale.US, "%10.9f", angle[i]), 10, (HEIGHT - 60) - (i * 30));
        font1[i].draw(batch, " Power: " + String.format(Locale.US, "%10.9f", power[i]), 265, (HEIGHT - 60) - (i * 30));
        font1[i].draw(batch, " Distance: " + (lstObjDown[i]==0&&bodyObj[i]!=null?String.format(Locale.US, "%10.9f", bodyObj[i].getPosition().x):String.format(Locale.US, "%10.9f", lstObjDown[i])), 510, (HEIGHT - 60) - (i * 30));

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

        TestNN();

        for(int i=0; i<wavetotal; i++)
            lstObjDown[i] = 0;

        for(int i=0; i<wavetotal; i++) {
            Shot(LauncherX, LauncherY, power[i], weight, angle[i], i);
        }

        gen++;

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
